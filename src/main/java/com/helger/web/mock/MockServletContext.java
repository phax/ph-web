/**
 * Copyright (C) 2014 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.web.mock;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.SystemProperties;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.Nonempty;
import com.helger.commons.annotations.UnsupportedOperation;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.io.IReadableResource;
import com.helger.commons.io.IReadableResourceProvider;
import com.helger.commons.io.resourceprovider.DefaultResourceProvider;
import com.helger.commons.mime.MimeTypeInfoManager;
import com.helger.commons.string.StringHelper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

//ESCA-JAVA0116:
/**
 * Mock implementation of the {@link ServletContext} interface.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class MockServletContext implements ServletContext
{
  public static final int SERVLET_SPEC_MAJOR_VERSION = 2;
  public static final int SERVLET_SPEC_MINOR_VERSION = 5;
  public static final String DEFAULT_SERVLET_CONTEXT_NAME = "MockServletContext";
  public static final String DEFAULT_SERVLET_CONTEXT_PATH = "";
  private static final Logger s_aLogger = LoggerFactory.getLogger (MockServletContext.class);

  private final IReadableResourceProvider m_aResourceProvider;
  private final String m_sResourceBasePath;
  private String m_sContextPath = DEFAULT_SERVLET_CONTEXT_PATH;
  private final Map <String, ServletContext> m_aContexts = new HashMap <String, ServletContext> ();
  private final Properties m_aInitParameters = new Properties ();
  private final Map <String, Object> m_aAttributes = new HashMap <String, Object> ();
  private String m_sServletContextName = DEFAULT_SERVLET_CONTEXT_NAME;
  private final MockServletPool m_aServletPool;
  private boolean m_bInvalidated = false;

  /**
   * Create a new MockServletContext, using no base path and a
   * DefaultIResourceProvider (i.e. the classpath root as WAR root).
   */
  public MockServletContext ()
  {
    this (null, "", null, null);
  }

  /**
   * Create a new MockServletContext, using no base path and a
   * DefaultIResourceProvider (i.e. the classpath root as WAR root).
   *
   * @param aInitParams
   *        The init parameter
   */
  public MockServletContext (@Nullable final Map <String, String> aInitParams)
  {
    this (null, "", null, aInitParams);
  }

  /**
   * Create a new MockServletContext.
   *
   * @param sContextPath
   *        The context path to use. May be <code>null</code>.
   */
  public MockServletContext (@Nullable final String sContextPath)
  {
    this (sContextPath, "", null, null);
  }

  /**
   * Create a new MockServletContext.
   *
   * @param sContextPath
   *        Context path to use. May be <code>null</code>.
   * @param aInitParams
   *        The init parameter The context path to use
   */
  public MockServletContext (@Nullable final String sContextPath, @Nullable final Map <String, String> aInitParams)
  {
    this (sContextPath, "", null, aInitParams);
  }

  /**
   * Create a new MockServletContext.
   *
   * @param sContextPath
   *        The context path to use
   * @param sResourceBasePath
   *        the WAR root directory (should not end with a slash)
   */
  public MockServletContext (@Nullable final String sContextPath, @Nullable final String sResourceBasePath)
  {
    this (sContextPath, sResourceBasePath, null, null);
  }

  /**
   * Create a new MockServletContext.
   *
   * @param sContextPath
   *        The context path to use
   * @param sResourceBasePath
   *        the WAR root directory (should not end with a slash)
   * @param aResourceLoader
   *        the IReadableResourceProvider to use (or null for the default)
   */
  public MockServletContext (@Nullable final String sContextPath,
                             @Nullable final String sResourceBasePath,
                             @Nullable final IReadableResourceProvider aResourceLoader,
                             @Nullable final Map <String, String> aInitParams)
  {
    setContextPath (sContextPath);
    m_aResourceProvider = aResourceLoader != null ? aResourceLoader : new DefaultResourceProvider ();
    m_sResourceBasePath = sResourceBasePath != null ? sResourceBasePath : "";

    // Use JVM temp dir as ServletContext temp dir.
    final String sTempDir = SystemProperties.getTmpDir ();
    if (sTempDir != null)
      setAttribute ("tempdir", new File (sTempDir));

    if (aInitParams != null)
      for (final Map.Entry <String, String> aEntry : aInitParams.entrySet ())
        addInitParameter (aEntry.getKey (), aEntry.getValue ());

    // Invoke all event listeners
    final ServletContextEvent aSCE = new ServletContextEvent (this);
    for (final ServletContextListener aListener : MockHttpListener.getAllServletContextListeners ())
      aListener.contextInitialized (aSCE);

    m_aServletPool = new MockServletPool (this);
  }

  /**
   * Build a full resource location for the given path, prepending the resource
   * base path of this MockServletContext.
   *
   * @param sPath
   *        the path as specified
   * @return the full resource path
   */
  @Nonnull
  protected String getResourceLocation (@Nonnull final String sPath)
  {
    return StringHelper.startsWith (sPath, '/') ? m_sResourceBasePath + sPath : m_sResourceBasePath + "/" + sPath;
  }

  public final void setContextPath (@Nullable final String sContextPath)
  {
    if (sContextPath == null)
      m_sContextPath = "";
    else
      if (StringHelper.startsWith (sContextPath, '/'))
        m_sContextPath = sContextPath;
      else
        m_sContextPath = "/" + sContextPath;
  }

  /* This is a Servlet API 2.5 method. */
  @Nonnull
  public String getContextPath ()
  {
    return m_sContextPath;
  }

  public void registerContext (@Nonnull final String sContextPath, @Nonnull final ServletContext aContext)
  {
    ValueEnforcer.notNull (sContextPath, "ContextPath");
    ValueEnforcer.notNull (aContext, "Context");
    m_aContexts.put (sContextPath, aContext);
  }

  @Nullable
  public ServletContext getContext (@Nullable final String sContextPath)
  {
    if (m_sContextPath.equals (sContextPath))
      return this;
    return m_aContexts.get (sContextPath);
  }

  @Nonnegative
  public int getMajorVersion ()
  {
    return SERVLET_SPEC_MAJOR_VERSION;
  }

  @Nonnegative
  public int getMinorVersion ()
  {
    return SERVLET_SPEC_MINOR_VERSION;
  }

  @Nullable
  public String getMimeType (@Nonnull final String sFilename)
  {
    return MimeTypeInfoManager.getDefaultInstance ().getPrimaryMimeTypeStringForFilename (sFilename);
  }

  @UnsupportedOperation
  @Deprecated
  public Set <String> getResourcePaths (final String sPath)
  {
    throw new UnsupportedOperationException ();
  }

  @Nullable
  public URL getResource (@Nonnull final String sPath) throws MalformedURLException
  {
    final IReadableResource aResource = m_aResourceProvider.getReadableResource (getResourceLocation (sPath));
    if (!aResource.exists ())
      return null;
    return aResource.getAsURL ();
  }

  @Nullable
  public InputStream getResourceAsStream (@Nonnull final String sPath)
  {
    final IReadableResource aResource = m_aResourceProvider.getReadableResource (getResourceLocation (sPath));
    if (!aResource.exists ())
      return null;
    return aResource.getInputStream ();
  }

  @Nonnull
  public RequestDispatcher getRequestDispatcher (@Nonnull final String sPath)
  {
    if (!StringHelper.startsWith (sPath, '/'))
      throw new IllegalArgumentException ("RequestDispatcher path at ServletContext level must start with '/'");
    return new MockRequestDispatcher (sPath);
  }

  @Nullable
  @Deprecated
  public RequestDispatcher getNamedDispatcher (@Nullable final String sPath)
  {
    return null;
  }

  @Deprecated
  public Servlet getServlet (@Nullable final String sName)
  {
    return null;
  }

  @Deprecated
  @Nonnull
  public Enumeration <Object> getServlets ()
  {
    return ContainerHelper.<Object> getEmptyEnumeration ();
  }

  @Deprecated
  @Nonnull
  public Enumeration <Object> getServletNames ()
  {
    return ContainerHelper.<Object> getEmptyEnumeration ();
  }

  public void log (@Nullable final String message)
  {
    s_aLogger.info (message);
  }

  @Deprecated
  public void log (@Nullable final Exception ex, @Nullable final String sMessage)
  {
    s_aLogger.info (sMessage, ex);
  }

  public void log (@Nullable final String sMessage, @Nullable final Throwable ex)
  {
    s_aLogger.info (sMessage, ex);
  }

  @Nonnull
  @SuppressFBWarnings ("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
  public String getRealPath (@Nonnull final String sPath)
  {
    final IReadableResource aResource = m_aResourceProvider.getReadableResource (getResourceLocation (sPath));
    if (aResource == null)
      throw new IllegalStateException ("Failed to get real path of '" + sPath + "'");
    final File aFile = aResource.getAsFile ();
    if (aFile == null)
      throw new IllegalStateException ("Failed to convert resource " + aResource + " to a file");
    return aFile.getAbsolutePath ();
  }

  @Nonnull
  @Nonempty
  public String getServerInfo ()
  {
    return "MockServletContext";
  }

  @Nullable
  public String getInitParameter (@Nonnull final String sName)
  {
    ValueEnforcer.notNull (sName, "Name");
    return m_aInitParameters.getProperty (sName);
  }

  public final void addInitParameter (@Nonnull final String sName, @Nonnull final String sValue)
  {
    ValueEnforcer.notNull (sName, "Name");
    ValueEnforcer.notNull (sValue, "Value");
    m_aInitParameters.setProperty (sName, sValue);
  }

  @Nonnull
  public Enumeration <Object> getInitParameterNames ()
  {
    return m_aInitParameters.keys ();
  }

  @Nullable
  public Object getAttribute (@Nonnull final String sName)
  {
    ValueEnforcer.notNull (sName, "Name");
    return m_aAttributes.get (sName);
  }

  @Nonnull
  public Enumeration <String> getAttributeNames ()
  {
    return ContainerHelper.getEnumeration (m_aAttributes.keySet ());
  }

  public final void setAttribute (@Nonnull final String sName, @Nullable final Object aValue)
  {
    ValueEnforcer.notNull (sName, "Name");
    if (aValue != null)
      m_aAttributes.put (sName, aValue);
    else
      m_aAttributes.remove (sName);
  }

  public void removeAttribute (@Nonnull final String sName)
  {
    ValueEnforcer.notNull (sName, "Name");
    m_aAttributes.remove (sName);
  }

  public void setServletContextName (@Nullable final String sServletContextName)
  {
    m_sServletContextName = sServletContextName;
  }

  @Nullable
  public String getServletContextName ()
  {
    return m_sServletContextName;
  }

  /**
   * Create a new {@link MockServletConfig} object without servlet init
   * parameters.
   *
   * @param sServletName
   *        Name of the servlet. May neither be <code>null</code> nor empty.
   * @return A new {@link MockServletConfig} object for this servlet context.
   */
  @Nonnull
  public MockServletConfig createServletConfig (@Nonnull @Nonempty final String sServletName)
  {
    return createServletConfig (sServletName, null);
  }

  /**
   * Create a new {@link MockServletConfig} object.
   *
   * @param sServletName
   *        Name of the servlet. May neither be <code>null</code> nor empty.
   * @param aServletInitParams
   *        The map with all servlet init parameters. May be <code>null</code>
   *        or empty.
   * @return A new {@link MockServletConfig} object for this servlet context.
   */
  @Nonnull
  public MockServletConfig createServletConfig (@Nonnull @Nonempty final String sServletName,
                                                @Nullable final Map <String, String> aServletInitParams)
  {
    return new MockServletConfig (this, sServletName, aServletInitParams);
  }

  /**
   * @return The servlet pool for registering mock servlets.
   */
  @Nonnull
  public MockServletPool getServletPool ()
  {
    return m_aServletPool;
  }

  @Nullable
  public MockHttpServletResponse invoke (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    // Find matching servlet
    final String sServletPath = aHttpRequest.getServletPath ();
    final Servlet aServlet = m_aServletPool.getServletOfPath (sServletPath);
    if (aServlet == null)
    {
      s_aLogger.error ("Found no servlet matching '" + sServletPath + "'");
      return null;
    }

    // Main invocation
    final MockHttpServletResponse ret = new MockHttpServletResponse ();
    try
    {
      aServlet.service (aHttpRequest, ret);
    }
    catch (final Throwable t)
    {
      throw new IllegalStateException ("Failed to invoke servlet " + aServlet + " for request " + aHttpRequest, t);
    }
    return ret;
  }

  public void invalidate ()
  {
    if (m_bInvalidated)
      throw new IllegalStateException ("Servlet context already invalidated!");
    m_bInvalidated = true;

    // Destroy all servlets
    m_aServletPool.invalidate ();

    // Call all HTTP listener
    final ServletContextEvent aSCE = new ServletContextEvent (this);
    for (final ServletContextListener aListener : MockHttpListener.getAllServletContextListeners ())
      aListener.contextDestroyed (aSCE);

    m_aAttributes.clear ();
  }
}

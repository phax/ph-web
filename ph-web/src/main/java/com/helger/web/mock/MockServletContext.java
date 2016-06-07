/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.annotation.UnsupportedOperation;
import com.helger.commons.collection.IteratorHelper;
import com.helger.commons.collection.ext.CommonsHashMap;
import com.helger.commons.collection.ext.CommonsLinkedHashMap;
import com.helger.commons.collection.ext.ICommonsMap;
import com.helger.commons.collection.ext.ICommonsOrderedMap;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resourceprovider.DefaultResourceProvider;
import com.helger.commons.io.resourceprovider.IReadableResourceProvider;
import com.helger.commons.lang.priviledged.IPrivilegedAction;
import com.helger.commons.string.StringHelper;
import com.helger.commons.system.SystemProperties;
import com.helger.xml.util.mime.MimeTypeInfoManager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Mock implementation of the {@link ServletContext} interface.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class MockServletContext implements ServletContext
{
  public static final int SERVLET_SPEC_MAJOR_VERSION = 3;
  public static final int SERVLET_SPEC_MINOR_VERSION = 0;
  public static final String DEFAULT_SERVLET_CONTEXT_NAME = "MockServletContext";
  public static final String DEFAULT_SERVLET_CONTEXT_PATH = "";
  private static final Logger s_aLogger = LoggerFactory.getLogger (MockServletContext.class);
  private static final AtomicBoolean s_aReThrowListenerException = new AtomicBoolean (false);

  private final IReadableResourceProvider m_aResourceProvider;
  private final String m_sResourceBasePath;
  private String m_sContextPath = DEFAULT_SERVLET_CONTEXT_PATH;
  private final ICommonsMap <String, ServletContext> m_aContexts = new CommonsHashMap<> ();
  private final ICommonsOrderedMap <String, String> m_aInitParameters = new CommonsLinkedHashMap<> ();
  private final ICommonsMap <String, Object> m_aAttributes = new CommonsHashMap<> ();
  private String m_sServletContextName = DEFAULT_SERVLET_CONTEXT_NAME;
  private final MockServletPool m_aServletPool;
  private boolean m_bInvalidated = false;

  /**
   * @return <code>true</code> if runtime exceptions from context listeners
   *         should be propagated to the outside or if they should be logged and
   *         processing should continue. Default is <code>false</code>.
   */
  public static boolean isReThrowListenerException ()
  {
    return s_aReThrowListenerException.get ();
  }

  /**
   * @param bReThrowListenerException
   *        <code>true</code> to re-throw listener exceptions (on context inited
   *        and destroyed)
   */
  public static void setReThrowListenerException (final boolean bReThrowListenerException)
  {
    s_aReThrowListenerException.set (bReThrowListenerException);
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
   * @param aInitParams
   *        Optional map with initialization parameters
   */
  protected MockServletContext (@Nullable final String sContextPath,
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

    m_aServletPool = new MockServletPool (this);
  }

  @OverrideOnDemand
  protected void initListeners ()
  {
    // Invoke all event listeners after the servlet context object itself
    // finished!
    final ServletContextEvent aSCE = new ServletContextEvent (this);
    for (final ServletContextListener aListener : MockHttpListener.getAllServletContextListeners ())
      try
      {
        aListener.contextInitialized (aSCE);
      }
      catch (final RuntimeException ex)
      {
        if (isReThrowListenerException ())
          throw ex;
        s_aLogger.error ("Failed to call contextInitialized on " + aListener, ex);
      }
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
    if (StringHelper.hasNoText (sContextPath))
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
  public Enumeration <Servlet> getServlets ()
  {
    return IteratorHelper.<Servlet> getEmptyEnumeration ();
  }

  @Deprecated
  @Nonnull
  public Enumeration <String> getServletNames ()
  {
    return IteratorHelper.<String> getEmptyEnumeration ();
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
    return m_aInitParameters.get (sName);
  }

  public final void addInitParameter (@Nonnull final String sName, @Nonnull final String sValue)
  {
    ValueEnforcer.notNull (sName, "Name");
    ValueEnforcer.notNull (sValue, "Value");
    m_aInitParameters.put (sName, sValue);
  }

  @Nonnull
  public Enumeration <String> getInitParameterNames ()
  {
    return IteratorHelper.getEnumeration (m_aInitParameters.keySet ());
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
    return IteratorHelper.getEnumeration (m_aAttributes.keySet ());
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
      try
      {
        aListener.contextDestroyed (aSCE);
      }
      catch (final RuntimeException ex)
      {
        if (isReThrowListenerException ())
          throw ex;
        s_aLogger.error ("Failed to call contextDestroyed on " + aListener, ex);
      }

    m_aAttributes.clear ();
  }

  // Servlet 3.0 API

  public int getEffectiveMajorVersion ()
  {
    return SERVLET_SPEC_MAJOR_VERSION;
  }

  public int getEffectiveMinorVersion ()
  {
    return SERVLET_SPEC_MINOR_VERSION;
  }

  public boolean setInitParameter (final String sName, final String sValue)
  {
    addInitParameter (sName, sValue);
    return true;
  }

  @UnsupportedOperation
  public Dynamic addServlet (final String servletName, final String className)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public Dynamic addServlet (final String servletName, final Servlet servlet)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public Dynamic addServlet (final String servletName, final Class <? extends Servlet> servletClass)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public <T extends Servlet> T createServlet (final Class <T> clazz) throws ServletException
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public ServletRegistration getServletRegistration (final String servletName)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public Map <String, ? extends ServletRegistration> getServletRegistrations ()
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public javax.servlet.FilterRegistration.Dynamic addFilter (final String filterName, final String className)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public javax.servlet.FilterRegistration.Dynamic addFilter (final String filterName, final Filter filter)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public javax.servlet.FilterRegistration.Dynamic addFilter (final String filterName,
                                                             final Class <? extends Filter> filterClass)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public <T extends Filter> T createFilter (final Class <T> clazz) throws ServletException
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public FilterRegistration getFilterRegistration (final String filterName)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public Map <String, ? extends FilterRegistration> getFilterRegistrations ()
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public SessionCookieConfig getSessionCookieConfig ()
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public void setSessionTrackingModes (final Set <SessionTrackingMode> sessionTrackingModes)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public Set <SessionTrackingMode> getDefaultSessionTrackingModes ()
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public Set <SessionTrackingMode> getEffectiveSessionTrackingModes ()
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public void addListener (final String className)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public <T extends EventListener> void addListener (final T t)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public void addListener (final Class <? extends EventListener> listenerClass)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public <T extends EventListener> T createListener (final Class <T> clazz) throws ServletException
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public JspConfigDescriptor getJspConfigDescriptor ()
  {
    throw new UnsupportedOperationException ();
  }

  public ClassLoader getClassLoader ()
  {
    return IPrivilegedAction.getClassLoader (getClass ()).invokeSafe ();
  }

  @UnsupportedOperation
  public void declareRoles (final String... roleNames)
  {
    throw new UnsupportedOperationException ();
  }

  public String getVirtualServerName ()
  {
    return "Mock Server";
  }

  /**
   * Create a new {@link MockServletContext}, using no base path and no context
   * path. The initialization listeners are triggered automatically.
   *
   * @return The created {@link MockServletContext}
   */
  @Nonnull
  public static MockServletContext create ()
  {
    return create (null, "", null, null);
  }

  /**
   * Create a new {@link MockServletContext}, using no base path and no context
   * path using the provided initialization parameters. The initialization
   * listeners are triggered automatically.
   *
   * @param aInitParams
   *        The init parameter. May be <code>null</code>.
   * @return The created {@link MockServletContext}
   */
  @Nonnull
  public static MockServletContext create (@Nullable final Map <String, String> aInitParams)
  {
    return create (null, "", null, aInitParams);
  }

  /**
   * Create a new {@link MockServletContext} using no base path but the provided
   * context path. The initialization listeners are triggered automatically.
   *
   * @param sContextPath
   *        The context path to use. May be <code>null</code>.
   * @return The created {@link MockServletContext}
   */
  @Nonnull
  public static MockServletContext create (@Nullable final String sContextPath)
  {
    return create (sContextPath, "", null, null);
  }

  /**
   * Create a new {@link MockServletContext} using the provided context path and
   * init parameters. The initialization listeners are triggered automatically.
   *
   * @param sContextPath
   *        Context path to use. May be <code>null</code>.
   * @param aInitParams
   *        The init parameter. May be <code>null</code>.
   * @return The created {@link MockServletContext}
   */
  @Nonnull
  public static MockServletContext create (@Nullable final String sContextPath,
                                           @Nullable final Map <String, String> aInitParams)
  {
    return create (sContextPath, "", null, aInitParams);
  }

  /**
   * Create a new {@link MockServletContext} using the provided context path and
   * resource base oath. The initialization listeners are triggered
   * automatically.
   *
   * @param sContextPath
   *        The context path to use. May be <code>null</code>.
   * @param sResourceBasePath
   *        the WAR root directory (should not end with a slash). May be
   *        <code>null</code>.
   * @return The created {@link MockServletContext}
   */
  @Nonnull
  public static MockServletContext create (@Nullable final String sContextPath,
                                           @Nullable final String sResourceBasePath)
  {
    return create (sContextPath, sResourceBasePath, null, null);
  }

  /**
   * Create a new {@link MockServletContext} with all possible parameters.
   *
   * @param sContextPath
   *        The context path to use. May be <code>null</code>.
   * @param sResourceBasePath
   *        the WAR root directory (should not end with a slash). May be
   *        <code>null</code>.
   * @param aResourceLoader
   *        the IReadableResourceProvider to use. May be <code>null</code>.
   * @param aInitParams
   *        Optional map with initialization parameters. May be
   *        <code>null</code>.
   * @return The created {@link MockServletContext}
   */
  @Nonnull
  public static MockServletContext create (@Nullable final String sContextPath,
                                           @Nullable final String sResourceBasePath,
                                           @Nullable final IReadableResourceProvider aResourceLoader,
                                           @Nullable final Map <String, String> aInitParams)
  {
    final MockServletContext ret = new MockServletContext (sContextPath,
                                                           sResourceBasePath,
                                                           aResourceLoader,
                                                           aInitParams);
    ret.initListeners ();
    return ret;
  }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.lang.GenericReflection;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;

/**
 * A pool for registered servlets inside a {@link MockServletContext}.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class MockServletPool
{
  /**
   * Contains a single servlet item
   *
   * @author Philip Helger
   */
  private static final class ServletItem
  {
    private final Servlet m_aServlet;
    private final String m_sServletPath;
    private final String m_sServletPathRegEx;

    @Nonnull
    @Nonempty
    private static String _getAsRegEx (@Nonnull @Nonempty final String sPath)
    {
      String sPathToUse = sPath;
      if (sPathToUse.endsWith ("/*"))
      {
        // Special handling for trailing "/*" which means that a servlet
        // registered for "/test/*" can be found with the servlet path "/test".
        sPathToUse = sPath.substring (0, sPath.length () - 2);
      }

      // Convert wildcard to regex
      return StringHelper.replaceAll (sPathToUse, "*", ".*");
    }

    public ServletItem (@Nonnull final Servlet aServlet, @Nonnull @Nonempty final String sServletPath)
    {
      m_aServlet = ValueEnforcer.notNull (aServlet, "Servlet");
      m_sServletPath = ValueEnforcer.notEmpty (sServletPath, "ServletPath");
      m_sServletPathRegEx = _getAsRegEx (sServletPath);
    }

    @Nonnull
    public Servlet getServlet ()
    {
      return m_aServlet;
    }

    @Nonnull
    @Nonempty
    public String getServletName ()
    {
      return m_aServlet.getServletConfig ().getServletName ();
    }

    @Nonnull
    @Nonempty
    public String getServletPath ()
    {
      return m_sServletPath;
    }

    public boolean matchesPath (@Nonnull final String sServletPath)
    {
      return RegExHelper.stringMatchesPattern (m_sServletPathRegEx, sServletPath);
    }

    @Override
    public String toString ()
    {
      return new ToStringGenerator (this).append ("servlet", m_aServlet)
                                         .append ("servletPath", m_sServletPath)
                                         .toString ();
    }
  }

  private static final Logger s_aLogger = LoggerFactory.getLogger (MockServletPool.class);

  private final MockServletContext m_aSC;
  private final List <ServletItem> m_aServlets = new ArrayList <ServletItem> ();
  private boolean m_bInvalidated = false;

  public MockServletPool (@Nonnull final MockServletContext aSC)
  {
    m_aSC = ValueEnforcer.notNull (aSC, "ServletContext");
  }

  /**
   * Register a new servlet without servlet init parameters
   *
   * @param aServletClass
   *        The class of the servlet to be registered. May not be
   *        <code>null</code>.
   * @param sServletPath
   *        The path where the servlet should listen to requests. May neither be
   *        <code>null</code> nor empty.
   * @param sServletName
   *        The name of the servlet. May neither be <code>null</code> nor empty.
   */
  public void registerServlet (@Nonnull final Class <? extends Servlet> aServletClass,
                               @Nonnull @Nonempty final String sServletPath,
                               @Nonnull @Nonempty final String sServletName)
  {
    registerServlet (aServletClass, sServletPath, sServletName, (Map <String, String>) null);
  }

  /**
   * Register a new servlet
   *
   * @param aServletClass
   *        The class of the servlet to be registered. May not be
   *        <code>null</code>.
   * @param sServletPath
   *        The path where the servlet should listen to requests. May neither be
   *        <code>null</code> nor empty.
   * @param sServletName
   *        The name of the servlet. May neither be <code>null</code> nor empty.
   * @param aServletInitParams
   *        An optional map of servlet init parameters. May be <code>null</code>
   *        or empty.
   */
  public void registerServlet (@Nonnull final Class <? extends Servlet> aServletClass,
                               @Nonnull @Nonempty final String sServletPath,
                               @Nonnull @Nonempty final String sServletName,
                               @Nullable final Map <String, String> aServletInitParams)
  {
    ValueEnforcer.notNull (aServletClass, "ServletClass");
    ValueEnforcer.notEmpty (sServletPath, "ServletPath");

    for (final ServletItem aItem : m_aServlets)
    {
      // Check path uniqueness
      if (aItem.getServletPath ().equals (sServletPath))
        throw new IllegalArgumentException ("Another servlet with the path '" +
                                            sServletPath +
                                            "' is already registered: " +
                                            aItem);
      // Check name uniqueness
      if (aItem.getServletName ().equals (sServletName))
        throw new IllegalArgumentException ("Another servlet with the name '" +
                                            sServletName +
                                            "' is already registered: " +
                                            aItem);
    }

    // Instantiate servlet
    final Servlet aServlet = GenericReflection.newInstance (aServletClass);
    if (aServlet == null)
      throw new IllegalArgumentException ("Failed to instantiate servlet class " + aServletClass);

    final ServletConfig aServletConfig = m_aSC.createServletConfig (sServletName, aServletInitParams);
    try
    {
      aServlet.init (aServletConfig);
    }
    catch (final ServletException ex)
    {
      throw new IllegalStateException ("Failed to init servlet " +
                                       aServlet +
                                       " with configuration  " +
                                       aServletConfig +
                                       " for path '" +
                                       sServletPath +
                                       "'");
    }
    m_aServlets.add (new ServletItem (aServlet, sServletPath));
  }

  /**
   * Find the servlet matching the specified path.
   *
   * @param sPath
   *        The path, relative to the servlet context. May be <code>null</code>.
   * @return <code>null</code> if no {@link Servlet} matching the specified path
   *         was found. If more than one matching servlet was found, the first
   *         one is returned.
   */
  @Nullable
  public Servlet getServletOfPath (@Nullable final String sPath)
  {
    final List <ServletItem> aMatchingItems = new ArrayList <ServletItem> ();
    if (StringHelper.hasText (sPath))
      for (final ServletItem aItem : m_aServlets)
        if (aItem.matchesPath (sPath))
          aMatchingItems.add (aItem);
    final int nMatchingItems = aMatchingItems.size ();
    if (nMatchingItems == 0)
      return null;
    if (nMatchingItems > 1)
      s_aLogger.warn ("Found more than 1 servlet matching path '" + sPath + "' - using first one: " + aMatchingItems);
    return CollectionHelper.getFirstElement (aMatchingItems).getServlet ();
  }

  /**
   * Invalidate the servlet pool, by destroying all contained servlets. Also the
   * list of registered servlets is cleared.
   */
  public void invalidate ()
  {
    if (m_bInvalidated)
      throw new IllegalArgumentException ("Servlet pool already invalidated!");
    m_bInvalidated = true;

    // Destroy all servlets
    for (final ServletItem aServletItem : m_aServlets)
      try
      {
        aServletItem.getServlet ().destroy ();
      }
      catch (final Throwable t)
      {
        s_aLogger.error ("Failed to destroy servlet " + aServletItem, t);
      }

    m_aServlets.clear ();
  }
}

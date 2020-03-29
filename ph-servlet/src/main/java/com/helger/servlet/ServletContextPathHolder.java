/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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
package com.helger.servlet;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.debug.GlobalDebug;

/**
 * Helper class to hold the current servlet context path. In certain cases it is
 * necessary to overwrite the context path (custom context path) if an
 * application is run behind a reverse proxy but needs to emit absolute URLs.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public final class ServletContextPathHolder
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ServletContextPathHolder.class);

  private static String s_sServletContextPath;
  private static String s_sCustomContextPath;
  private static final AtomicBoolean SILENT_MODE = new AtomicBoolean (GlobalDebug.DEFAULT_SILENT_MODE);

  @PresentForCodeCoverage
  private static final ServletContextPathHolder s_aInstance = new ServletContextPathHolder ();

  private ServletContextPathHolder ()
  {}

  public static boolean isSilentMode ()
  {
    return SILENT_MODE.get ();
  }

  public static boolean setSilentMode (final boolean bSilentMode)
  {
    return SILENT_MODE.getAndSet (bSilentMode);
  }

  public static void setServletContextPath (@Nonnull final String sServletContextPath)
  {
    ValueEnforcer.notNull (sServletContextPath, "ServletContextPath");
    if (s_sServletContextPath == null)
    {
      if (!isSilentMode ())
        if (LOGGER.isInfoEnabled ())
          LOGGER.info ("Setting servlet context path to '" + sServletContextPath + "'!");
      s_sServletContextPath = sServletContextPath;
    }
    else
      if (!s_sServletContextPath.equals (sServletContextPath))
      {
        if (LOGGER.isErrorEnabled ())
          LOGGER.error ("Overwriting servlet context path '" +
                        s_sServletContextPath +
                        "' with '" +
                        sServletContextPath +
                        "'",
                        new IllegalStateException ("Just for tracking how this happens"));
        s_sServletContextPath = sServletContextPath;
      }
  }

  /**
   * @return <code>true</code> if a servlet context path was set.
   * @see #setServletContextPath(String)
   * @since 8.7.1
   */
  public static boolean hasServletContextPath ()
  {
    return s_sCustomContextPath != null;
  }

  /**
   * @return The default servlet context path. May be <code>null</code>.
   * @since 8.7.1
   * @see #setServletContextPath(String)
   * @see #getContextPath()
   */
  @Nullable
  public static String getServletContextPath ()
  {
    return s_sServletContextPath;
  }

  /**
   * Manually change the context path to be used. Normally there is no need to
   * call the method, because the context path is automatically determined from
   * the {@link ServletContext} or from the underlying request. This method is
   * only needed, if a web application is proxied by e.g. an Apache httpd and
   * the context path between httpd and Java web application server is
   * different!
   *
   * @param sCustomContextPath
   *        The context path of the web application, or "" for the default
   *        (root) context. May not be <code>null</code>.
   */
  public static void setCustomContextPath (@Nonnull final String sCustomContextPath)
  {
    ValueEnforcer.notNull (sCustomContextPath, "CustomContextPath");
    if (s_sCustomContextPath == null)
    {
      if (!isSilentMode ())
        if (LOGGER.isInfoEnabled ())
          LOGGER.info ("Setting custom servlet context path to '" + sCustomContextPath + "'!");
      s_sCustomContextPath = sCustomContextPath;
    }
    else
      if (!s_sCustomContextPath.equals (sCustomContextPath))
      {
        if (LOGGER.isErrorEnabled ())
          LOGGER.error ("Overwriting custom servlet context path '" +
                        s_sCustomContextPath +
                        "' with '" +
                        sCustomContextPath +
                        "'",
                        new IllegalStateException ("Just for tracking how this happens"));
        s_sCustomContextPath = sCustomContextPath;
      }
  }

  /**
   * @return <code>true</code> if a custom context path was set.
   * @see #setCustomContextPath(String)
   */
  public static boolean hasCustomContextPath ()
  {
    return s_sCustomContextPath != null;
  }

  /**
   * @return The custom context path. May be <code>null</code>.
   * @since 8.7.1
   * @see #setCustomContextPath(String)
   * @see #getContextPath()
   */
  @Nullable
  public static String getCustomContextPath ()
  {
    return s_sCustomContextPath;
  }

  /**
   * @return <code>true</code> if either custom context path or servlet context
   *         path are present.
   * @since 9.1.2
   */
  public static boolean hasContextPath ()
  {
    return hasCustomContextPath () || hasServletContextPath ();
  }

  /**
   * Returns the context path of the web application.
   * <p>
   * The context path is the portion of the request URI that is used to select
   * the context of the request. The context path always comes first in a
   * request URI. The path starts with a "/" character but does not end with a
   * "/" character. For servlets in the default (root) context, this method
   * returns "".
   * <p>
   * It is possible that a servlet container may match a context by more than
   * one context path. In such cases the context path will return the actual
   * context path used by the request and it may differ from the path returned
   * by this method. The context path returned by this method should be
   * considered as the prime or preferred context path of the application.
   *
   * @return The context path of the web application, or "" for the default
   *         (root) context or <code>null</code> if none of them is set.
   * @see #getCustomContextPath()
   * @see #getServletContextPath()
   * @since 9.1.0
   */
  @Nullable
  public static String getContextPathOrNull ()
  {
    String ret = s_sCustomContextPath;
    if (ret == null)
      ret = s_sServletContextPath;
    return ret;
  }

  /**
   * Returns the context path of the web application.
   * <p>
   * The context path is the portion of the request URI that is used to select
   * the context of the request. The context path always comes first in a
   * request URI. The path starts with a "/" character but does not end with a
   * "/" character. For servlets in the default (root) context, this method
   * returns "".
   * <p>
   * It is possible that a servlet container may match a context by more than
   * one context path. In such cases the context path will return the actual
   * context path used by the request and it may differ from the path returned
   * by this method. The context path returned by this method should be
   * considered as the prime or preferred context path of the application.
   *
   * @return The context path of the web application, or "" for the default
   *         (root) context
   * @throws IllegalStateException
   *         if neither a custom context path nor a servlet context path is set
   * @see #getCustomContextPath()
   * @see #getServletContextPath()
   */
  @Nonnull
  public static String getContextPath ()
  {
    final String ret = getContextPathOrNull ();
    if (ret == null)
      throw new IllegalStateException ("No servlet context path present!");
    return ret;
  }

  /**
   * Clears both servlet context and custom context path.
   */
  public static void clearContextPath ()
  {
    if (s_sServletContextPath != null)
    {
      if (!isSilentMode ())
        if (LOGGER.isInfoEnabled ())
          LOGGER.info ("The servlet context path '" + s_sServletContextPath + "' was cleared!");
      s_sServletContextPath = null;
    }
    if (s_sCustomContextPath != null)
    {
      if (!isSilentMode ())
        if (LOGGER.isInfoEnabled ())
          LOGGER.info ("The custom servlet context path '" + s_sCustomContextPath + "' was cleared!");
      s_sCustomContextPath = null;
    }
  }
}

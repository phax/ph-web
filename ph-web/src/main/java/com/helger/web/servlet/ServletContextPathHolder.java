package com.helger.web.servlet;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.PresentForCodeCoverage;

public final class ServletContextPathHolder
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (ServletContextPathHolder.class);

  private static String s_sServletContextPath;
  private static String s_sCustomContextPath;

  @PresentForCodeCoverage
  private static final ServletContextPathHolder s_aInstance = new ServletContextPathHolder ();

  private ServletContextPathHolder ()
  {}

  public static void setServletContextPath (@Nonnull final String sServletContextPath)
  {
    s_sServletContextPath = ValueEnforcer.notNull (sServletContextPath, "ServletContextPath");
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
    s_sCustomContextPath = ValueEnforcer.notNull (sCustomContextPath, "CustomContextPath");
    s_aLogger.info ("The context path was manually overridden to use '" + sCustomContextPath + "'!");
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
   */
  @Nonnull
  public static String getContextPath ()
  {
    String ret = s_sCustomContextPath;
    if (ret == null)
      ret = s_sServletContextPath;
    if (ret == null)
      throw new IllegalStateException ("No Context Path present!");
    return ret;
  }

  public static void clearContextPath ()
  {
    s_sServletContextPath = null;
    s_sCustomContextPath = null;
  }
}

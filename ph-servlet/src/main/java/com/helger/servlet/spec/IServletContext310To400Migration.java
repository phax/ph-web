/*
 * Copyright (C) 2014-2024 Philip Helger (www.helger.com)
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
package com.helger.servlet.spec;

import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRegistration.Dynamic;

/**
 * Dummy interface with all ServletContext default methods for new methods in
 * Servlet Spec 4.0.0 compared to 3.1.0
 *
 * @author Philip Helger
 */
public interface IServletContext310To400Migration extends IServletContext300To310Migration
{
  /**
   * Adds the servlet with the given jsp file to this servlet context.
   * <p>
   * The registered servlet may be further configured via the returned
   * {@link ServletRegistration} object.
   * <p>
   * If this ServletContext already contains a preliminary ServletRegistration
   * for a servlet with the given <code>servletName</code>, it will be completed
   * (by assigning the given <code>jspFile</code> to it) and returned.
   *
   * @param sServletName
   *        the name of the servlet
   * @param sJspFile
   *        the full path to a JSP file within the web application beginning
   *        with a `/'.
   * @return a ServletRegistration object that may be used to further configure
   *         the registered servlet, or <code>null</code> if this ServletContext
   *         already contains a complete ServletRegistration for a servlet with
   *         the given <code>servletName</code>
   * @throws IllegalStateException
   *         if this ServletContext has already been initialized
   * @throws IllegalArgumentException
   *         if <code>servletName</code> is null or an empty String
   * @throws UnsupportedOperationException
   *         if this ServletContext was passed to the
   *         {@link ServletContextListener#contextInitialized} method of a
   *         {@link ServletContextListener} that was neither declared in
   *         <code>web.xml</code> or <code>web-fragment.xml</code>, nor
   *         annotated with {@link jakarta.servlet.annotation.WebListener}
   * @since Servlet 4.0
   */
  default Dynamic addJspFile (final String sServletName, final String sJspFile)
  {
    throw new UnsupportedOperationException ();
  }

  /**
   * Gets the session timeout in minutes that are supported by default for this
   * <code>ServletContext</code>.
   *
   * @return the session timeout in minutes that are supported by default for
   *         this <code>ServletContext</code>
   * @throws UnsupportedOperationException
   *         if this ServletContext was passed to the
   *         {@link ServletContextListener#contextInitialized} method of a
   *         {@link ServletContextListener} that was neither declared in
   *         <code>web.xml</code> or <code>web-fragment.xml</code>, nor
   *         annotated with {@link jakarta.servlet.annotation.WebListener}
   * @since Servlet 4.0
   */
  default int getSessionTimeout ()
  {
    throw new UnsupportedOperationException ();
  }

  /**
   * Sets the session timeout in minutes for this ServletContext.
   *
   * @param nSessionTimeout
   *        session timeout in minutes
   * @throws IllegalStateException
   *         if this ServletContext has already been initialized
   * @throws UnsupportedOperationException
   *         if this ServletContext was passed to the
   *         {@link ServletContextListener#contextInitialized} method of a
   *         {@link ServletContextListener} that was neither declared in
   *         <code>web.xml</code> or <code>web-fragment.xml</code>, nor
   *         annotated with {@link jakarta.servlet.annotation.WebListener}
   * @since Servlet 4.0
   */
  default void setSessionTimeout (final int nSessionTimeout)
  {
    throw new UnsupportedOperationException ();
  }

  /**
   * Gets the request character encoding that are supported by default for this
   * <code>ServletContext</code>. This method returns null if no request
   * encoding character encoding has been specified in deployment descriptor or
   * container specific configuration (for all web applications in the
   * container).
   *
   * @return the request character encoding that are supported by default for
   *         this <code>ServletContext</code>
   * @throws UnsupportedOperationException
   *         if this ServletContext was passed to the
   *         {@link ServletContextListener#contextInitialized} method of a
   *         {@link ServletContextListener} that was neither declared in
   *         <code>web.xml</code> or <code>web-fragment.xml</code>, nor
   *         annotated with {@link jakarta.servlet.annotation.WebListener}
   * @since Servlet 4.0
   */
  default String getRequestCharacterEncoding ()
  {
    throw new UnsupportedOperationException ();
  }

  /**
   * Sets the request character encoding for this ServletContext.
   *
   * @param sEncoding
   *        request character encoding
   * @throws IllegalStateException
   *         if this ServletContext has already been initialized
   * @throws UnsupportedOperationException
   *         if this ServletContext was passed to the
   *         {@link ServletContextListener#contextInitialized} method of a
   *         {@link ServletContextListener} that was neither declared in
   *         <code>web.xml</code> or <code>web-fragment.xml</code>, nor
   *         annotated with {@link jakarta.servlet.annotation.WebListener}
   * @since Servlet 4.0
   */
  default void setRequestCharacterEncoding (final String sEncoding)
  {
    throw new UnsupportedOperationException ();
  }

  /**
   * Gets the response character encoding that are supported by default for this
   * <code>ServletContext</code>. This method returns null if no response
   * encoding character encoding has been specified in deployment descriptor or
   * container specific configuration (for all web applications in the
   * container).
   *
   * @return the request character encoding that are supported by default for
   *         this <code>ServletContext</code>
   * @throws UnsupportedOperationException
   *         if this ServletContext was passed to the
   *         {@link ServletContextListener#contextInitialized} method of a
   *         {@link ServletContextListener} that was neither declared in
   *         <code>web.xml</code> or <code>web-fragment.xml</code>, nor
   *         annotated with {@link jakarta.servlet.annotation.WebListener}
   * @since Servlet 4.0
   */
  default String getResponseCharacterEncoding ()
  {
    throw new UnsupportedOperationException ();
  }

  /**
   * Sets the response character encoding for this ServletContext.
   *
   * @param sEncoding
   *        response character encoding
   * @throws IllegalStateException
   *         if this ServletContext has already been initialized
   * @throws UnsupportedOperationException
   *         if this ServletContext was passed to the
   *         {@link ServletContextListener#contextInitialized} method of a
   *         {@link ServletContextListener} that was neither declared in
   *         <code>web.xml</code> or <code>web-fragment.xml</code>, nor
   *         annotated with {@link jakarta.servlet.annotation.WebListener}
   * @since Servlet 4.0
   */
  default void setResponseCharacterEncoding (final String sEncoding)
  {
    throw new UnsupportedOperationException ();
  }
}

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
package com.helger.http.servlet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.ServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.PresentForCodeCoverage;

/**
 * Very basic servlet API helper
 *
 * @author Philip Helger
 * @since 8.6.3
 */
@Immutable
public final class ServletHelper
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (ServletHelper.class);

  @PresentForCodeCoverage
  private static final ServletHelper s_aInstance = new ServletHelper ();

  private ServletHelper ()
  {}

  /**
   * Safe version of <code>ServletRequest.setAttribute (String, Object)</code>
   * to work around an error in certain Tomcat versions.
   *
   * @param aRequest
   *        Servlet request. May not be <code>null</code>.
   * @param sAttrName
   *        Attribute name. May not be <code>null</code>.
   * @param aAttrValue
   *        Attribute value. May be <code>null</code>.
   */
  public static void setRequestAttribute (@Nonnull final ServletRequest aRequest,
                                          @Nonnull final String sAttrName,
                                          @Nullable final Object aAttrValue)
  {
    try
    {
      aRequest.setAttribute (sAttrName, aAttrValue);
    }
    catch (final Throwable t)
    {
      // Happens in certain Tomcat versions (e.g. 7.0.42 with JDK 8):
      /**
       * <pre>
      java.lang.NullPointerException
      1.: org.apache.catalina.connector.Request.notifyAttributeAssigned(Request.java:1493)
      2.: org.apache.catalina.connector.Request.setAttribute(Request.java:1483)
      3.: org.apache.catalina.connector.RequestFacade.setAttribute(RequestFacade.java:539)
       * </pre>
       */
      s_aLogger.warn ("Failed to set attribute '" + sAttrName + "' in HTTP request", t);
    }
  }

}

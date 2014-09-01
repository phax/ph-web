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
package com.helger.web.fileupload.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import com.helger.web.fileupload.IRequestContext;
import com.helger.web.servlet.request.RequestHelper;

/**
 * <p>
 * Provides access to the request information needed for a request made to an
 * HTTP servlet.
 * </p>
 * 
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @since FileUpload 1.1
 * @version $Id: ServletRequestContext.java 479262 2006-11-26 03:09:24Z niallp $
 */
public final class ServletRequestContext implements IRequestContext
{
  /**
   * The request for which the context is being provided.
   */
  private final HttpServletRequest m_aHttpRequest;

  /**
   * Construct a context for this request.
   * 
   * @param request
   *        The request to which this context applies.
   */
  public ServletRequestContext (@Nonnull final HttpServletRequest request)
  {
    m_aHttpRequest = request;
  }

  /**
   * Retrieve the character encoding for the request.
   * 
   * @return The character encoding for the request.
   */
  public String getCharacterEncoding ()
  {
    return m_aHttpRequest.getCharacterEncoding ();
  }

  /**
   * Retrieve the content type of the request.
   * 
   * @return The content type of the request.
   */
  public String getContentType ()
  {
    return m_aHttpRequest.getContentType ();
  }

  /**
   * Retrieve the content length of the request.
   * 
   * @return The content length of the request.
   */
  public long getContentLength ()
  {
    return RequestHelper.getContentLength (m_aHttpRequest);
  }

  /**
   * Retrieve the input stream for the request.
   * 
   * @return The input stream for the request.
   * @throws IOException
   *         if a problem occurs.
   */
  public InputStream getInputStream () throws IOException
  {
    return m_aHttpRequest.getInputStream ();
  }

  /**
   * Returns a string representation of this object.
   * 
   * @return a string representation of this object.
   */
  @Override
  public String toString ()
  {
    return "ContentLength=" + getContentLength () + ", ContentType=" + getContentType ();
  }
}

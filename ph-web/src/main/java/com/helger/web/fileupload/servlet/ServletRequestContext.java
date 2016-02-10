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
package com.helger.web.fileupload.servlet;

import java.io.IOException;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;
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
public class ServletRequestContext implements IRequestContext
{
  /**
   * The request for which the context is being provided.
   */
  private final HttpServletRequest m_aHttpRequest;

  /**
   * Construct a context for this request.
   *
   * @param aHttpRequest
   *        The request to which this context applies. May not be
   *        <code>null</code>.
   */
  public ServletRequestContext (@Nonnull final HttpServletRequest aHttpRequest)
  {
    m_aHttpRequest = ValueEnforcer.notNull (aHttpRequest, "HttpRequest");
  }

  @Nullable
  public String getCharacterEncoding ()
  {
    return m_aHttpRequest.getCharacterEncoding ();
  }

  @Nullable
  public String getContentType ()
  {
    return m_aHttpRequest.getContentType ();
  }

  @CheckForSigned
  public long getContentLength ()
  {
    return RequestHelper.getContentLength (m_aHttpRequest);
  }

  @Nonnull
  public ServletInputStream getInputStream () throws IOException
  {
    return m_aHttpRequest.getInputStream ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("contentLength", getContentLength ())
                                       .append ("contentType", getContentType ())
                                       .toString ();
  }
}

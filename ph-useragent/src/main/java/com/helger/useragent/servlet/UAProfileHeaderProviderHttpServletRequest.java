/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.useragent.servlet;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.collection.commons.ICommonsList;
import com.helger.servlet.ServletHelper;
import com.helger.useragent.uaprofile.IUAProfileHeaderProvider;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Special implementation of {@link IUAProfileHeaderProvider} for
 * {@link HttpServletRequest}.
 *
 * @author Philip Helger
 * @since 10.3.0
 */
public class UAProfileHeaderProviderHttpServletRequest implements IUAProfileHeaderProvider
{
  private final HttpServletRequest m_aHttpRequest;

  public UAProfileHeaderProviderHttpServletRequest (@NonNull final HttpServletRequest aHttpRequest)
  {
    m_aHttpRequest = aHttpRequest;
  }

  @NonNull
  @ReturnsMutableCopy
  public ICommonsList <String> getAllHeaderNames ()
  {
    return ServletHelper.getRequestHeaderNames (m_aHttpRequest);
  }

  @NonNull
  @ReturnsMutableCopy
  public ICommonsList <String> getHeaders (@Nullable final String sName)
  {
    return ServletHelper.getRequestHeaders (m_aHttpRequest, sName);
  }

  @Nullable
  public String getHeaderValue (@Nullable final String sHeader)
  {
    return ServletHelper.getRequestHeader (m_aHttpRequest, sHeader);
  }
}

/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.scopes.factory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.helger.commons.annotation.Nonempty;
import com.helger.web.scopes.domain.IApplicationWebScope;
import com.helger.web.scopes.domain.IGlobalWebScope;
import com.helger.web.scopes.domain.IRequestWebScope;
import com.helger.web.scopes.domain.ISessionApplicationWebScope;
import com.helger.web.scopes.domain.ISessionWebScope;
import com.helger.web.scopes.impl.ApplicationWebScope;
import com.helger.web.scopes.impl.GlobalWebScope;
import com.helger.web.scopes.impl.RequestWebScope;
import com.helger.web.scopes.impl.SessionApplicationWebScope;
import com.helger.web.scopes.impl.SessionWebScope;

/**
 * Web version of the scope factory.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class DefaultWebScopeFactory implements IWebScopeFactory
{
  public DefaultWebScopeFactory ()
  {}

  @Nonnull
  public IGlobalWebScope createGlobalScope (@Nonnull final ServletContext aServletContext)
  {
    return new GlobalWebScope (aServletContext);
  }

  @Nonnull
  public IApplicationWebScope createApplicationScope (@Nonnull @Nonempty final String sScopeID)
  {
    return new ApplicationWebScope (sScopeID);
  }

  @Nonnull
  public ISessionWebScope createSessionScope (@Nonnull final HttpSession aHttpSession)
  {
    return new SessionWebScope (aHttpSession);
  }

  @Nonnull
  public ISessionApplicationWebScope createSessionApplicationScope (@Nonnull @Nonempty final String sScopeID)
  {
    return new SessionApplicationWebScope (sScopeID);
  }

  @Nonnull
  public IRequestWebScope createRequestScope (@Nonnull final HttpServletRequest aHttpRequest,
                                              @Nonnull final HttpServletResponse aHttpResponse)
  {
    return new RequestWebScope (aHttpRequest, aHttpResponse);
  }
}

/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.web.servlets.scope;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.state.EContinue;
import com.helger.web.scope.IRequestWebScope;

/**
 * A simple Servlet filter that surrounds each and every call with the necessary
 * scope begin and end calls. This does make sense as an easy way to ensure that
 * every call is scoped.
 *
 * @author Philip Helger
 */
public class DefaultScopeAwareFilter extends AbstractScopeAwareFilter
{
  @Override
  @Nonnull
  protected EContinue doHttpFilter (@Nonnull final HttpServletRequest aHttpRequest,
                                    @Nonnull final HttpServletResponse aHttpResponse,
                                    @Nonnull final IRequestWebScope aRequestScope)
  {
    // No filtering
    return EContinue.CONTINUE;
  }
}

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
package com.helger.web.scope.util;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.callback.INonThrowingCallable;
import com.helger.servlet.mock.MockHttpServletResponse;
import com.helger.servlet.mock.OfflineHttpServletRequest;
import com.helger.web.scope.mgr.WebScopeManager;

/**
 * Abstract implementation of {@link Callable} that handles WebScopes correctly.
 *
 * @author Philip Helger
 * @param <DATATYPE>
 *        The return type of the function.
 */
@Deprecated
public abstract class AbstractWebScopeAwareCallable <DATATYPE> extends AbstractWebScopeAwareAction
                                                    implements INonThrowingCallable <DATATYPE>
{
  public AbstractWebScopeAwareCallable ()
  {
    this (WebScopeManager.getGlobalScope ().getServletContext (), WebScopeManager.getApplicationScope ().getID ());
  }

  public AbstractWebScopeAwareCallable (@Nonnull final ServletContext aSC,
                                        @Nonnull @Nonempty final String sApplicationID)
  {
    super (aSC, sApplicationID);
  }

  /**
   * Implement your code in here
   *
   * @return The return value of the {@link #call()} method.
   */
  @Nullable
  protected abstract DATATYPE scopedRun ();

  @Nullable
  public final DATATYPE call ()
  {
    WebScopeManager.onRequestBegin (m_sApplicationID,
                                    new OfflineHttpServletRequest (m_aSC, false),
                                    new MockHttpServletResponse ());
    try
    {
      final DATATYPE ret = scopedRun ();
      return ret;
    }
    finally
    {
      WebScopeManager.onRequestEnd ();
    }
  }
}

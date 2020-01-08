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
package com.helger.web.scope.mock;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;

import com.helger.servlet.mock.MockServletRequestListener;
import com.helger.web.scope.mgr.WebScopeManager;

/**
 * This mock listeners is responsible for creating
 *
 * @author Philip Helger
 */
@ThreadSafe
public class MockServletRequestListenerScopeAware extends MockServletRequestListener
{
  public MockServletRequestListenerScopeAware ()
  {}

  @Override
  public void requestInitialized (@Nonnull final ServletRequestEvent aEvent)
  {
    super.requestInitialized (aEvent);
    WebScopeManager.onRequestBegin ((HttpServletRequest) aEvent.getServletRequest (), getCurrentMockResponse ());
  }

  @Override
  public void requestDestroyed (@Nonnull final ServletRequestEvent aEvent)
  {
    WebScopeManager.onRequestEnd ();
    super.requestDestroyed (aEvent);
  }
}

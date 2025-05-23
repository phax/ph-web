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
package com.helger.servlet.mock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

/**
 * This mock listeners is responsible for creating
 *
 * @author Philip Helger
 */
@ThreadSafe
public class MockServletRequestListener implements ServletRequestListener
{
  private MockHttpServletResponse m_aResp;

  public MockServletRequestListener ()
  {}

  public void requestInitialized (@Nonnull final ServletRequestEvent aEvent)
  {
    m_aResp = new MockHttpServletResponse ();
  }

  @Nullable
  public MockHttpServletResponse getCurrentMockResponse ()
  {
    return m_aResp;
  }

  public void requestDestroyed (@Nonnull final ServletRequestEvent aEvent)
  {
    m_aResp = null;
  }
}

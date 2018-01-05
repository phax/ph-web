/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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

import java.util.EventListener;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionListener;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.ICommonsList;

/**
 * This class globally holds the HTTP listeners ({@link ServletContextListener}
 * , {@link HttpSessionListener} and {@link ServletRequestListener}) that are
 * triggered in tests.
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class MockHttpListener
{
  private static MockEventListenerList s_aDefaultListener = new MockEventListenerList ();
  private static MockEventListenerList s_aCurrentListener = new MockEventListenerList ();

  private MockHttpListener ()
  {}

  public static void addDefaultListener (@Nonnull final EventListener aListener)
  {
    s_aDefaultListener.addListener (aListener);
  }

  public static void removeDefaultListeners (@Nonnull final Class <? extends EventListener> aListenerClass)
  {
    s_aDefaultListener.removeListeners (aListenerClass);
  }

  public static void removeAllDefaultListeners ()
  {
    s_aDefaultListener.removeAllListeners ();
  }

  public static void setCurrentToDefault ()
  {
    s_aCurrentListener.setFrom (s_aDefaultListener);
  }

  public static void addListener (@Nonnull final EventListener aListener)
  {
    s_aCurrentListener.addListener (aListener);
  }

  public static void removeListeners (@Nonnull final Class <? extends EventListener> aListenerClass)
  {
    s_aCurrentListener.removeListeners (aListenerClass);
  }

  public static void removeAllListeners ()
  {
    s_aCurrentListener.removeAllListeners ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <ServletContextListener> getAllServletContextListeners ()
  {
    return s_aCurrentListener.getAllServletContextListeners ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <HttpSessionListener> getAllHttpSessionListeners ()
  {
    return s_aCurrentListener.getAllHttpSessionListeners ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <ServletRequestListener> getAllServletRequestListeners ()
  {
    return s_aCurrentListener.getAllServletRequestListeners ();
  }
}

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

import java.util.EventListener;

import com.helger.annotation.concurrent.ThreadSafe;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.collection.commons.ICommonsList;

import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpSessionListener;

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
  private static final MockEventListenerList DEFAULT_LISTENER = new MockEventListenerList ();
  private static final MockEventListenerList CURRENT_LISTENER = new MockEventListenerList ();

  private MockHttpListener ()
  {}

  public static void addDefaultListener (@Nonnull final EventListener aListener)
  {
    DEFAULT_LISTENER.addListener (aListener);
  }

  public static void removeDefaultListeners (@Nonnull final Class <? extends EventListener> aListenerClass)
  {
    DEFAULT_LISTENER.removeListeners (aListenerClass);
  }

  public static void removeAllDefaultListeners ()
  {
    DEFAULT_LISTENER.removeAllListeners ();
  }

  public static void setCurrentToDefault ()
  {
    CURRENT_LISTENER.setFrom (DEFAULT_LISTENER);
  }

  public static void addListener (@Nonnull final EventListener aListener)
  {
    CURRENT_LISTENER.addListener (aListener);
  }

  public static void removeListeners (@Nonnull final Class <? extends EventListener> aListenerClass)
  {
    CURRENT_LISTENER.removeListeners (aListenerClass);
  }

  public static void removeAllListeners ()
  {
    CURRENT_LISTENER.removeAllListeners ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <ServletContextListener> getAllServletContextListeners ()
  {
    return CURRENT_LISTENER.getAllServletContextListeners ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <HttpSessionListener> getAllHttpSessionListeners ()
  {
    return CURRENT_LISTENER.getAllHttpSessionListeners ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <ServletRequestListener> getAllServletRequestListeners ()
  {
    return CURRENT_LISTENER.getAllServletRequestListeners ();
  }
}

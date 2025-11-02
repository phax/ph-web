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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.GuardedBy;
import com.helger.annotation.concurrent.ThreadSafe;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.concurrent.SimpleReadWriteLock;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.state.EChange;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;

import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpSessionListener;

/**
 * This class holds the different listeners ({@link ServletContextListener},
 * {@link HttpSessionListener} and {@link ServletRequestListener}) used by
 * {@link MockHttpListener}
 *
 * @author Philip Helger
 */
@ThreadSafe
public class MockEventListenerList
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MockEventListenerList.class);

  private final SimpleReadWriteLock m_aRWLock = new SimpleReadWriteLock ();
  @GuardedBy ("m_aRWLock")
  private final ICommonsList <EventListener> m_aListener = new CommonsArrayList <> ();

  public MockEventListenerList ()
  {}

  /**
   * Set all listeners from the passed list to this list
   *
   * @param aList
   *        The other list. May not be <code>null</code>.
   * @return {@link EChange}
   */
  @NonNull
  public EChange setFrom (@NonNull final MockEventListenerList aList)
  {
    ValueEnforcer.notNull (aList, "List");

    // Assigning this to this?
    if (this == aList)
      return EChange.UNCHANGED;

    // Get all listeners to assign
    final ICommonsList <EventListener> aOtherListeners = aList.getAllListeners ();

    return m_aRWLock.writeLockedGet ( () -> {
      if (m_aListener.isEmpty () && aOtherListeners.isEmpty ())
        return EChange.UNCHANGED;

      m_aListener.setAll (aOtherListeners);
      return EChange.CHANGED;
    });
  }

  /**
   * Add a new listener.
   *
   * @param aListener
   *        The listener to be added. May not be <code>null</code>.
   * @return {@link EChange}.
   */
  @NonNull
  public EChange addListener (@NonNull final EventListener aListener)
  {
    ValueEnforcer.notNull (aListener, "Listener");

    // Small consistency check
    if (!(aListener instanceof ServletContextListener) &&
        !(aListener instanceof HttpSessionListener) &&
        !(aListener instanceof ServletRequestListener))
    {
      LOGGER.warn ("Passed mock listener is none of ServletContextListener, HttpSessionListener or ServletRequestListener and therefore has no effect. The listener class is: " +
                   aListener.getClass ());
    }

    return EChange.valueOf (m_aRWLock.writeLockedBoolean ( () -> m_aListener.add (aListener)));
  }

  @NonNull
  public EChange removeListeners (@Nullable final Class <? extends EventListener> aListenerClass)
  {
    if (aListenerClass == null)
      return EChange.UNCHANGED;

    return m_aRWLock.writeLockedGet ( () -> {
      EChange ret = EChange.UNCHANGED;
      // Create a copy of the list
      for (final EventListener aListener : m_aListener.getClone ())
        if (aListener.getClass ().equals (aListenerClass))
          ret = ret.or (EChange.valueOf (m_aListener.remove (aListener)));
      return ret;
    });
  }

  @NonNull
  public EChange removeAllListeners ()
  {
    return m_aRWLock.writeLockedGet (m_aListener::removeAll);
  }

  @NonNull
  @ReturnsMutableCopy
  public ICommonsList <EventListener> getAllListeners ()
  {
    return m_aRWLock.readLockedGet (m_aListener::getClone);
  }

  @NonNull
  @ReturnsMutableCopy
  public ICommonsList <ServletContextListener> getAllServletContextListeners ()
  {
    return m_aRWLock.readLockedGet ( () -> m_aListener.getAllInstanceOf (ServletContextListener.class));
  }

  @NonNull
  @ReturnsMutableCopy
  public ICommonsList <HttpSessionListener> getAllHttpSessionListeners ()
  {
    return m_aRWLock.readLockedGet ( () -> m_aListener.getAllInstanceOf (HttpSessionListener.class));
  }

  @NonNull
  @ReturnsMutableCopy
  public ICommonsList <ServletRequestListener> getAllServletRequestListeners ()
  {
    return m_aRWLock.readLockedGet ( () -> m_aListener.getAllInstanceOf (ServletRequestListener.class));
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Listeners", m_aListener).getToString ();
  }
}

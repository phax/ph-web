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
package com.helger.web.mock;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.collections.CollectionHelper;
import com.helger.commons.state.EChange;
import com.helger.commons.string.ToStringGenerator;

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
  private static final Logger s_aLogger = LoggerFactory.getLogger (MockEventListenerList.class);

  private final ReadWriteLock m_aRWLock = new ReentrantReadWriteLock ();
  private final List <EventListener> m_aListener = new ArrayList <EventListener> ();

  public MockEventListenerList ()
  {}

  /**
   * Set all listeners from the passed list to this list
   *
   * @param aList
   *        The other list. May not be <code>null</code>.
   * @return {@link EChange}
   */
  @Nonnull
  public EChange setFrom (@Nonnull final MockEventListenerList aList)
  {
    ValueEnforcer.notNull (aList, "List");

    // Assigning this to this?
    if (this == aList)
      return EChange.UNCHANGED;

    // Get all listeners to assign
    final List <EventListener> aOtherListeners = aList.getAllListeners ();

    m_aRWLock.writeLock ().lock ();
    try
    {
      if (m_aListener.isEmpty () && aOtherListeners.isEmpty ())
        return EChange.UNCHANGED;

      m_aListener.clear ();
      m_aListener.addAll (aOtherListeners);
      return EChange.CHANGED;
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * Add a new listener.
   *
   * @param aListener
   *        The listener to be added. May not be <code>null</code>.
   * @return {@link EChange}.
   */
  @Nonnull
  public EChange addListener (@Nonnull final EventListener aListener)
  {
    ValueEnforcer.notNull (aListener, "Listener");

    // Small consistency check
    if (!(aListener instanceof ServletContextListener) &&
        !(aListener instanceof HttpSessionListener) &&
        !(aListener instanceof ServletRequestListener))
    {
      s_aLogger.warn ("Passed mock listener is none of ServletContextListener, HttpSessionListener or ServletRequestListener and therefore has no effect. The listener class is: " +
                      aListener.getClass ());
    }

    m_aRWLock.writeLock ().lock ();
    try
    {
      return EChange.valueOf (m_aListener.add (aListener));
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
  }

  @Nonnull
  public EChange removeListeners (@Nullable final Class <? extends EventListener> aListenerClass)
  {
    EChange ret = EChange.UNCHANGED;
    if (aListenerClass != null)
    {
      m_aRWLock.writeLock ().lock ();
      try
      {
        // Create a copy of the list
        for (final EventListener aListener : CollectionHelper.newList (m_aListener))
          if (aListener.getClass ().equals (aListenerClass))
            ret = ret.or (EChange.valueOf (m_aListener.remove (aListener)));
      }
      finally
      {
        m_aRWLock.writeLock ().unlock ();
      }
    }
    return ret;
  }

  @Nonnull
  public EChange removeAllListeners ()
  {
    m_aRWLock.writeLock ().lock ();
    try
    {
      if (m_aListener.isEmpty ())
        return EChange.UNCHANGED;
      m_aListener.clear ();
      return EChange.CHANGED;
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <EventListener> getAllListeners ()
  {
    m_aRWLock.readLock ().lock ();
    try
    {
      return CollectionHelper.newList (m_aListener);
    }
    finally
    {
      m_aRWLock.readLock ().unlock ();
    }
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <ServletContextListener> getAllServletContextListeners ()
  {
    final List <ServletContextListener> ret = new ArrayList <ServletContextListener> ();
    for (final EventListener aListener : getAllListeners ())
      if (aListener instanceof ServletContextListener)
        ret.add ((ServletContextListener) aListener);
    return ret;
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <HttpSessionListener> getAllHttpSessionListeners ()
  {
    final List <HttpSessionListener> ret = new ArrayList <HttpSessionListener> ();
    for (final EventListener aListener : getAllListeners ())
      if (aListener instanceof HttpSessionListener)
        ret.add ((HttpSessionListener) aListener);
    return ret;
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <ServletRequestListener> getAllServletRequestListeners ()
  {
    final List <ServletRequestListener> ret = new ArrayList <ServletRequestListener> ();
    for (final EventListener aListener : getAllListeners ())
      if (aListener instanceof ServletRequestListener)
        ret.add ((ServletRequestListener) aListener);
    return ret;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("listeners", m_aListener).toString ();
  }
}

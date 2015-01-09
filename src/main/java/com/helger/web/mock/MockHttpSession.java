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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.Nonempty;
import com.helger.commons.annotations.UnsupportedOperation;
import com.helger.commons.collections.ArrayHelper;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.idfactory.GlobalIDFactory;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;

// ESCA-JAVA0116:
/**
 * Mock implementation of {@link HttpSession}.
 * 
 * @author Philip Helger
 */
@SuppressWarnings ("deprecation")
@NotThreadSafe
public class MockHttpSession implements HttpSession
{
  public static final String SESSION_COOKIE_NAME = "JSESSIONID";

  private final String m_sID;
  private final long m_nCreationTime = System.currentTimeMillis ();
  private int m_nMaxInactiveInterval = -1; // indefinite
  private long m_nLastAccessedTime = System.currentTimeMillis ();
  private final ServletContext m_aServletContext;
  private final Map <String, Object> m_aAttributes = new HashMap <String, Object> ();
  private boolean m_bInvalidated = false;
  private boolean m_bIsNew = true;

  /**
   * Create a new MockHttpSession with a default {@link MockServletContext}.
   * 
   * @see MockServletContext
   */
  public MockHttpSession ()
  {
    this (null);
  }

  /**
   * Create a new MockHttpSession.
   * 
   * @param aServletContext
   *        the ServletContext that the session runs in
   */
  public MockHttpSession (@Nullable final ServletContext aServletContext)
  {
    this (aServletContext, null);
  }

  /**
   * Create a new MockHttpSession.
   * 
   * @param aServletContext
   *        the ServletContext that the session runs in
   * @param sID
   *        a unique identifier for this session
   */
  public MockHttpSession (@Nullable final ServletContext aServletContext, @Nullable final String sID)
  {
    m_aServletContext = aServletContext;
    m_sID = StringHelper.hasText (sID) ? sID : GlobalIDFactory.getNewStringID ();

    final HttpSessionEvent aHSE = new HttpSessionEvent (this);
    for (final HttpSessionListener aListener : MockHttpListener.getAllHttpSessionListeners ())
      aListener.sessionCreated (aHSE);
  }

  public long getCreationTime ()
  {
    return m_nCreationTime;
  }

  @Nonnull
  @Nonempty
  public String getId ()
  {
    return m_sID;
  }

  public void doAccess ()
  {
    m_nLastAccessedTime = System.currentTimeMillis ();
    m_bIsNew = false;
  }

  public long getLastAccessedTime ()
  {
    return m_nLastAccessedTime;
  }

  @Nonnull
  public ServletContext getServletContext ()
  {
    if (m_aServletContext == null)
      throw new IllegalStateException ("No servlet context present!");
    return m_aServletContext;
  }

  public void setMaxInactiveInterval (final int nInterval)
  {
    m_nMaxInactiveInterval = nInterval;
  }

  public int getMaxInactiveInterval ()
  {
    return m_nMaxInactiveInterval;
  }

  @Deprecated
  @UnsupportedOperation
  public HttpSessionContext getSessionContext ()
  {
    throw new UnsupportedOperationException ("getSessionContext");
  }

  @Nullable
  public Object getAttribute (@Nonnull final String sName)
  {
    ValueEnforcer.notNull (sName, "Name");
    return m_aAttributes.get (sName);
  }

  @Deprecated
  public Object getValue (@Nonnull final String sName)
  {
    return getAttribute (sName);
  }

  @Nonnull
  public Enumeration <String> getAttributeNames ()
  {
    return ContainerHelper.getEnumeration (m_aAttributes.keySet ());
  }

  @Deprecated
  @Nonnull
  public String [] getValueNames ()
  {
    return ArrayHelper.newArray (m_aAttributes.keySet (), String.class);
  }

  public void setAttribute (@Nonnull final String sName, @Nullable final Object aValue)
  {
    ValueEnforcer.notNull (sName, "Name");

    if (aValue != null)
    {
      m_aAttributes.put (sName, aValue);
      if (aValue instanceof HttpSessionBindingListener)
        ((HttpSessionBindingListener) aValue).valueBound (new HttpSessionBindingEvent (this, sName, aValue));
    }
    else
    {
      removeAttribute (sName);
    }
  }

  @Deprecated
  public void putValue (@Nonnull final String sName, @Nullable final Object aValue)
  {
    setAttribute (sName, aValue);
  }

  public void removeAttribute (@Nonnull final String sName)
  {
    ValueEnforcer.notNull (sName, "Name");

    final Object aValue = m_aAttributes.remove (sName);
    if (aValue instanceof HttpSessionBindingListener)
      ((HttpSessionBindingListener) aValue).valueUnbound (new HttpSessionBindingEvent (this, sName, aValue));
  }

  @Deprecated
  public void removeValue (@Nonnull final String sName)
  {
    removeAttribute (sName);
  }

  /**
   * Clear all of this session's attributes.
   */
  public void clearAttributes ()
  {
    for (final Map.Entry <String, Object> entry : m_aAttributes.entrySet ())
    {
      final String sName = entry.getKey ();
      final Object aValue = entry.getValue ();
      if (aValue instanceof HttpSessionBindingListener)
        ((HttpSessionBindingListener) aValue).valueUnbound (new HttpSessionBindingEvent (this, sName, aValue));
    }
    m_aAttributes.clear ();
  }

  public void invalidate ()
  {
    if (m_bInvalidated)
      throw new IllegalStateException ("Session scope '" + getId () + "' already invalidated!");
    m_bInvalidated = true;

    final HttpSessionEvent aHSE = new HttpSessionEvent (this);
    for (final HttpSessionListener aListener : MockHttpListener.getAllHttpSessionListeners ())
      aListener.sessionDestroyed (aHSE);

    clearAttributes ();
  }

  public boolean isInvalid ()
  {
    return m_bInvalidated;
  }

  public void setNew (final boolean bIsNew)
  {
    m_bIsNew = bIsNew;
  }

  public boolean isNew ()
  {
    return m_bIsNew;
  }

  /**
   * Serialize the attributes of this session into an object that can be turned
   * into a byte array with standard Java serialization.
   * 
   * @return a representation of this session's serialized state
   */
  @Nonnull
  public Serializable serializeState ()
  {
    final HashMap <String, Object> aState = new HashMap <String, Object> ();
    for (final Map.Entry <String, Object> entry : m_aAttributes.entrySet ())
    {
      final String sName = entry.getKey ();
      final Object aValue = entry.getValue ();
      if (aValue instanceof Serializable)
      {
        aState.put (sName, aValue);
      }
      else
      {
        // Not serializable... Servlet containers usually automatically
        // unbind the attribute in this case.
        if (aValue instanceof HttpSessionBindingListener)
        {
          ((HttpSessionBindingListener) aValue).valueUnbound (new HttpSessionBindingEvent (this, sName, aValue));
        }
      }
    }
    m_aAttributes.clear ();
    return aState;
  }

  /**
   * Deserialize the attributes of this session from a state object created by
   * {@link #serializeState()}.
   * 
   * @param aState
   *        a representation of this session's serialized state
   */
  @SuppressWarnings ("unchecked")
  public void deserializeState (@Nonnull final Serializable aState)
  {
    if (!(aState instanceof Map))
      throw new IllegalArgumentException ("Serialized state needs to be of type [java.util.Map]");
    m_aAttributes.putAll ((Map <String, Object>) aState);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ID", m_sID)
                                       .append ("creationTime", m_nCreationTime)
                                       .append ("maxInactiveInterval", m_nMaxInactiveInterval)
                                       .append ("lastAccessedTime", m_nLastAccessedTime)
                                       .appendIfNotNull ("servletContext",
                                                         m_aServletContext == null ? null
                                                                                  : m_aServletContext.getServerInfo ())
                                       .append ("attributes", m_aAttributes)
                                       .append ("isInvalidated", m_bInvalidated)
                                       .append ("isNew", m_bIsNew)
                                       .toString ();
  }
}

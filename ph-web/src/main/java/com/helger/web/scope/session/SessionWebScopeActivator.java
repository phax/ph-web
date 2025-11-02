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
package com.helger.web.scope.session;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.misc.DevelopersNote;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.lang.clazz.ClassHelper;
import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsMap;
import com.helger.scope.ScopeHelper;
import com.helger.web.scope.ISessionWebScope;
import com.helger.web.scope.mgr.WebScopeManager;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionActivationListener;
import jakarta.servlet.http.HttpSessionEvent;

/**
 * This class is responsible for passivating and activating session web scopes.
 * Important: this object itself may NOT be passivated!
 *
 * @author Philip Helger
 */
public final class SessionWebScopeActivator implements
                                            HttpSessionActivationListener,
                                            ISessionWebScopeDontPassivate,
                                            Serializable
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SessionWebScopeActivator.class);

  private ISessionWebScope m_aSessionWebScope;
  private ICommonsMap <String, Object> m_aAttrs;

  @Deprecated (forRemoval = false)
  @DevelopersNote ("For reading only")
  public SessionWebScopeActivator ()
  {}

  /**
   * Constructor for writing
   *
   * @param aSessionWebScope
   *        the scope to be written
   */
  public SessionWebScopeActivator (@NonNull final ISessionWebScope aSessionWebScope)
  {
    m_aSessionWebScope = ValueEnforcer.notNull (aSessionWebScope, "SessionWebScope");
  }

  private void writeObject (@NonNull final ObjectOutputStream out) throws IOException
  {
    if (m_aSessionWebScope == null)
      throw new IllegalStateException ("No SessionWebScope is present!");
    {
      // Determine all attributes to be passivated
      final ICommonsMap <String, Object> aRelevantObjects = new CommonsHashMap <> ();
      for (final Map.Entry <String, Object> aEntry : m_aSessionWebScope.attrs ().entrySet ())
      {
        final Object aValue = aEntry.getValue ();
        if (!(aValue instanceof ISessionWebScopeDontPassivate))
          aRelevantObjects.put (aEntry.getKey (), aValue);
      }
      out.writeObject (aRelevantObjects);
    }
    if (ScopeHelper.isDebugSessionScopeLifeCycle ())
      LOGGER.info ("Wrote info on session web scope '" +
                   m_aSessionWebScope.getID () +
                   "' of class " +
                   ClassHelper.getClassLocalName (this),
                   ScopeHelper.getDebugException ());
  }

  @SuppressWarnings ("unchecked")
  private void readObject (@NonNull final ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    if (m_aSessionWebScope != null)
      throw new IllegalStateException ("Another SessionWebScope is already present: " + m_aSessionWebScope.toString ());

    // Read session attributes
    m_aAttrs = (ICommonsMap <String, Object>) in.readObject ();

    if (ScopeHelper.isDebugSessionScopeLifeCycle ())
      LOGGER.info ("Read info on session scope: " +
                   m_aAttrs.size () +
                   " attrs of class " +
                   ClassHelper.getClassLocalName (this),
                   ScopeHelper.getDebugException ());
  }

  public void sessionWillPassivate (@NonNull final HttpSessionEvent aEvent)
  {
    // Writing is all handled in the writeObject method

    // Invoke callbacks on all attributes
    if (m_aSessionWebScope != null)
    {
      for (final Object aValue : m_aSessionWebScope.attrs ().values ())
        if (aValue instanceof ISessionWebScopePassivationHandler)
          ((ISessionWebScopePassivationHandler) aValue).onSessionWillPassivate (m_aSessionWebScope);

      if (ScopeHelper.isDebugSessionScopeLifeCycle ())
        LOGGER.info ("Successfully passivated session web scope '" +
                     m_aSessionWebScope.getID () +
                     "' of class " +
                     ClassHelper.getClassLocalName (this),
                     ScopeHelper.getDebugException ());
    }
  }

  public void sessionDidActivate (@NonNull final HttpSessionEvent aEvent)
  {
    final HttpSession aHttpSession = aEvent.getSession ();

    // Create a new session web scope
    final ISessionWebScope aSessionWebScope = WebScopeManager.internalGetOrCreateSessionScope (aHttpSession,
                                                                                               true,
                                                                                               true);

    // Restore the read values into the scope
    if (m_aAttrs != null)
    {
      aSessionWebScope.attrs ().putAllIn (m_aAttrs);
      m_aAttrs.clear ();
    }
    // Remember for later passivation
    m_aSessionWebScope = aSessionWebScope;

    // Invoke callbacks on all attributes
    {
      for (final Object aValue : aSessionWebScope.attrs ().values ())
        if (aValue instanceof ISessionWebScopeActivationHandler)
          ((ISessionWebScopeActivationHandler) aValue).onSessionDidActivate (aSessionWebScope);
    }
    if (ScopeHelper.isDebugSessionScopeLifeCycle ())
      LOGGER.info ("Successfully activated session web scope '" +
                   aSessionWebScope.getID () +
                   "' of class " +
                   ClassHelper.getClassLocalName (this),
                   ScopeHelper.getDebugException ());
  }
}

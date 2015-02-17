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
package com.helger.webscopes.session;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.DevelopersNote;
import com.helger.commons.lang.CGStringHelper;
import com.helger.commons.scopes.ScopeUtils;
import com.helger.commons.scopes.domain.ISessionApplicationScope;
import com.helger.webscopes.domain.ISessionWebScope;
import com.helger.webscopes.mgr.WebScopeManager;

/**
 * This class is responsible for passivating and activating session web scopes.
 * Important: this object itself may NOT be passivated!
 * 
 * @author Philip Helger
 */
public final class SessionWebScopeActivator implements Serializable, HttpSessionActivationListener, ISessionWebScopeDontPassivate
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (SessionWebScopeActivator.class);
  private ISessionWebScope m_aSessionWebScope;
  private Map <String, Object> m_aAttrs;
  private Map <String, ISessionApplicationScope> m_aSessionApplicationScopes;

  @Deprecated
  @DevelopersNote ("For reading only")
  public SessionWebScopeActivator ()
  {}

  /**
   * Constructor for writing
   * 
   * @param aSessionWebScope
   *        the scope to be written
   */
  public SessionWebScopeActivator (@Nonnull final ISessionWebScope aSessionWebScope)
  {
    m_aSessionWebScope = ValueEnforcer.notNull (aSessionWebScope, "SessionWebScope");
  }

  private void writeObject (@Nonnull final ObjectOutputStream out) throws IOException
  {
    if (m_aSessionWebScope == null)
      throw new IllegalStateException ("No SessionWebScope is present!");

    {
      // Determine all attributes to be passivated
      final Map <String, Object> aRelevantObjects = new HashMap <String, Object> ();
      for (final Map.Entry <String, Object> aEntry : m_aSessionWebScope.getAllAttributes ().entrySet ())
      {
        final Object aValue = aEntry.getValue ();
        if (!(aValue instanceof ISessionWebScopeDontPassivate))
          aRelevantObjects.put (aEntry.getKey (), aValue);
      }
      out.writeObject (aRelevantObjects);
    }

    // Write all session application scopes
    {
      final Map <String, ISessionApplicationScope> aSAScopes = m_aSessionWebScope.getAllSessionApplicationScopes ();
      out.writeInt (aSAScopes.size ());
      for (final Map.Entry <String, ISessionApplicationScope> aEntry : aSAScopes.entrySet ())
      {
        // Write scope ID
        out.writeUTF (aEntry.getKey ());

        final ISessionApplicationScope aScope = aEntry.getValue ();
        // Remember all attributes
        final Map <String, Object> aOrigAttrs = aScope.getAllAttributes ();
        // Remove all attributes
        aScope.clear ();

        // Write the scope without attributes
        out.writeObject (aScope);

        // restore the original attributes after serialization
        aScope.setAttributes (aOrigAttrs);

        // Determine all relevant attributes to passivate
        final Map <String, Object> aRelevantObjects = new HashMap <String, Object> ();
        for (final Map.Entry <String, Object> aEntry2 : aOrigAttrs.entrySet ())
        {
          final Object aValue = aEntry2.getValue ();
          if (!(aValue instanceof ISessionWebScopeDontPassivate))
            aRelevantObjects.put (aEntry2.getKey (), aValue);
        }
        out.writeObject (aRelevantObjects);
      }
    }

    if (ScopeUtils.debugSessionScopeLifeCycle (s_aLogger))
      s_aLogger.info ("Wrote info on session web scope '" +
                      m_aSessionWebScope.getID () +
                      "' of class " +
                      CGStringHelper.getClassLocalName (this), ScopeUtils.getDebugStackTrace ());
  }

  @SuppressWarnings ("unchecked")
  private void readObject (@Nonnull final ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    if (m_aSessionWebScope != null)
      throw new IllegalStateException ("Another SessionWebScope is already present: " + m_aSessionWebScope.toString ());

    // Read session attributes
    m_aAttrs = (Map <String, Object>) in.readObject ();

    // Read session application scopes
    final int nSAScopes = in.readInt ();
    final Map <String, ISessionApplicationScope> aSAS = new HashMap <String, ISessionApplicationScope> (nSAScopes);
    for (int i = 0; i < nSAScopes; ++i)
    {
      final String sScopeID = in.readUTF ();
      final ISessionApplicationScope aScope = (ISessionApplicationScope) in.readObject ();
      final Map <String, Object> aScopeAttrs = (Map <String, Object>) in.readObject ();
      aScope.setAttributes (aScopeAttrs);
      aSAS.put (sScopeID, aScope);
    }
    m_aSessionApplicationScopes = aSAS;

    if (ScopeUtils.debugSessionScopeLifeCycle (s_aLogger))
      s_aLogger.info ("Read info on session scope: " +
                          m_aAttrs.size () +
                          " attrs and " +
                          m_aSessionApplicationScopes.size () +
                          " SAScopes of class " +
                          CGStringHelper.getClassLocalName (this),
                      ScopeUtils.getDebugStackTrace ());
  }

  public void sessionWillPassivate (@Nonnull final HttpSessionEvent aEvent)
  {
    // Writing is all handled in the writeObject method

    // Invoke callbacks on all attributes
    {
      for (final Object aValue : m_aSessionWebScope.getAllAttributeValues ())
        if (aValue instanceof ISessionWebScopePassivationHandler)
          ((ISessionWebScopePassivationHandler) aValue).onSessionWillPassivate (m_aSessionWebScope);

      for (final ISessionApplicationScope aScope : m_aSessionWebScope.getAllSessionApplicationScopes ().values ())
        for (final Object aValue : aScope.getAllAttributeValues ())
          if (aValue instanceof ISessionWebScopePassivationHandler)
            ((ISessionWebScopePassivationHandler) aValue).onSessionWillPassivate (m_aSessionWebScope);
    }

    if (ScopeUtils.debugSessionScopeLifeCycle (s_aLogger))
      s_aLogger.info ("Successfully passivated session web scope '" +
                      m_aSessionWebScope.getID () +
                      "' of class " +
                      CGStringHelper.getClassLocalName (this), ScopeUtils.getDebugStackTrace ());
  }

  public void sessionDidActivate (@Nonnull final HttpSessionEvent aEvent)
  {
    final HttpSession aHttpSession = aEvent.getSession ();

    // Create a new session web scope
    final ISessionWebScope aSessionWebScope = WebScopeManager.internalGetOrCreateSessionScope (aHttpSession, true, true);

    // Restore the read values into the scope
    for (final Map.Entry <String, Object> aEntry : m_aAttrs.entrySet ())
      aSessionWebScope.setAttribute (aEntry.getKey (), aEntry.getValue ());
    m_aAttrs.clear ();

    for (final Map.Entry <String, ISessionApplicationScope> aEntry : m_aSessionApplicationScopes.entrySet ())
      aSessionWebScope.restoreSessionApplicationScope (aEntry.getKey (), aEntry.getValue ());
    m_aSessionApplicationScopes.clear ();

    // Remember for later passivation
    m_aSessionWebScope = aSessionWebScope;

    // Invoke callbacks on all attributes
    {
      for (final Object aValue : aSessionWebScope.getAllAttributeValues ())
        if (aValue instanceof ISessionWebScopeActivationHandler)
          ((ISessionWebScopeActivationHandler) aValue).onSessionDidActivate (aSessionWebScope);

      for (final ISessionApplicationScope aScope : aSessionWebScope.getAllSessionApplicationScopes ().values ())
        for (final Object aValue : aScope.getAllAttributeValues ())
          if (aValue instanceof ISessionWebScopeActivationHandler)
            ((ISessionWebScopeActivationHandler) aValue).onSessionDidActivate (aSessionWebScope);
    }

    if (ScopeUtils.debugSessionScopeLifeCycle (s_aLogger))
      s_aLogger.info ("Successfully activated session web scope '" +
                      aSessionWebScope.getID () +
                      "' of class " +
                      CGStringHelper.getClassLocalName (this), ScopeUtils.getDebugStackTrace ());
  }
}

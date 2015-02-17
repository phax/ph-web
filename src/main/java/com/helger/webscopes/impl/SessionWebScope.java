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
package com.helger.webscopes.impl;

import java.io.Serializable;
import java.util.Enumeration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotations.Nonempty;
import com.helger.commons.state.EChange;
import com.helger.commons.state.EContinue;
import com.helger.commons.string.ToStringGenerator;
import com.helger.scopes.impl.SessionScope;
import com.helger.webscopes.MetaWebScopeFactory;
import com.helger.webscopes.domain.ISessionApplicationWebScope;
import com.helger.webscopes.domain.ISessionWebScope;

/**
 * Default implementation of the {@link ISessionWebScope} interface. It is
 * serializable in general, but just don't do it :)
 * 
 * @author Philip Helger
 */
@ThreadSafe
public class SessionWebScope extends SessionScope implements ISessionWebScope
{
  // Because of transient field
  private static final long serialVersionUID = 8912368923565761267L;

  private static final Logger s_aLogger = LoggerFactory.getLogger (SessionWebScope.class);

  // Do not serialize the session
  private final transient HttpSession m_aHttpSession;

  public SessionWebScope (@Nonnull final HttpSession aHttpSession)
  {
    super (aHttpSession.getId ());
    m_aHttpSession = aHttpSession;
  }

  @Override
  public void initScope ()
  {
    // Copy all attributes from the HTTP session in this scope
    final Enumeration <?> aAttrNames = m_aHttpSession.getAttributeNames ();
    if (aAttrNames != null)
      while (aAttrNames.hasMoreElements ())
      {
        final String sAttrName = (String) aAttrNames.nextElement ();
        final Object aAttrValue = m_aHttpSession.getAttribute (sAttrName);
        setAttribute (sAttrName, aAttrValue);
      }
  }

  @Override
  @Nonnull
  public EContinue selfDestruct ()
  {
    // Since the session is still open when we're shutting down the global
    // context, the session must also be invalidated!
    try
    {
      // Should implicitly trigger a call to WebScopeManager.onSessionEnd, which
      // than triggers a call to aSessionScope.destroyScope
      m_aHttpSession.invalidate ();
      // Do not continue with the regular destruction procedure!
      return EContinue.BREAK;
    }
    catch (final RuntimeException ex)
    {
      s_aLogger.warn ("Session '" + getID () + "' was already invalidated, but was still contained!");
    }

    // Continue with the regular destruction
    return EContinue.CONTINUE;
  }

  @Override
  @Nonnull
  public EChange setAttribute (@Nonnull final String sName, @Nullable final Object aNewValue)
  {
    if (aNewValue != null && !(aNewValue instanceof Serializable))
      s_aLogger.warn ("Value of class " + aNewValue.getClass ().getName () + " should implement Serializable!");

    return super.setAttribute (sName, aNewValue);
  }

  @Override
  @Nonnull
  protected ISessionApplicationWebScope createSessionApplicationScope (@Nonnull @Nonempty final String sApplicationID)
  {
    return MetaWebScopeFactory.getWebScopeFactory ().createSessionApplicationScope (sApplicationID);
  }

  @Override
  @Nullable
  public ISessionApplicationWebScope getSessionApplicationScope (@Nonnull @Nonempty final String sApplicationID,
                                                                 final boolean bCreateIfNotExisting)
  {
    return (ISessionApplicationWebScope) super.getSessionApplicationScope (sApplicationID, bCreateIfNotExisting);
  }

  @Nonnull
  public HttpSession getSession ()
  {
    return m_aHttpSession;
  }

  public long getCreationTime ()
  {
    return m_aHttpSession.getCreationTime ();
  }

  public long getLastAccessedTime ()
  {
    return m_aHttpSession.getLastAccessedTime ();
  }

  public long getMaxInactiveInterval ()
  {
    return m_aHttpSession.getMaxInactiveInterval ();
  }

  public boolean isNew ()
  {
    return m_aHttpSession.isNew ();
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ()).append ("httpSession", m_aHttpSession).toString ();
  }
}

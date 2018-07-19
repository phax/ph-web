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
package com.helger.web.scope.impl;

import java.io.Serializable;
import java.util.Enumeration;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.state.EContinue;
import com.helger.commons.string.ToStringGenerator;
import com.helger.scope.SessionScope;
import com.helger.web.scope.ISessionWebScope;

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

  private static final Logger LOGGER = LoggerFactory.getLogger (SessionWebScope.class);

  // Do not serialize the session
  private final transient HttpSession m_aHttpSession;

  public SessionWebScope (@Nonnull final HttpSession aHttpSession)
  {
    super (aHttpSession.getId ());
    m_aHttpSession = aHttpSession;

    attrs ().beforeSetValueCallbacks ().add ( (aName, aNewValueValue) -> {
      if (aNewValueValue != null && !(aNewValueValue instanceof Serializable))
        LOGGER.warn ("Value of class " + aNewValueValue.getClass ().getName () + " should implement Serializable!");
      return EContinue.CONTINUE;
    });
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
        attrs ().putIn (sAttrName, aAttrValue);
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
      LOGGER.warn ("Session '" + getID () + "' was already invalidated, but was still contained!");
    }

    // Continue with the regular destruction
    return EContinue.CONTINUE;
  }

  @Nonnull
  public HttpSession getSession ()
  {
    return m_aHttpSession;
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ()).append ("HttpSession", m_aHttpSession).getToString ();
  }
}

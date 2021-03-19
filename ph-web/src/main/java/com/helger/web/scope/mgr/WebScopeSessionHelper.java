/**
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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
package com.helger.web.scope.mgr;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.state.EChange;
import com.helger.scope.IScopeRenewalAware;
import com.helger.scope.mgr.ScopeSessionManager;
import com.helger.web.scope.ISessionWebScope;

/**
 * Some utility methods to handle complex actions in session scopes.
 *
 * @author Philip Helger
 */
@Immutable
public final class WebScopeSessionHelper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (WebScopeSessionHelper.class);

  @PresentForCodeCoverage
  private static final WebScopeSessionHelper INSTANCE = new WebScopeSessionHelper ();

  private WebScopeSessionHelper ()
  {}

  private static void _restoreScopeAttributes (@Nonnull final ISessionWebScope aNewSessionScope,
                                               @Nonnull final Map <String, IScopeRenewalAware> aSessionScopeValues)
  {
    // restore the session scope attributes
    for (final Map.Entry <String, IScopeRenewalAware> aEntry : aSessionScopeValues.entrySet ())
      aNewSessionScope.attrs ().putIn (aEntry.getKey (), aEntry.getValue ());
  }

  /**
   * Renew the current session scope. This means all session and session
   * application scopes are cleared, and only attributes implementing the
   * {@link IScopeRenewalAware} interface are kept.
   *
   * @param bInvalidateHttpSession
   *        if <code>true</code> the underlying HTTP session is also invalidated
   *        and a new session is created.
   * @return {@link EChange#UNCHANGED} if no session scope is present.
   */
  @Nonnull
  public static EChange renewCurrentSessionScope (final boolean bInvalidateHttpSession)
  {
    // Get the old session scope
    final ISessionWebScope aOldSessionScope = WebScopeManager.getSessionScope (false);
    if (aOldSessionScope == null)
    {
      // No session present, so no need to create a new one
      return EChange.UNCHANGED;
    }

    // OK, we have a session scope to renew

    // Save all values from session scopes and from all session application
    // scopes
    final ICommonsMap <String, IScopeRenewalAware> aSessionScopeValues = aOldSessionScope.getAllScopeRenewalAwareAttributes ();

    // Clear the old the session scope
    if (bInvalidateHttpSession)
    {
      // renew the session
      LOGGER.info ("Invalidating session " + aOldSessionScope.getID ());
      aOldSessionScope.selfDestruct ();
    }
    else
    {
      // Do not invalidate the underlying session - only renew the session scope
      // itself
      ScopeSessionManager.getInstance ().onScopeEnd (aOldSessionScope);
    }

    // Ensure that we get a new session!
    // Here it is OK to create a new session scope explicitly!
    final ISessionWebScope aNewSessionScope = WebScopeManager.internalGetSessionScope (true, true);
    _restoreScopeAttributes (aNewSessionScope, aSessionScopeValues);
    return EChange.CHANGED;
  }

  /**
   * Renew the session scope identified by the passed HTTP session. Note: the
   * underlying HTTP session is not invalidate, because we have no way to
   * retrieve a new underlying HTTP session, because no request is present.
   *
   * @param aHttpSession
   *        The HTTP session to be renewed.
   * @return <code>null</code> if nothing was changed, the new session web scope
   *         otherwise.
   */
  @Nullable
  public static ISessionWebScope renewSessionScope (@Nonnull final HttpSession aHttpSession)
  {
    ValueEnforcer.notNull (aHttpSession, "HttpSession");

    // Get the old session scope
    final ISessionWebScope aOldSessionScope = WebScopeManager.internalGetOrCreateSessionScope (aHttpSession, false, false);
    if (aOldSessionScope == null)
      return null;

    // OK, we have a session scope to renew

    // Save all values from session scopes and from all session application
    // scopes
    final ICommonsMap <String, IScopeRenewalAware> aSessionScopeValues = aOldSessionScope.getAllScopeRenewalAwareAttributes ();

    // Do not invalidate the underlying session - only renew the session scope
    // itself because we don't have the possibility to create a new HTTP
    // session for an arbitrary user!
    ScopeSessionManager.getInstance ().onScopeEnd (aOldSessionScope);

    // Ensure that we get a new session!
    // Here it is OK to create a new session scope explicitly!
    final ISessionWebScope aNewSessionScope = WebScopeManager.internalGetOrCreateSessionScope (aHttpSession, true, true);
    _restoreScopeAttributes (aNewSessionScope, aSessionScopeValues);
    return aNewSessionScope;
  }
}

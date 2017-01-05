/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ext.CommonsHashMap;
import com.helger.commons.collection.ext.ICommonsMap;
import com.helger.commons.scope.IScopeRenewalAware;
import com.helger.commons.scope.ISessionApplicationScope;
import com.helger.commons.scope.mgr.ScopeSessionManager;
import com.helger.commons.state.EChange;
import com.helger.web.scope.ISessionApplicationWebScope;
import com.helger.web.scope.ISessionWebScope;

/**
 * Some utility methods to handle complex actions in session scopes.
 *
 * @author Philip Helger
 */
@Immutable
public final class WebScopeSessionHelper
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (WebScopeSessionHelper.class);

  @PresentForCodeCoverage
  private static final WebScopeSessionHelper s_aInstance = new WebScopeSessionHelper ();

  private WebScopeSessionHelper ()
  {}

  @Nonnull
  @ReturnsMutableCopy
  private static ICommonsMap <String, ICommonsMap <String, IScopeRenewalAware>> _getSessionApplicationScopeValues (@Nonnull final ISessionWebScope aOldSessionScope)
  {
    // Map from <application ID> to <map from <field name> to <field value>>
    final ICommonsMap <String, ICommonsMap <String, IScopeRenewalAware>> aSessionApplicationScopeValues = new CommonsHashMap <> ();

    // For all session application scope values
    final ICommonsMap <String, ISessionApplicationScope> aAllSessionApplicationScopes = aOldSessionScope.getAllSessionApplicationScopes ();
    if (!aAllSessionApplicationScopes.isEmpty ())
    {
      // For all existing session application scopes in the session scope
      for (final Map.Entry <String, ISessionApplicationScope> aEntry : aAllSessionApplicationScopes.entrySet ())
      {
        // Get all values from the current session application scope
        final ICommonsMap <String, IScopeRenewalAware> aSurviving = aEntry.getValue ()
                                                                          .getAllScopeRenewalAwareAttributes ();
        if (!aSurviving.isEmpty ())
        {
          // Extract the application ID
          final String sScopeApplicationID = aOldSessionScope.getApplicationIDFromApplicationScopeID (aEntry.getKey ());
          aSessionApplicationScopeValues.put (sScopeApplicationID, aSurviving);
        }
      }
    }
    return aSessionApplicationScopeValues;
  }

  private static void _restoreScopeAttributes (@Nonnull final ISessionWebScope aNewSessionScope,
                                               @Nonnull final Map <String, IScopeRenewalAware> aSessionScopeValues,
                                               @Nonnull final Map <String, ? extends Map <String, IScopeRenewalAware>> aSessionApplicationScopeValues)
  {
    // restore the session scope attributes
    for (final Map.Entry <String, IScopeRenewalAware> aEntry : aSessionScopeValues.entrySet ())
      aNewSessionScope.setAttribute (aEntry.getKey (), aEntry.getValue ());

    // restore the session application scope attributes
    for (final Map.Entry <String, ? extends Map <String, IScopeRenewalAware>> aEntry : aSessionApplicationScopeValues.entrySet ())
    {
      // Create the session application scope in the new session scope
      final ISessionApplicationWebScope aNewSessionApplicationScope = aNewSessionScope.getSessionApplicationScope (aEntry.getKey (),
                                                                                                                   true);

      // Put all attributes in
      for (final Map.Entry <String, IScopeRenewalAware> aInnerEntry : aEntry.getValue ().entrySet ())
        aNewSessionApplicationScope.setAttribute (aInnerEntry.getKey (), aInnerEntry.getValue ());
    }
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
      return EChange.UNCHANGED;

    // OK, we have a session scope to renew

    // Save all values from session scopes and from all session application
    // scopes
    final ICommonsMap <String, IScopeRenewalAware> aSessionScopeValues = aOldSessionScope.getAllScopeRenewalAwareAttributes ();
    final ICommonsMap <String, ICommonsMap <String, IScopeRenewalAware>> aSessionApplicationScopeValues = _getSessionApplicationScopeValues (aOldSessionScope);

    // Clear the old the session scope
    if (bInvalidateHttpSession)
    {
      // renew the session
      s_aLogger.info ("Invalidating session " + aOldSessionScope.getID ());
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
    _restoreScopeAttributes (aNewSessionScope, aSessionScopeValues, aSessionApplicationScopeValues);
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
    final ISessionWebScope aOldSessionScope = WebScopeManager.internalGetOrCreateSessionScope (aHttpSession,
                                                                                               false,
                                                                                               false);
    if (aOldSessionScope == null)
      return null;

    // OK, we have a session scope to renew

    // Save all values from session scopes and from all session application
    // scopes
    final ICommonsMap <String, IScopeRenewalAware> aSessionScopeValues = aOldSessionScope.getAllScopeRenewalAwareAttributes ();
    final ICommonsMap <String, ICommonsMap <String, IScopeRenewalAware>> aSessionApplicationScopeValues = _getSessionApplicationScopeValues (aOldSessionScope);

    // Do not invalidate the underlying session - only renew the session scope
    // itself because we don't have the possibility to create a new HTTP
    // session for an arbitrary user!
    ScopeSessionManager.getInstance ().onScopeEnd (aOldSessionScope);

    // Ensure that we get a new session!
    // Here it is OK to create a new session scope explicitly!
    final ISessionWebScope aNewSessionScope = WebScopeManager.internalGetOrCreateSessionScope (aHttpSession,
                                                                                               true,
                                                                                               true);
    _restoreScopeAttributes (aNewSessionScope, aSessionScopeValues, aSessionApplicationScopeValues);
    return aNewSessionScope;
  }
}

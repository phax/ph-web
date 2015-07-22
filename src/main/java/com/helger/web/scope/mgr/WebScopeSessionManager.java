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
package com.helger.web.scope.mgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.scope.ISessionScope;
import com.helger.commons.scope.mgr.ScopeSessionManager;
import com.helger.web.scope.ISessionWebScope;

/**
 * This is a specialization of {@link ScopeSessionManager} for web scopes.
 *
 * @author Philip Helger
 */
@Immutable
public final class WebScopeSessionManager
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (WebScopeSessionManager.class);

  @PresentForCodeCoverage
  private static final WebScopeSessionManager s_aInstance = new WebScopeSessionManager ();

  private WebScopeSessionManager ()
  {}

  /**
   * Get the web session scope with the specified ID. If no such scope exists,
   * <code>null</code> is returned.
   *
   * @param sScopeID
   *        The ID to be resolved.
   * @return <code>null</code> if no such scope exists or if the passed scope is
   *         not a session web scopes.
   */
  @Nullable
  public static ISessionWebScope getSessionWebScopeOfID (@Nullable final String sScopeID)
  {
    final ISessionScope aSessionScope = ScopeSessionManager.getInstance ().getSessionScopeOfID (sScopeID);
    if (aSessionScope == null)
      return null;
    if (!(aSessionScope instanceof ISessionWebScope))
    {
      s_aLogger.warn ("The passed scope ID '" + sScopeID + "' is not a session web scope: " + aSessionScope.toString ());
      return null;
    }
    return (ISessionWebScope) aSessionScope;
  }

  /**
   * Get the session web scope of the passed HTTP session.
   *
   * @param aHttpSession
   *        The HTTP session to get the scope from. May be <code>null</code>.
   * @return <code>null</code> if either the HTTP session is <code>null</code>
   *         or if no such scope exists.
   */
  @Nullable
  public static ISessionWebScope getSessionWebScopeOfSession (@Nullable final HttpSession aHttpSession)
  {
    return aHttpSession == null ? null : getSessionWebScopeOfID (aHttpSession.getId ());
  }

  /**
   * @return A non-<code>null</code>, mutable copy of all managed session web
   *         scopes.
   */
  @Nonnull
  @ReturnsMutableCopy
  public static Collection <? extends ISessionWebScope> getAllSessionWebScopes ()
  {
    final List <ISessionWebScope> ret = new ArrayList <ISessionWebScope> ();
    for (final ISessionScope aSessionScope : ScopeSessionManager.getInstance ().getAllSessionScopes ())
      if (aSessionScope instanceof ISessionWebScope)
        ret.add ((ISessionWebScope) aSessionScope);
    return ret;
  }

  /**
   * Destroy all available web scopes.
   */
  public static void destroyAllWebSessions ()
  {
    // destroy all session web scopes (make a copy, because we're invalidating
    // the sessions!)
    for (final ISessionWebScope aSessionScope : getAllSessionWebScopes ())
    {
      // Unfortunately we need a special handling here
      if (aSessionScope.selfDestruct ().isContinue ())
      {
        // Remove from map
        ScopeSessionManager.getInstance ().onScopeEnd (aSessionScope);
      }
      // Else the destruction was already started!
    }
  }
}

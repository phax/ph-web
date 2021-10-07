/*
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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.DevelopersNote;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.collection.impl.CommonsHashSet;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.scope.IGlobalScope;
import com.helger.scope.IRequestScope;
import com.helger.scope.ISessionScope;
import com.helger.scope.mgr.ScopeManager;
import com.helger.scope.mgr.ScopeSessionManager;
import com.helger.web.scope.IGlobalWebScope;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.ISessionWebScope;
import com.helger.web.scope.impl.GlobalWebScope;
import com.helger.web.scope.impl.SessionWebScope;
import com.helger.web.scope.multipart.RequestWebScopeMultipart;
import com.helger.web.scope.session.SessionWebScopeActivator;

/**
 * This is the main manager class for web scope handling.
 *
 * @author Philip Helger
 */
@Immutable
public final class WebScopeManager
{
  // For backward compatibility passivation is disabled
  public static final boolean DEFAULT_SESSION_PASSIVATION_ALLOWED = false;
  private static final String SESSION_ATTR_SESSION_SCOPE_ACTIVATOR = ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL +
                                                                     "sessionwebscope.activator";
  private static final Logger LOGGER = LoggerFactory.getLogger (WebScopeManager.class);
  private static final AtomicBoolean SESSION_PASSIVATION_ALLOWED = new AtomicBoolean (DEFAULT_SESSION_PASSIVATION_ALLOWED);

  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  private static final ICommonsSet <String> SESSION_IN_INVALIDATION = new CommonsHashSet <> ();

  @PresentForCodeCoverage
  private static final WebScopeManager INSTANCE = new WebScopeManager ();

  private WebScopeManager ()
  {}

  // --- settings ---

  /**
   * @return <code>true</code> if session passivation is allowed. Default is
   *         {@link #DEFAULT_SESSION_PASSIVATION_ALLOWED}
   */
  public static boolean isSessionPassivationAllowed ()
  {
    return SESSION_PASSIVATION_ALLOWED.get ();
  }

  /**
   * Allow or disallow session passivation
   *
   * @param bSessionPassivationAllowed
   *        <code>true</code> to enable session passivation, <code>false</code>
   *        to disable it
   */
  public static void setSessionPassivationAllowed (final boolean bSessionPassivationAllowed)
  {
    SESSION_PASSIVATION_ALLOWED.set (bSessionPassivationAllowed);
    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Session passivation is now " + (bSessionPassivationAllowed ? "enabled" : "disabled"));

    // For passivation to work, the session scopes may not be invalidated at the
    // end of the global scope!
    final ScopeSessionManager aSSM = ScopeSessionManager.getInstance ();
    aSSM.setDestroyAllSessionsOnScopeEnd (!bSessionPassivationAllowed);
    aSSM.setEndAllSessionsOnScopeEnd (!bSessionPassivationAllowed);

    // Ensure that all session web scopes have the activator set or removed
    for (final ISessionWebScope aSessionWebScope : WebScopeSessionManager.getAllSessionWebScopes ())
    {
      final HttpSession aHttpSession = aSessionWebScope.getSession ();
      if (bSessionPassivationAllowed)
      {
        // Ensure the activator is present
        if (aHttpSession.getAttribute (SESSION_ATTR_SESSION_SCOPE_ACTIVATOR) == null)
          aHttpSession.setAttribute (SESSION_ATTR_SESSION_SCOPE_ACTIVATOR, new SessionWebScopeActivator (aSessionWebScope));
      }
      else
      {
        // Ensure the activator is not present
        aHttpSession.removeAttribute (SESSION_ATTR_SESSION_SCOPE_ACTIVATOR);
      }
    }
  }

  // --- global scope ---

  /**
   * To be called, when the global web scope is initialized. Most commonly this
   * is called from within
   * {@link javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)}
   *
   * @param aServletContext
   *        The source servlet context to be used to retrieve the scope ID. May
   *        not be <code>null</code>
   * @return The created global web scope
   */
  @Nonnull
  public static IGlobalWebScope onGlobalBegin (@Nonnull final ServletContext aServletContext)
  {
    return onGlobalBegin (aServletContext, GlobalWebScope::new);
  }

  @Nonnull
  public static IGlobalWebScope onGlobalBegin (@Nonnull final ServletContext aServletContext,
                                               @Nonnull final Function <? super ServletContext, ? extends IGlobalWebScope> aFactory)
  {
    final IGlobalWebScope aGlobalScope = aFactory.apply (aServletContext);
    ScopeManager.setGlobalScope (aGlobalScope);
    return aGlobalScope;
  }

  /**
   * @return <code>true</code> if a global scope is defined, <code>false</code>
   *         if none is defined
   */
  public static boolean isGlobalScopePresent ()
  {
    return ScopeManager.isGlobalScopePresent ();
  }

  /**
   * @return The global scope object or <code>null</code> if no global web scope
   *         is present.
   */
  @Nullable
  public static IGlobalWebScope getGlobalScopeOrNull ()
  {
    final IGlobalScope aGlobalScope = ScopeManager.getGlobalScopeOrNull ();
    try
    {
      return (IGlobalWebScope) aGlobalScope;
    }
    catch (final ClassCastException ex)
    {
      if (LOGGER.isWarnEnabled ())
        LOGGER.warn ("Gobal scope object is not a global web scope: " + aGlobalScope, ex);
      return null;
    }
  }

  /**
   * @return The global scope object and never <code>null</code>.
   * @throws IllegalStateException
   *         If no global web scope object is present
   */
  @Nonnull
  public static IGlobalWebScope getGlobalScope ()
  {
    final IGlobalWebScope aGlobalScope = getGlobalScopeOrNull ();
    if (aGlobalScope == null)
      throw new IllegalStateException ("No global web scope object has been set!");
    return aGlobalScope;
  }

  /**
   * To be called, when the global web scope is destroyed. Most commonly this is
   * called from within
   * {@link javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)}
   */
  public static void onGlobalEnd ()
  {
    ScopeManager.onGlobalEnd ();
  }

  // --- session scope ---

  /**
   * To be called, when a session web scope is initialized. Most commonly this
   * is called from within
   * {@link javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)}
   *
   * @param aHttpSession
   *        The source session to base the scope on. May not be
   *        <code>null</code>
   * @return The created global session scope
   */
  @Nonnull
  public static ISessionWebScope onSessionBegin (@Nonnull final HttpSession aHttpSession)
  {
    return onSessionBegin (aHttpSession, SessionWebScope::new);
  }

  @Nonnull
  public static <T extends ISessionWebScope> T onSessionBegin (@Nonnull final HttpSession aHttpSession,
                                                               @Nonnull final Function <? super HttpSession, T> aFactory)
  {
    final T aSessionWebScope = aFactory.apply (aHttpSession);
    ScopeSessionManager.getInstance ().onScopeBegin (aSessionWebScope);
    if (isSessionPassivationAllowed ())
    {
      // Add the special session activator
      aHttpSession.setAttribute (SESSION_ATTR_SESSION_SCOPE_ACTIVATOR, new SessionWebScopeActivator (aSessionWebScope));
    }
    return aSessionWebScope;
  }

  /**
   * Internal method which does the main logic for session web scope creation
   *
   * @param aHttpSession
   *        The underlying HTTP session
   * @param bCreateIfNotExisting
   *        if <code>true</code> if a new session web scope is created, if none
   *        is present
   * @param bItsOkayToCreateANewScope
   *        if <code>true</code> no warning is emitted, if a new session scope
   *        must be created. This is e.g. used when renewing a session or when
   *        activating a previously passivated session.
   * @return <code>null</code> if no session scope is present, and
   *         bCreateIfNotExisting is false
   */
  @Nullable
  @DevelopersNote ("This is only for project-internal use!")
  public static ISessionWebScope internalGetOrCreateSessionScope (@Nonnull final HttpSession aHttpSession,
                                                                  final boolean bCreateIfNotExisting,
                                                                  final boolean bItsOkayToCreateANewScope)
  {
    ValueEnforcer.notNull (aHttpSession, "HttpSession");

    // Do we already have a session web scope for the session?
    final String sSessionID = aHttpSession.getId ();
    ISessionScope aSessionWebScope = ScopeSessionManager.getInstance ().getSessionScopeOfID (sSessionID);
    if (aSessionWebScope == null && bCreateIfNotExisting)
    {
      if (!bItsOkayToCreateANewScope)
      {
        // This can e.g. happen in tests, when there are no registered
        // listeners for session events!
        // Or after an application server restart, if the cookie from the old
        // server is present and session passivation is not enabled
        if (LOGGER.isWarnEnabled ())
          LOGGER.warn ("Creating a new session web scope for ID '" +
                       sSessionID +
                       "' but there should already be one!" +
                       " Check your HttpSessionListener implementation." +
                       " Ignore this after a application server restart.");
      }

      // Create a new session scope
      aSessionWebScope = onSessionBegin (aHttpSession);
    }

    try
    {
      return (ISessionWebScope) aSessionWebScope;
    }
    catch (final ClassCastException ex)
    {
      throw new IllegalStateException ("Session scope object is not a web scope but: " + aSessionWebScope, ex);
    }
  }

  /**
   * Get or create a session scope based on the current request scope. This is
   * the same as calling
   * <code>getSessionScope({@link ScopeManager#DEFAULT_CREATE_SCOPE})</code>
   *
   * @return Never <code>null</code>.
   */
  @Nonnull
  public static ISessionWebScope getSessionScope ()
  {
    return getSessionScope (ScopeManager.DEFAULT_CREATE_SCOPE);
  }

  /**
   * Get the session scope from the current request scope.
   *
   * @param bCreateIfNotExisting
   *        if <code>true</code> a new session scope (and a new HTTP session if
   *        required) is created if none is existing so far.
   * @return <code>null</code> if no session scope is present, and none should
   *         be created.
   */
  @Nullable
  public static ISessionWebScope getSessionScope (final boolean bCreateIfNotExisting)
  {
    return internalGetSessionScope (bCreateIfNotExisting, false);
  }

  /**
   * Get the session scope from the current request scope.
   *
   * @param bCreateIfNotExisting
   *        if <code>true</code> a new session scope (and a new HTTP session if
   *        required) is created if none is existing so far.
   * @param bItsOkayToCreateANewSession
   *        if <code>true</code> no warning is emitted, if a new session scope
   *        must be created. This is e.g. used when renewing a session.
   * @return <code>null</code> if no session scope is present, and none should
   *         be created.
   */
  @Nullable
  @DevelopersNote ("This is only for project-internal use!")
  public static ISessionWebScope internalGetSessionScope (final boolean bCreateIfNotExisting, final boolean bItsOkayToCreateANewSession)
  {
    // Try to to resolve the current request scope
    final IRequestWebScope aRequestScope = getRequestScopeOrNull ();
    return internalGetSessionScope (aRequestScope, bCreateIfNotExisting, bItsOkayToCreateANewSession);
  }

  /**
   * Get the session scope of the provided request scope.
   *
   * @param aRequestScope
   *        The request scope it is about. May be <code>null</code>.
   * @param bCreateIfNotExisting
   *        if <code>true</code> a new session scope (and a new HTTP session if
   *        required) is created if none is existing so far.
   * @param bItsOkayToCreateANewSession
   *        if <code>true</code> no warning is emitted, if a new session scope
   *        must be created. This is e.g. used when renewing a session.
   * @return <code>null</code> if no session scope is present, and none should
   *         be created.
   */
  @Nullable
  @DevelopersNote ("This is only for project-internal use!")
  public static ISessionWebScope internalGetSessionScope (@Nullable final IRequestWebScope aRequestScope,
                                                          final boolean bCreateIfNotExisting,
                                                          final boolean bItsOkayToCreateANewSession)
  {
    // Try to to resolve the current request scope
    if (aRequestScope != null)
    {
      // Check if we have an HTTP session object
      final HttpSession aHttpSession = aRequestScope.getSession (bCreateIfNotExisting);
      if (aHttpSession != null)
        return internalGetOrCreateSessionScope (aHttpSession, bCreateIfNotExisting, bItsOkayToCreateANewSession);
    }
    else
    {
      // If we want a session scope, we expect the return value to be non-null!
      if (bCreateIfNotExisting)
        throw new IllegalStateException ("No request scope is present, so no session scope can be retrieved!");
    }
    return null;
  }

  /**
   * To be called, when a session web scope is destroyed. Most commonly this is
   * called from within
   * {@link javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)}
   *
   * @param aHttpSession
   *        The source session to destroy the matching scope. May not be
   *        <code>null</code>
   */
  public static void onSessionEnd (@Nonnull final HttpSession aHttpSession)
  {
    ValueEnforcer.notNull (aHttpSession, "HttpSession");

    final ScopeSessionManager aSSM = ScopeSessionManager.getInstance ();
    final String sSessionID = aHttpSession.getId ();
    final ISessionScope aSessionScope = aSSM.getSessionScopeOfID (sSessionID);
    if (aSessionScope != null)
    {
      // Regular scope end
      aSSM.onScopeEnd (aSessionScope);
    }
    else
    {
      // Ensure session is invalidated anyhow, even if no session scope is
      // present.
      // Happens in Tomcat startup if sessions that where serialized in
      // a previous invocation are invalidated on Tomcat restart

      // Ensure that session.invalidate can not be called recursively
      final boolean bCanInvalidateSession = RW_LOCK.writeLockedBoolean ( () -> SESSION_IN_INVALIDATION.add (sSessionID));

      if (bCanInvalidateSession)
      {
        try
        {
          aHttpSession.invalidate ();
          if (LOGGER.isWarnEnabled ())
            LOGGER.warn ("Found no session scope but invalidated session '" + sSessionID + "' anyway");
        }
        catch (final IllegalStateException ex)
        {
          // session already invalidated
        }
        finally
        {
          // Remove from "in invalidation" list
          RW_LOCK.writeLocked ( () -> SESSION_IN_INVALIDATION.remove (sSessionID));
        }
      }
    }
  }

  // --- request scopes ---

  @Nonnull
  public static IRequestWebScope onRequestBegin (@Nonnull final HttpServletRequest aHttpRequest,
                                                 @Nonnull final HttpServletResponse aHttpResponse)
  {
    return onRequestBegin (aHttpRequest, aHttpResponse, RequestWebScopeMultipart::new);
  }

  @Nonnull
  public static <T extends IRequestWebScope> T onRequestBegin (@Nonnull final HttpServletRequest aHttpRequest,
                                                               @Nonnull final HttpServletResponse aHttpResponse,
                                                               @Nonnull final BiFunction <? super HttpServletRequest, ? super HttpServletResponse, T> aFactory)
  {
    final T aRequestScope = aFactory.apply (aHttpRequest, aHttpResponse);
    ScopeManager.internalSetAndInitRequestScope (aRequestScope);
    return aRequestScope;
  }

  @Nullable
  public static IRequestWebScope getRequestScopeOrNull ()
  {
    final IRequestScope aRequestScope = ScopeManager.getRequestScopeOrNull ();
    try
    {
      return (IRequestWebScope) aRequestScope;
    }
    catch (final ClassCastException ex)
    {
      if (LOGGER.isWarnEnabled ())
        LOGGER.warn ("Request scope object is not a request web scope: " + aRequestScope, ex);
      return null;
    }
  }

  public static boolean isRequestScopePresent ()
  {
    return ScopeManager.getRequestScopeOrNull () instanceof IRequestWebScope;
  }

  @Nonnull
  public static IRequestWebScope getRequestScope ()
  {
    final IRequestWebScope aRequestScope = getRequestScopeOrNull ();
    if (aRequestScope == null)
      throw new IllegalStateException ("No request web scope object has been set!");
    return aRequestScope;
  }

  public static void onRequestEnd ()
  {
    ScopeManager.onRequestEnd ();
  }
}

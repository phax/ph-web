/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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
package com.helger.xservlet.servletstatus;

import java.lang.reflect.Modifier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.ELockType;
import com.helger.annotation.concurrent.GuardedBy;
import com.helger.annotation.concurrent.MustBeLocked;
import com.helger.annotation.concurrent.ThreadSafe;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.annotation.style.UsedViaReflection;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.state.EChange;
import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsMap;
import com.helger.scope.singleton.AbstractGlobalSingleton;
import com.helger.web.scope.IGlobalWebScope;
import com.helger.web.scope.mgr.WebScopeManager;

import jakarta.servlet.GenericServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

/**
 * A manager for keeping track of the default servlets states.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
@ThreadSafe
public final class ServletStatusManager extends AbstractGlobalSingleton
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ServletStatusManager.class);

  @GuardedBy ("m_aRWLock")
  private final ICommonsMap <String, ServletStatus> m_aMap = new CommonsHashMap <> ();

  @Deprecated (forRemoval = false)
  @UsedViaReflection
  public ServletStatusManager ()
  {}

  @NonNull
  public static ServletStatusManager getInstance ()
  {
    return getGlobalSingleton (ServletStatusManager.class);
  }

  @Nullable
  public static ServletStatusManager getInstanceIfInstantiated ()
  {
    return getGlobalSingletonIfInstantiated (ServletStatusManager.class);
  }

  /**
   * Reset all contained information!
   *
   * @return {@link EChange}
   */
  @NonNull
  public EChange reset ()
  {
    return m_aRWLock.writeLockedGet (m_aMap::removeAll);
  }

  @NonNull
  @Nonempty
  private static String _getKey (@NonNull final Class <? extends GenericServlet> aServletClass)
  {
    return aServletClass.getName ();
  }

  @NonNull
  @MustBeLocked (ELockType.WRITE)
  private ServletStatus _getOrCreateServletStatus (@NonNull final Class <? extends GenericServlet> aServletClass)
  {
    ValueEnforcer.notNull (aServletClass, "Servlet class");
    if (Modifier.isAbstract (aServletClass.getModifiers ()))
      throw new IllegalStateException ("Passed servlet class is abstract: " + aServletClass);

    final String sKey = _getKey (aServletClass);
    return m_aMap.computeIfAbsent (sKey, k -> new ServletStatus (aServletClass.getName ()));
  }

  private void _updateStatus (@NonNull final Class <? extends GenericServlet> aServletClass,
                              @NonNull final EServletStatus eNewStatus)
  {
    ValueEnforcer.notNull (eNewStatus, "NewStatus");

    m_aRWLock.writeLocked ( () -> _getOrCreateServletStatus (aServletClass).internalSetCurrentStatus (eNewStatus));

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Servlet status of " + aServletClass + " changed to " + eNewStatus);
  }

  public void onServletCtor (@NonNull final Class <? extends GenericServlet> aServletClass)
  {
    _updateStatus (aServletClass, EServletStatus.CONSTRUCTED);
  }

  /**
   * Invoked at the beginning of the servlet initialization.
   *
   * @param aServletClass
   *        Relevant servlet class. May not be <code>null</code>.
   */
  public void onServletInit (@NonNull final Class <? extends GenericServlet> aServletClass)
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("onServletInit: " + aServletClass);
    _updateStatus (aServletClass, EServletStatus.INITED);
  }

  public void onServletInitFailed (@NonNull final Exception aInitException,
                                   @NonNull final Class <? extends GenericServlet> aServletClass)
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("onServletInitFailed: " + aServletClass, aInitException);
    // Reset status to previous state!
    _updateStatus (aServletClass, EServletStatus.CONSTRUCTED);
  }

  /**
   * Invoked at the beginning of a servlet invocation
   *
   * @param aServletClass
   *        Servlet class invoked. May not be <code>null</code>.
   */
  public void onServletInvocation (@NonNull final Class <? extends GenericServlet> aServletClass)
  {
    m_aRWLock.writeLocked ( () -> _getOrCreateServletStatus (aServletClass).internalIncrementInvocationCount ());
  }

  public void onServletDestroy (@NonNull final Class <? extends GenericServlet> aServletClass)
  {
    _updateStatus (aServletClass, EServletStatus.DESTROYED);
  }

  @Nullable
  public ServletStatus getStatus (@Nullable final Class <? extends GenericServlet> aServletClass)
  {
    if (aServletClass == null)
      return null;

    final String sKey = _getKey (aServletClass);
    return m_aRWLock.readLockedGet ( () -> m_aMap.get (sKey));
  }

  @NonNull
  @ReturnsMutableCopy
  public ICommonsMap <String, ServletStatus> getAllStatus ()
  {
    return m_aRWLock.readLockedGet (m_aMap::getClone);
  }

  /**
   * Static utility method that checks the {@link ServletContext} whether the
   * passed servlet class is registered or not.
   *
   * @param aServletClass
   *        The servlet class to be checked. May not be <code>null</code>.
   * @return <code>true</code> if the passed servlet class is contained in the
   *         {@link ServletContext}.
   */
  public static boolean isServletRegistered (@NonNull final Class <? extends GenericServlet> aServletClass)
  {
    final String sClassName = ValueEnforcer.notNull (aServletClass, "ServletClass").getName ();

    // May be null for unit tests
    final IGlobalWebScope aGlobalScope = WebScopeManager.getGlobalScopeOrNull ();
    if (aGlobalScope != null)
    {
      try
      {
        for (final ServletRegistration aRegistration : aGlobalScope.getServletContext ()
                                                                   .getServletRegistrations ()
                                                                   .values ())
          if (aRegistration.getClassName ().equals (sClassName))
            return true;
      }
      catch (final UnsupportedOperationException ex)
      {
        // Happens for mock servlet contexts
      }
    }
    return false;
  }
}

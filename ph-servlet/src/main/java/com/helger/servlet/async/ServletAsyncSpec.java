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
package com.helger.servlet.async;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.string.ToStringGenerator;
import com.helger.http.EHTTPMethod;

/**
 * This class encapsulates all the parameters necessary to handle asynchronous
 * servlet requests.
 *
 * @author Philip Helger
 * @since 8.8.0
 */
@Immutable
public class ServletAsyncSpec
{
  /** The constant defining "no timeout defined" */
  public static final long NO_TIMEOUT = 0L;
  /** The constant for synchronous invocations */
  public static final ServletAsyncSpec SYNC_SPEC = new ServletAsyncSpec (false,
                                                                         NO_TIMEOUT,
                                                                         (Iterable <? extends AsyncListener>) null,
                                                                         (Set <EHTTPMethod>) null);

  private final boolean m_bAsynchronous;
  private final long m_nTimeoutMillis;
  private final ICommonsList <AsyncListener> m_aAsyncListeners;
  private final EnumSet <EHTTPMethod> m_aAsyncHTTPMethods;

  /**
   * Constructor
   *
   * @param bAsynchronous
   *        <code>true</code> for asynchronous stuff, <code>false</code> for
   *        synchronous spec.
   * @param nTimeoutMillis
   *        The timeout in milliseconds. Must be &le; 0 for synchronous usage.
   * @param aAsyncListeners
   *        {@link AsyncListener}s to be added to the AsyncContext. Must be
   *        <code>null</code> for synchronous usage.
   * @param aAsyncHTTPMethods
   *        The HTTP methods that are available for async usage. May neither be
   *        <code>null</code> nor empty in async mode.
   */
  protected ServletAsyncSpec (final boolean bAsynchronous,
                              @CheckForSigned final long nTimeoutMillis,
                              @Nullable final Iterable <? extends AsyncListener> aAsyncListeners,
                              @Nullable final Set <EHTTPMethod> aAsyncHTTPMethods)
  {
    if (bAsynchronous)
    {
      ValueEnforcer.notEmptyNoNullValue (aAsyncHTTPMethods, "AsyncHTTPMethods");
    }
    else
    {
      ValueEnforcer.isLE0 (nTimeoutMillis, "TimeoutMillis");
      ValueEnforcer.isNull (aAsyncListeners, "AsyncListeners");
      ValueEnforcer.isNull (aAsyncHTTPMethods, "AsyncHTTPMethods");
    }
    m_bAsynchronous = bAsynchronous;
    m_nTimeoutMillis = nTimeoutMillis;
    m_aAsyncListeners = new CommonsArrayList <> (aAsyncListeners);
    m_aAsyncHTTPMethods = aAsyncHTTPMethods == null ? EnumSet.noneOf (EHTTPMethod.class)
                                                    : EnumSet.copyOf (aAsyncHTTPMethods);
  }

  /**
   * @return The timeout in milliseconds. Only value &gt; 0 are defined. Values
   *         &le; 0 must be ignored.
   */
  @CheckForSigned
  public long getTimeoutMillis ()
  {
    return m_nTimeoutMillis;
  }

  /**
   * @return <code>true</code> if a timeout is defined, <code>false</code>
   *         otherwise. Only asynchronous definitions can have timeouts.
   */
  public boolean hasTimeoutMillis ()
  {
    return m_nTimeoutMillis > 0;
  }

  /**
   * @return <code>true</code> for asynchronous, <code>false</code> for
   *         synchronous. If it is asynchronous the HTTP method is also a
   *         determinator for whether a request is to be handled asynchronously
   *         or not.
   * @see #isAsyncHTTPMethod(EHTTPMethod)
   */
  public boolean isAsynchronous ()
  {
    return m_bAsynchronous;
  }

  /**
   * @return A copy of the list of all {@link AsyncListener}. Only contains
   *         items if this is an asynchronous spec.
   */
  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <AsyncListener> getAllAsyncListeners ()
  {
    return m_aAsyncListeners.getClone ();
  }

  /**
   * @return <code>true</code> if any async listener is present,
   *         <code>false</code> otherwise.
   */
  public boolean hasAsyncListeners ()
  {
    return m_aAsyncListeners.isNotEmpty ();
  }

  /**
   * @return A set with all async HTTP methods. Only non-empty for asynchronous
   *         specs. Never <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableCopy
  public EnumSet <EHTTPMethod> getAllAsyncHTTPMethods ()
  {
    return EnumSet.copyOf (m_aAsyncHTTPMethods);
  }

  public boolean isAsyncHTTPMethod (@Nonnull final EHTTPMethod eHTTPMethod)
  {
    ValueEnforcer.notNull (eHTTPMethod, "HTTPMethod");
    return m_aAsyncHTTPMethods.contains (eHTTPMethod);
  }

  public void applyToAsyncContext (@Nonnull final AsyncContext aAsyncCtx)
  {
    if (!isAsynchronous ())
      throw new IllegalStateException ("This servlet is not declared asynchronous: " + toString ());
    if (hasAsyncListeners ())
      for (final AsyncListener aListener : m_aAsyncListeners)
        aAsyncCtx.addListener (aListener);
    if (hasTimeoutMillis ())
      aAsyncCtx.setTimeout (m_nTimeoutMillis);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Asynchronous", m_bAsynchronous)
                                       .appendIf ("TimeoutMillis", m_nTimeoutMillis, (final long x) -> x > 0)
                                       .append ("AsyncListeners", m_aAsyncListeners)
                                       .append ("AsyncHTTPMethods", m_aAsyncHTTPMethods)
                                       .getToString ();
  }

  /**
   * @return A synchronous spec. This always returns the constant value
   *         {@link #SYNC_SPEC}. Never <code>null</code>.
   */
  @Nonnull
  public static ServletAsyncSpec createSync ()
  {
    return SYNC_SPEC;
  }

  /**
   * Create an async spec.
   *
   * @param nTimeoutMillis
   *        Timeout in milliseconds. Only value &gt; 0 are considered.
   * @param aAsyncListeners
   *        The async listeners to use. May be <code>null</code>.
   * @param aAsyncHTTPMethods
   *        The HTTP methods that are available for async usage. May neither be
   *        <code>null</code> nor empty.
   * @return A new {@link ServletAsyncSpec} and never <code>null</code>.
   */
  @Nonnull
  public static ServletAsyncSpec createAsync (@CheckForSigned final long nTimeoutMillis,
                                              @Nullable final Iterable <? extends AsyncListener> aAsyncListeners,
                                              @Nonnull @Nonempty final Set <EHTTPMethod> aAsyncHTTPMethods)
  {
    return new ServletAsyncSpec (true, nTimeoutMillis, aAsyncListeners, aAsyncHTTPMethods);
  }

  @Nonnull
  public static ServletAsyncSpec createAsyncDefault ()
  {
    return createAsync (GlobalDebug.isDebugMode () ? 0 : 30 * CGlobal.MILLISECONDS_PER_SECOND,
                        null,
                        EnumSet.of (EHTTPMethod.GET, EHTTPMethod.HEAD));
  }
}

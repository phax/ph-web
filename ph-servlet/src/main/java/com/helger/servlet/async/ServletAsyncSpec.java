package com.helger.servlet.async;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.string.ToStringGenerator;

/**
 * This class encapsulates all the parameters necessary to handle asynchronous
 * servlet requests.
 *
 * @author Philip Helger
 * @since 8.7.5
 */
@Immutable
public class ServletAsyncSpec
{
  /** The constant defining "no timeout defined" */
  public static final long NO_TIMEOUT = 0L;
  /** The constant for synchronous invocations */
  public static final ServletAsyncSpec SYNC_SPEC = new ServletAsyncSpec (false, NO_TIMEOUT, null);

  private final boolean m_bAsynchronous;
  private final long m_nTimeoutMillis;
  private final ICommonsList <AsyncListener> m_aAsyncListeners;

  /**
   * Constructor
   *
   * @param bAsynchronous
   *        <code>true</code> for asynchronous stuff, <code>false</code> for
   *        synchronous spec.
   * @param aAsyncListeners
   *        {@link AsyncListener}s to be added to the AsyncContext. Must be
   *        <code>null</code> for synchronous usage.
   * @param nTimeoutMillis
   *        The timeout in milliseconds. Must be &le; 0 for synchronous usage.
   */
  protected ServletAsyncSpec (final boolean bAsynchronous,
                              @CheckForSigned final long nTimeoutMillis,
                              @Nullable final Iterable <? extends AsyncListener> aAsyncListeners)
  {
    if (!bAsynchronous)
    {
      ValueEnforcer.isLE0 (nTimeoutMillis, "TimeoutMillis");
      ValueEnforcer.isNull (aAsyncListeners, "AsyncListeners");
    }
    m_bAsynchronous = bAsynchronous;
    m_nTimeoutMillis = nTimeoutMillis;
    m_aAsyncListeners = new CommonsArrayList <> (aAsyncListeners);
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
   *         synchronous.
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
                                       .append ("AsyncListeners", m_aAsyncListeners)
                                       .appendIf ("TimeoutMillis", m_nTimeoutMillis, (final long x) -> x > 0)
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
   * @return A new {@link ServletAsyncSpec} and never <code>null</code>.
   */
  @Nonnull
  public static ServletAsyncSpec createAsync (@CheckForSigned final long nTimeoutMillis,
                                              @Nullable final Iterable <? extends AsyncListener> aAsyncListeners)
  {
    return new ServletAsyncSpec (true, nTimeoutMillis, aAsyncListeners);
  }

  /**
   * Create an async spec.
   *
   * @param nTimeoutMillis
   *        Timeout in milliseconds. Only value &gt; 0 are considered.
   * @param aAsyncListeners
   *        The async listeners to use. May be <code>null</code>.
   * @return A new {@link ServletAsyncSpec} and never <code>null</code>.
   */
  @Nonnull
  public static ServletAsyncSpec createAsync (@CheckForSigned final long nTimeoutMillis,
                                              @Nullable final AsyncListener... aAsyncListeners)
  {
    return createAsync (nTimeoutMillis, new CommonsArrayList <> (aAsyncListeners));
  }

  /**
   * Create an async spec without a timeout.
   *
   * @param aAsyncListeners
   *        The async listeners to use. May be <code>null</code>.
   * @return A new {@link ServletAsyncSpec} and never <code>null</code>.
   */
  @Nonnull
  public static ServletAsyncSpec createAsync (@Nullable final AsyncListener... aAsyncListeners)
  {
    return createAsync (NO_TIMEOUT, aAsyncListeners);
  }
}

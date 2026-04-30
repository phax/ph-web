/*
 * Copyright (C) 2020-2026 Philip Helger (www.helger.com)
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
package com.helger.dns.naptr;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.xbill.DNS.NAPTRRecord;

import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.equals.EqualsHelper;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;

/**
 * The outcome of a {@link NaptrLookup#lookupResult()} call: a status code, the (possibly empty)
 * list of NAPTR records, and an optional error message for diagnostics. This carries the
 * technical-vs-functional distinction (see {@link ENaptrLookupStatus}) that the legacy
 * {@link NaptrLookup#lookup()} list-only return type cannot express.
 *
 * @author Philip Helger
 * @since 11.4.0
 */
@Immutable
public class NaptrLookupResult
{
  private final ENaptrLookupStatus m_eStatus;
  private final ICommonsList <NAPTRRecord> m_aRecords;
  private final String m_sErrorMessage;

  public NaptrLookupResult (@NonNull final ENaptrLookupStatus eStatus,
                            @NonNull final ICommonsList <NAPTRRecord> aRecords,
                            @Nullable final String sErrorMessage)
  {
    ValueEnforcer.notNull (eStatus, "Status");
    ValueEnforcer.notNull (aRecords, "Records");

    m_eStatus = eStatus;
    m_aRecords = aRecords;
    m_sErrorMessage = sErrorMessage;
  }

  /**
   * @return The lookup status. Never <code>null</code>.
   */
  @NonNull
  public ENaptrLookupStatus getStatus ()
  {
    return m_eStatus;
  }

  /**
   * @return A mutable copy of the records list. Empty for non-{@link ENaptrLookupStatus#SUCCESSFUL}
   *         outcomes.
   */
  @NonNull
  @ReturnsMutableCopy
  public ICommonsList <NAPTRRecord> getRecords ()
  {
    return m_aRecords.getClone ();
  }

  /**
   * @return The dnsjava error message for non-success cases, or <code>null</code> otherwise.
   */
  @Nullable
  public String getErrorMessage ()
  {
    return m_sErrorMessage;
  }

  /**
   * @return Shortcut for <code>getStatus ().isSuccess ()</code>.
   */
  public boolean isSuccess ()
  {
    return m_eStatus.isSuccess ();
  }

  /**
   * @return Shortcut for <code>getStatus ().isFunctionalNotFound ()</code>.
   */
  public boolean isFunctionalNotFound ()
  {
    return m_eStatus.isFunctionalNotFound ();
  }

  /**
   * @return Shortcut for <code>getStatus ().isTechnicalFailure ()</code>.
   */
  public boolean isTechnicalFailure ()
  {
    return m_eStatus.isTechnicalFailure ();
  }

  /**
   * @return Shortcut for <code>getStatus ().isRetryable ()</code>.
   */
  public boolean isRetryable ()
  {
    return m_eStatus.isRetryable ();
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final NaptrLookupResult rhs = (NaptrLookupResult) o;
    return m_eStatus.equals (rhs.m_eStatus) &&
           m_aRecords.equals (rhs.m_aRecords) &&
           EqualsHelper.equals (m_sErrorMessage, rhs.m_sErrorMessage);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_eStatus).append (m_aRecords).append (m_sErrorMessage).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Status", m_eStatus)
                                       .append ("Records", m_aRecords)
                                       .appendIfNotNull ("ErrorMessage", m_sErrorMessage)
                                       .getToString ();
  }

  /**
   * Build a successful result.
   *
   * @param aRecords
   *        The NAPTR records returned by the lookup. May not be <code>null</code>, but may be
   *        empty.
   * @return A {@link NaptrLookupResult} with status {@link ENaptrLookupStatus#SUCCESSFUL}.
   */
  @NonNull
  public static NaptrLookupResult success (@NonNull final ICommonsList <NAPTRRecord> aRecords)
  {
    return new NaptrLookupResult (ENaptrLookupStatus.SUCCESSFUL, aRecords, null);
  }

  /**
   * Build a failure result.
   *
   * @param eStatus
   *        The non-success status. May not be <code>null</code> or
   *        {@link ENaptrLookupStatus#SUCCESSFUL}.
   * @param sErrorMessage
   *        The error message from dnsjava. May be <code>null</code>.
   * @return A {@link NaptrLookupResult} with the given status and an empty records list.
   */
  @NonNull
  public static NaptrLookupResult failure (@NonNull final ENaptrLookupStatus eStatus,
                                           @Nullable final String sErrorMessage)
  {
    ValueEnforcer.notNull (eStatus, "Status");
    ValueEnforcer.isFalse (eStatus.isSuccess (), "Status SUCCESSFUL is not allowed for failure ()");
    return new NaptrLookupResult (eStatus, new CommonsArrayList <> (), sErrorMessage);
  }
}

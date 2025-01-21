/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.smtp.util;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.email.EmailAddressHelper;

/**
 * Perform email address validations.
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class EmailAddressValidator
{
  public static final boolean DEFAULT_PERFORM_MX_RECORD_CHECK = false;

  private static final Logger LOGGER = LoggerFactory.getLogger (EmailAddressValidator.class);
  private static final AtomicBoolean PERFORM_MX_RECORD_CHECK = new AtomicBoolean (DEFAULT_PERFORM_MX_RECORD_CHECK);

  @PresentForCodeCoverage
  private static final EmailAddressValidator INSTANCE = new EmailAddressValidator ();

  private EmailAddressValidator ()
  {}

  /**
   * Set the global "check MX record" flag.
   *
   * @param bPerformMXRecordCheck
   *        <code>true</code> to enable, <code>false</code> otherwise.
   */
  public static void setPerformMXRecordCheck (final boolean bPerformMXRecordCheck)
  {
    PERFORM_MX_RECORD_CHECK.set (bPerformMXRecordCheck);

    LOGGER.info ("Email address record check is " + (bPerformMXRecordCheck ? "enabled" : "disabled"));
  }

  /**
   * @return <code>true</code> if global MX record checking is enabled
   */
  public static boolean isPerformMXRecordCheck ()
  {
    return PERFORM_MX_RECORD_CHECK.get ();
  }

  /**
   * Check if the passed host name has an MX record.
   *
   * @param sHostName
   *        The host name to check.
   * @return <code>true</code> if an MX record was found, <code>false</code> if
   *         not (or if an exception occurred)
   */
  private static boolean _hasMXRecord (@Nonnull final String sHostName)
  {
    try
    {
      final Record [] aRecords = new Lookup (sHostName, Type.MX).run ();
      return aRecords != null && aRecords.length > 0;
    }
    catch (final Exception ex)
    {
      // Do not log this message, as this method is potentially called very
      // often!
      LOGGER.warn ("Failed to check for MX record on host '" +
                   sHostName +
                   "': " +
                   ex.getClass ().getName () +
                   " - " +
                   ex.getMessage ());
      return false;
    }
  }

  /**
   * Checks if a value is a valid e-mail address. Depending on the global value
   * for the MX record check the check is performed incl. the MX record check or
   * without.
   *
   * @param sEmail
   *        The value validation is being performed on. A <code>null</code>
   *        value is considered invalid.
   * @return <code>true</code> if the email address is valid, <code>false</code>
   *         otherwise.
   * @see #isPerformMXRecordCheck()
   * @see #setPerformMXRecordCheck(boolean)
   */
  public static boolean isValid (@Nullable final String sEmail)
  {
    return PERFORM_MX_RECORD_CHECK.get () ? isValidWithMXCheck (sEmail) : EmailAddressHelper.isValid (sEmail);
  }

  /**
   * Checks if a value is a valid e-mail address according to a complex regular
   * expression. Additionally an MX record lookup is performed to see whether
   * this host provides SMTP services.
   *
   * @param sEmail
   *        The value validation is being performed on. A <code>null</code>
   *        value is considered invalid.
   * @return <code>true</code> if the email address is valid, <code>false</code>
   *         otherwise.
   */
  public static boolean isValidWithMXCheck (@Nullable final String sEmail)
  {
    // First check without MX
    if (!EmailAddressHelper.isValid (sEmail))
      return false;

    final String sUnifiedEmail = EmailAddressHelper.getUnifiedEmailAddress (sEmail);

    // MX record checking
    final int i = sUnifiedEmail.indexOf ('@');
    final String sHostName = sUnifiedEmail.substring (i + 1);
    return _hasMXRecord (sHostName);
  }
}

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
package com.helger.smtp.transport;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;

/**
 * This class encapsulates all details about a single email receiver
 *
 * @author Philip Helger
 */
@Immutable
public class MailSendDetails implements Serializable
{
  private final boolean m_bAddressValid;
  private final String m_sAddress;
  private final String m_sCommand;
  private final String m_sErrorMessage;
  private final ESMTPErrorCode m_eErrorCode;

  public MailSendDetails (final boolean bAddressValid,
                          @Nonnull final String sAddress,
                          @Nonnull final String sCommand,
                          @Nonnull final String sErrorMessage,
                          @Nonnull final ESMTPErrorCode eErrorCode)
  {
    m_bAddressValid = bAddressValid;
    m_sAddress = ValueEnforcer.notNull (sAddress, "Address");
    m_sCommand = ValueEnforcer.notNull (sCommand, "Command");
    m_sErrorMessage = ValueEnforcer.notNull (sErrorMessage, "ErrorMessage");
    m_eErrorCode = ValueEnforcer.notNull (eErrorCode, "ErrorCode");
  }

  /**
   * @return <code>true</code> if the address is valid, <code>false</code>
   *         otherwise. This does not state anything about whether the mail was
   *         sent or not!
   */
  public boolean isAddressValid ()
  {
    return m_bAddressValid;
  }

  /**
   * @return The email address that it's all about. Never <code>null</code>.
   */
  @Nonnull
  public String getAddress ()
  {
    return m_sAddress;
  }

  /**
   * @return The issued RFC 821 command. For techies only. Never
   *         <code>null</code>-.
   */
  @Nonnull
  public String getCommand ()
  {
    return m_sCommand;
  }

  /**
   * @return The error message returned from the server. Never <code>null</code>
   *         .
   */
  @Nonnull
  public String getErrorMessage ()
  {
    return m_sErrorMessage;
  }

  /**
   * @return The occurred SMTP error code. Never <code>null</code>.
   */
  @Nonnull
  public ESMTPErrorCode getErrorCode ()
  {
    return m_eErrorCode;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("AddressValid", m_bAddressValid)
                                       .append ("Address", m_sAddress)
                                       .append ("Command", m_sCommand)
                                       .append ("ErrorMessage", m_sErrorMessage)
                                       .append ("ErrorCode", m_eErrorCode)
                                       .getToString ();
  }
}

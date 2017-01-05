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
package com.helger.smtp.transport;

import java.util.Locale;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.id.IHasIntID;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.text.display.IHasDisplayText;

/**
 * See RFC 821 for details
 *
 * @author Philip Helger
 */
public enum ESMTPErrorCode implements IHasDisplayText, IHasIntID
{
  E211 (211, "System status, or system help reply"),
  E214 (214, "Help message [Information on how to use the receiver or the meaning of a particular non-standard command; this reply is useful only to the human user]"),
  E220 (220, "<domain> Service ready"),
  E221 (221, "<domain> Service closing transmission channel"),
  E250 (250, "Requested mail action okay, completed"),
  E251 (251, "User not local; will forward to <forward-path>"),
  E354 (354, "Start mail input; end with <CRLF>.<CRLF>"),
  E421 (421, "<domain> Service not available, closing transmission channel [This may be a reply to any command if the service knows it must shut down]"),
  E450 (450, "Requested mail action not taken: mailbox unavailable [E.g., mailbox busy]"),
  E451 (451, "Requested action aborted: error in processing"),
  E452 (452, "Requested action not taken: insufficient system storage"),
  E500 (500, "Syntax error, command unrecognized [This may include errors such as command line too long]"),
  E501 (501, "Syntax error in parameters or arguments"),
  E502 (502, "Command not implemented"),
  E503 (503, "Bad sequence of commands"),
  E504 (504, "Command parameter not implemented"),
  E550 (550, "Requested action not taken: mailbox unavailable [E.g., mailbox not found, no access]"),
  E551 (551, "User not local; please try <forward-path>"),
  E552 (552, "Requested mail action aborted: exceeded storage allocation"),
  E553 (553, "Requested action not taken: mailbox name not allowed [E.g., mailbox syntax incorrect]"),
  E554 (554, "Transaction failed"),
  FALLBACK (0, "Unknown error");

  private final int m_nECode;
  private final String m_sErrorMsg;

  private ESMTPErrorCode (@Nonnegative final int nECode, @Nonnull @Nonempty final String sErrorMsg)
  {
    m_nECode = nECode;
    m_sErrorMsg = sErrorMsg;
  }

  @Nonnegative
  public int getECode ()
  {
    return m_nECode;
  }

  @Nonnull
  @Nonempty
  public String getDisplayText (final Locale aContentLocale)
  {
    return m_sErrorMsg;
  }

  public int getID ()
  {
    return m_nECode;
  }

  @Nullable
  public static ESMTPErrorCode getFromIDOrNull (final int nECode)
  {
    return EnumHelper.getFromIDOrNull (ESMTPErrorCode.class, nECode);
  }

  @Nullable
  public static ESMTPErrorCode getFromIDOrDefault (final int nECode, @Nullable final ESMTPErrorCode eDefault)
  {
    return EnumHelper.getFromIDOrDefault (ESMTPErrorCode.class, nECode, eDefault);
  }
}

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
package com.helger.smtp.data;

import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.internet.InternetAddress;

import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.attr.IStringMap;
import com.helger.commons.email.EmailAddress;
import com.helger.commons.email.IEmailAddress;
import com.helger.mail.address.InternetAddressHelper;

/**
 * Contains all possible fields for mail sending.<br>
 * Note: the attribute container may only contain String values!
 *
 * @author Philip Helger
 */
public interface IMutableEmailData extends IEmailData
{
  @Nonnull
  IMutableEmailData setEmailType (@Nonnull EEmailType eType);

  @Nonnull
  default IMutableEmailData setFrom (@Nullable final String sFromAddress)
  {
    return setFrom (sFromAddress == null ? null : new EmailAddress (sFromAddress));
  }

  @Nonnull
  default IMutableEmailData setFrom (@Nullable final InternetAddress aFrom)
  {
    return setFrom (InternetAddressHelper.getAsEmailAddress (aFrom));
  }

  @Nonnull
  IMutableEmailData setFrom (@Nullable IEmailAddress aFrom);

  @Nonnull
  IMutableEmailData removeAllReplyTo ();

  @Nonnull
  default IMutableEmailData setReplyTo (@Nullable final String sReplyToAddress)
  {
    removeAllReplyTo ();
    return addReplyTo (sReplyToAddress);
  }

  @Nonnull
  default IMutableEmailData setReplyTo (@Nullable final InternetAddress aReplyTo)
  {
    removeAllReplyTo ();
    return addReplyTo (aReplyTo);
  }

  @Nonnull
  default IMutableEmailData setReplyTo (@Nullable final IEmailAddress aReplyTo)
  {
    removeAllReplyTo ();
    return addReplyTo (aReplyTo);
  }

  @Nonnull
  default IMutableEmailData setReplyTo (@Nullable final IEmailAddress... aReplyTos)
  {
    removeAllReplyTo ();
    return addReplyTo (aReplyTos);
  }

  @Nonnull
  default IMutableEmailData setReplyTo (@Nullable final List <? extends IEmailAddress> aReplyTos)
  {
    removeAllReplyTo ();
    return addReplyTo (aReplyTos);
  }

  @Nonnull
  default IMutableEmailData addReplyTo (@Nullable final String sReplyToAddress)
  {
    return addReplyTo (sReplyToAddress == null ? null : new EmailAddress (sReplyToAddress));
  }

  @Nonnull
  default IMutableEmailData addReplyTo (@Nullable final InternetAddress aReplyTo)
  {
    return addReplyTo (InternetAddressHelper.getAsEmailAddress (aReplyTo));
  }

  @Nonnull
  IMutableEmailData addReplyTo (@Nullable IEmailAddress aReplyTo);

  @Nonnull
  default IMutableEmailData addReplyTo (@Nullable final IEmailAddress... aReplyTos)
  {
    if (aReplyTos != null)
      for (final IEmailAddress aReplyTo : aReplyTos)
        addReplyTo (aReplyTo);
    return this;
  }

  @Nonnull
  default IMutableEmailData addReplyTo (@Nullable final List <? extends IEmailAddress> aReplyTos)
  {
    if (aReplyTos != null)
      for (final IEmailAddress aReplyTo : aReplyTos)
        addReplyTo (aReplyTo);
    return this;
  }

  @Nonnull
  IMutableEmailData removeAllTo ();

  @Nonnull
  default IMutableEmailData setTo (@Nullable final String sToAddress)
  {
    removeAllTo ();
    return addTo (sToAddress);
  }

  @Nonnull
  default IMutableEmailData setTo (@Nullable final InternetAddress aTo)
  {
    removeAllTo ();
    return addTo (aTo);
  }

  @Nonnull
  default IMutableEmailData setTo (@Nullable final IEmailAddress aTo)
  {
    removeAllTo ();
    return addTo (aTo);
  }

  @Nonnull
  default IMutableEmailData setTo (@Nullable final IEmailAddress... aTos)
  {
    removeAllTo ();
    return addTo (aTos);
  }

  @Nonnull
  default IMutableEmailData setTo (@Nullable final List <? extends IEmailAddress> aTos)
  {
    removeAllTo ();
    return addTo (aTos);
  }

  @Nonnull
  default IMutableEmailData addTo (@Nullable final String sToAddress)
  {
    return addTo (sToAddress == null ? null : new EmailAddress (sToAddress));
  }

  @Nonnull
  default IMutableEmailData addTo (@Nullable final InternetAddress aTo)
  {
    return addTo (InternetAddressHelper.getAsEmailAddress (aTo));
  }

  @Nonnull
  IMutableEmailData addTo (@Nullable IEmailAddress aTo);

  @Nonnull
  default IMutableEmailData addTo (@Nullable final IEmailAddress... aTos)
  {
    if (aTos != null)
      for (final IEmailAddress aTo : aTos)
        addTo (aTo);
    return this;
  }

  @Nonnull
  default IMutableEmailData addTo (@Nullable final List <? extends IEmailAddress> aTos)
  {
    if (aTos != null)
      for (final IEmailAddress aTo : aTos)
        addTo (aTo);
    return this;
  }

  @Nonnull
  IMutableEmailData removeAllCc ();

  @Nonnull
  default IMutableEmailData setCc (@Nullable final String sCc)
  {
    removeAllCc ();
    return addCc (sCc);
  }

  @Nonnull
  default IMutableEmailData setCc (@Nullable final InternetAddress aCc)
  {
    removeAllCc ();
    return addCc (aCc);
  }

  @Nonnull
  default IMutableEmailData setCc (@Nullable final IEmailAddress aCc)
  {
    removeAllCc ();
    return addCc (aCc);
  }

  @Nonnull
  default IMutableEmailData setCc (@Nullable final IEmailAddress... aCcs)
  {
    removeAllCc ();
    return addCc (aCcs);
  }

  @Nonnull
  default IMutableEmailData setCc (@Nullable final List <? extends IEmailAddress> aCcs)
  {
    removeAllCc ();
    return addCc (aCcs);
  }

  @Nonnull
  default IMutableEmailData addCc (@Nullable final String sCc)
  {
    return addCc (sCc == null ? null : new EmailAddress (sCc));
  }

  @Nonnull
  default IMutableEmailData addCc (@Nullable final InternetAddress aCc)
  {
    return addCc (InternetAddressHelper.getAsEmailAddress (aCc));
  }

  @Nonnull
  IMutableEmailData addCc (@Nullable IEmailAddress aCc);

  @Nonnull
  default IMutableEmailData addCc (@Nullable final IEmailAddress... aCcs)
  {
    if (aCcs != null)
      for (final IEmailAddress aCc : aCcs)
        addCc (aCc);
    return this;
  }

  @Nonnull
  default IMutableEmailData addCc (@Nullable final List <? extends IEmailAddress> aCcs)
  {
    if (aCcs != null)
      for (final IEmailAddress aCc : aCcs)
        addCc (aCc);
    return this;
  }

  @Nonnull
  IMutableEmailData removeAllBcc ();

  @Nonnull
  default IMutableEmailData setBcc (@Nullable final String sBcc)
  {
    removeAllBcc ();
    return addBcc (sBcc);
  }

  @Nonnull
  default IMutableEmailData setBcc (@Nullable final InternetAddress aBcc)
  {
    removeAllBcc ();
    return addBcc (aBcc);
  }

  @Nonnull
  default IMutableEmailData setBcc (@Nullable final IEmailAddress aBcc)
  {
    removeAllBcc ();
    return addBcc (aBcc);
  }

  @Nonnull
  default IMutableEmailData setBcc (@Nullable final IEmailAddress... aBccs)
  {
    removeAllBcc ();
    return addBcc (aBccs);
  }

  @Nonnull
  default IMutableEmailData setBcc (@Nullable final List <? extends IEmailAddress> aBccs)
  {
    removeAllBcc ();
    return addBcc (aBccs);
  }

  @Nonnull
  default IMutableEmailData addBcc (@Nullable final String sBcc)
  {
    return addBcc (sBcc == null ? null : new EmailAddress (sBcc));
  }

  @Nonnull
  default IMutableEmailData addBcc (@Nullable final InternetAddress aBcc)
  {
    return addBcc (InternetAddressHelper.getAsEmailAddress (aBcc));
  }

  @Nonnull
  IMutableEmailData addBcc (@Nullable IEmailAddress aBcc);

  @Nonnull
  default IMutableEmailData addBcc (@Nullable final IEmailAddress... aBccs)
  {
    if (aBccs != null)
      for (final IEmailAddress aBcc : aBccs)
        addBcc (aBcc);
    return this;
  }

  @Nonnull
  default IMutableEmailData addBcc (@Nullable final List <? extends IEmailAddress> aBccs)
  {
    if (aBccs != null)
      for (final IEmailAddress aBcc : aBccs)
        addBcc (aBcc);
    return this;
  }

  @Nonnull
  IMutableEmailData setSentDateTime (@Nullable LocalDateTime aDateTime);

  @Nonnull
  IMutableEmailData setSubject (@Nullable String sSubject);

  @Nonnull
  IMutableEmailData setBody (@Nullable String sBody);

  @Nullable
  IMutableEmailAttachmentList getAttachments ();

  /**
   * @return The number of contained attachments. Always &ge; 0.
   */
  @Nonnegative
  int getAttachmentCount ();

  /**
   * Specify a set of attachments to be send together with the mail. Pass
   * <code>null</code> to indicate that no attachments are desired (this is the
   * default).
   *
   * @param aAttachments
   *        The attachments to be used. May be <code>null</code> or empty.
   * @return this
   */
  @Nonnull
  IMutableEmailData setAttachments (@Nullable IEmailAttachmentList aAttachments);

  @Nonnull
  @ReturnsMutableObject
  IStringMap customAttrs ();
}

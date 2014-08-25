/**
 * Copyright (C) 2006-2014 phloc systems (www.phloc.com)
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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
package com.helger.web.smtp.failed;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.microdom.IMicroElement;
import com.helger.commons.microdom.convert.IMicroTypeConverter;
import com.helger.commons.microdom.convert.MicroTypeConverter;
import com.helger.commons.microdom.impl.MicroElement;
import com.helger.commons.microdom.utils.MicroUtils;
import com.helger.commons.string.StringHelper;
import com.helger.datetime.config.PDTConfig;
import com.helger.web.datetime.PDTWebDateUtils;
import com.helger.web.smtp.IEmailData;
import com.helger.web.smtp.ISMTPSettings;
import com.helger.web.smtp.impl.EmailData;
import com.helger.web.smtp.impl.SMTPSettings;

public final class FailedMailDataMicroTypeConverter implements IMicroTypeConverter
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (FailedMailDataMicroTypeConverter.class);
  private static final String ATTR_ID = "id";
  private static final String ATTR_ERRORDT = "errordt";
  private static final String ATTR_ORIGINALSENTDT = "originalsentdt";
  private static final String ELEMENT_SMTPSETTINGS = "smtpsettings";
  private static final String ELEMENT_EMAILDATA = "emaildata";
  private static final String ELEMENT_ERRORMSG = "errormsg";

  @Nonnull
  public IMicroElement convertToMicroElement (@Nonnull final Object aSource,
                                              @Nullable final String sNamespaceURI,
                                              @Nonnull final String sTagName)
  {
    final FailedMailData aFailedMail = (FailedMailData) aSource;
    final IMicroElement eFailedMail = new MicroElement (sNamespaceURI, sTagName);

    eFailedMail.setAttribute (ATTR_ID, aFailedMail.getID ());
    eFailedMail.setAttribute (ATTR_ERRORDT, PDTWebDateUtils.getAsStringXSD (aFailedMail.getErrorDateTime ()));

    if (aFailedMail.getOriginalSentDateTime () != null)
      eFailedMail.setAttribute (ATTR_ORIGINALSENTDT,
                                PDTWebDateUtils.getAsStringXSD (aFailedMail.getOriginalSentDateTime ()));

    // SMTP settings
    eFailedMail.appendChild (MicroTypeConverter.convertToMicroElement (aFailedMail.getSMTPSettings (),
                                                                       sNamespaceURI,
                                                                       ELEMENT_SMTPSETTINGS));

    // email data
    eFailedMail.appendChild (MicroTypeConverter.convertToMicroElement (aFailedMail.getEmailData (),
                                                                       sNamespaceURI,
                                                                       ELEMENT_EMAILDATA));

    if (StringHelper.hasText (aFailedMail.getMessageDisplayText ()))
      eFailedMail.appendElement (sNamespaceURI, ELEMENT_ERRORMSG).appendText (aFailedMail.getMessageDisplayText ());
    return eFailedMail;
  }

  @Nullable
  public FailedMailData convertToNative (@Nonnull final IMicroElement eFailedMail)
  {
    final String sID = eFailedMail.getAttribute (ATTR_ID);
    if (sID == null)
    {
      s_aLogger.error ("Failed to read ID");
      return null;
    }

    // Read error date/time
    final String sErrorDT = eFailedMail.getAttribute (ATTR_ERRORDT);
    if (sErrorDT == null)
    {
      s_aLogger.error ("Failed to read error date/time");
      return null;
    }
    LocalDateTime aErrorDT = PDTWebDateUtils.getLocalDateTimeFromXSD (sErrorDT);
    if (aErrorDT == null)
      aErrorDT = PDTWebDateUtils.getLocalDateTimeFromW3C (sErrorDT);
    if (aErrorDT == null)
    {
      s_aLogger.error ("Failed to parse error date '" + sErrorDT + "'");
      return null;
    }

    // read original sent date/time
    final String sOriginalSentDT = eFailedMail.getAttribute (ATTR_ORIGINALSENTDT);
    DateTime aOriginalSentDT = null;
    if (sOriginalSentDT != null)
    {
      aOriginalSentDT = PDTWebDateUtils.getDateTimeFromXSD (sOriginalSentDT);
      if (aOriginalSentDT == null)
        aOriginalSentDT = PDTWebDateUtils.getDateTimeFromW3C (sOriginalSentDT);
    }
    if (aOriginalSentDT != null)
      aOriginalSentDT = aOriginalSentDT.withChronology (PDTConfig.getDefaultChronology ());

    // SMTP settings
    final IMicroElement eSMTPSettings = eFailedMail.getFirstChildElement (ELEMENT_SMTPSETTINGS);
    if (eSMTPSettings == null)
    {
      s_aLogger.error ("Failed to get child element of SMTP settings!");
      return null;
    }
    final ISMTPSettings aSMTPSettings = MicroTypeConverter.convertToNative (eSMTPSettings, SMTPSettings.class);

    // email data (may be null)
    final IMicroElement eEmailData = eFailedMail.getFirstChildElement (ELEMENT_EMAILDATA);
    final IEmailData aEmailData = MicroTypeConverter.convertToNative (eEmailData, EmailData.class);

    // error message
    final String sErrorMessage = MicroUtils.getChildTextContent (eFailedMail, ELEMENT_ERRORMSG);
    final Exception aError = StringHelper.hasNoText (sErrorMessage) ? null : new Exception (sErrorMessage);

    return new FailedMailData (sID, aErrorDT, aSMTPSettings, aOriginalSentDT, aEmailData, aError);
  }
}

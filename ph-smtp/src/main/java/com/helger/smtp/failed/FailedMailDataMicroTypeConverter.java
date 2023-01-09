/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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
package com.helger.smtp.failed;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.datetime.PDTWebDateHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;
import com.helger.commons.typeconvert.TypeConverter;
import com.helger.smtp.data.EmailData;
import com.helger.smtp.data.IMutableEmailData;
import com.helger.smtp.settings.ISMTPSettings;
import com.helger.smtp.settings.SMTPSettings;
import com.helger.smtp.transport.ESMTPErrorCode;
import com.helger.smtp.transport.MailSendDetails;
import com.helger.smtp.transport.MailTransportError;
import com.helger.xml.microdom.IMicroElement;
import com.helger.xml.microdom.MicroElement;
import com.helger.xml.microdom.convert.IMicroTypeConverter;
import com.helger.xml.microdom.convert.MicroTypeConverter;
import com.helger.xml.microdom.util.MicroHelper;

/**
 * Micro type converter for class {@link FailedMailData}.
 *
 * @author Philip Helger
 */
public class FailedMailDataMicroTypeConverter implements IMicroTypeConverter <FailedMailData>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (FailedMailDataMicroTypeConverter.class);
  private static final String ATTR_ID = "id";
  private static final String ATTR_ERRORDT = "errordt";
  private static final String ATTR_ORIGINALSENT_DT = "originalsentdt";
  private static final String ELEMENT_SMTP_SETTINGS = "smtpsettings";
  private static final String ELEMENT_EMAIL_DATA = "emaildata";
  private static final String ELEMENT_ERROR_MSG = "errormsg";
  private static final String ELEMENT_DETAILS = "details";
  private static final String ATTR_ADDRESS_VALID = "addressvalid";
  private static final String ATTR_ADDRESS = "address";
  private static final String ATTR_COMMAND = "command";
  private static final String ATTR_ERROR_MESSAGE = "errormsg";
  private static final String ATTR_ERROR_CODE = "errcode";

  @Nonnull
  public IMicroElement convertToMicroElement (@Nonnull final FailedMailData aFailedMail,
                                              @Nullable final String sNamespaceURI,
                                              @Nonnull final String sTagName)
  {
    final IMicroElement eFailedMail = new MicroElement (sNamespaceURI, sTagName);

    eFailedMail.setAttribute (ATTR_ID, aFailedMail.getID ());
    eFailedMail.setAttributeWithConversion (ATTR_ERRORDT, aFailedMail.getErrorDateTime ());
    eFailedMail.setAttributeWithConversion (ATTR_ORIGINALSENT_DT, aFailedMail.getOriginalSentDateTime ());

    // SMTP settings
    eFailedMail.appendChild (MicroTypeConverter.convertToMicroElement (aFailedMail.getSMTPSettings (),
                                                                       sNamespaceURI,
                                                                       ELEMENT_SMTP_SETTINGS));

    // email data
    eFailedMail.appendChild (MicroTypeConverter.convertToMicroElement (aFailedMail.getEmailData (), sNamespaceURI, ELEMENT_EMAIL_DATA));

    final MailTransportError aTransportError = aFailedMail.getTransportError ();
    if (aTransportError != null)
    {
      eFailedMail.appendElement (sNamespaceURI, ELEMENT_ERROR_MSG).appendText (aTransportError.getThrowable ().getMessage ());
      for (final MailSendDetails aDetails : aTransportError.getAllDetails ())
      {
        eFailedMail.appendElement (sNamespaceURI, ELEMENT_DETAILS)
                   .setAttribute (ATTR_ADDRESS_VALID, aDetails.isAddressValid ())
                   .setAttribute (ATTR_ADDRESS, aDetails.getAddress ())
                   .setAttribute (ATTR_COMMAND, aDetails.getCommand ())
                   .setAttribute (ATTR_ERROR_MESSAGE, aDetails.getErrorMessage ())
                   .setAttribute (ATTR_ERROR_CODE, aDetails.getErrorCode ().getECode ());
      }
    }
    return eFailedMail;
  }

  @Nullable
  public FailedMailData convertToNative (@Nonnull final IMicroElement eFailedMail)
  {
    final String sID = eFailedMail.getAttributeValue (ATTR_ID);
    if (sID == null)
    {
      LOGGER.error ("Failed to read ID");
      return null;
    }

    // Read error date/time
    final String sErrorDT = eFailedMail.getAttributeValue (ATTR_ERRORDT);
    if (sErrorDT == null)
    {
      LOGGER.error ("Failed to read error date/time");
      return null;
    }
    LocalDateTime aErrorDT = PDTWebDateHelper.getLocalDateTimeFromXSD (sErrorDT);
    if (aErrorDT == null)
      aErrorDT = TypeConverter.convert (sErrorDT, LocalDateTime.class);
    if (aErrorDT == null)
    {
      if (LOGGER.isErrorEnabled ())
        LOGGER.error ("Failed to parse error date '" + sErrorDT + "'");
      return null;
    }

    // read original sent date/time
    final String sOriginalSentDT = eFailedMail.getAttributeValue (ATTR_ORIGINALSENT_DT);
    LocalDateTime aOriginalSentDT = null;
    if (sOriginalSentDT != null)
    {
      aOriginalSentDT = PDTWebDateHelper.getLocalDateTimeFromXSD (sOriginalSentDT);
      if (aOriginalSentDT == null)
        aOriginalSentDT = TypeConverter.convert (sOriginalSentDT, LocalDateTime.class);
    }

    // SMTP settings
    final IMicroElement eSMTPSettings = eFailedMail.getFirstChildElement (ELEMENT_SMTP_SETTINGS);
    if (eSMTPSettings == null)
    {
      LOGGER.error ("Failed to get child element of SMTP settings!");
      return null;
    }
    final ISMTPSettings aSMTPSettings = MicroTypeConverter.convertToNative (eSMTPSettings, SMTPSettings.class);

    // email data (may be null)
    final IMicroElement eEmailData = eFailedMail.getFirstChildElement (ELEMENT_EMAIL_DATA);
    final IMutableEmailData aEmailData = MicroTypeConverter.convertToNative (eEmailData, EmailData.class);

    // error message
    final String sErrorMessage = MicroHelper.getChildTextContent (eFailedMail, ELEMENT_ERROR_MSG);
    final Exception aException = StringHelper.hasNoText (sErrorMessage) ? null : new Exception (sErrorMessage);
    MailTransportError aError = null;
    if (aException != null)
    {
      final ICommonsList <MailSendDetails> aDetails = new CommonsArrayList <> ();
      for (final IMicroElement eDetails : eFailedMail.getAllChildElements (ELEMENT_DETAILS))
      {
        final boolean bAddressValid = StringParser.parseBool (eDetails.getAttributeValue (ATTR_ADDRESS_VALID));
        final String sAddress = eDetails.getAttributeValue (ATTR_ADDRESS);
        final String sCommand = eDetails.getAttributeValue (ATTR_COMMAND);
        final String sDetailsErrorMessage = eDetails.getAttributeValue (ATTR_ERROR_MESSAGE);
        final int nErrorCode = StringParser.parseInt (eDetails.getAttributeValue (ATTR_ERROR_CODE), -1);
        final ESMTPErrorCode eErrorCode = ESMTPErrorCode.getFromIDOrDefault (nErrorCode, ESMTPErrorCode.FALLBACK);
        aDetails.add (new MailSendDetails (bAddressValid, sAddress, sCommand, sDetailsErrorMessage, eErrorCode));
      }
      aError = new MailTransportError (aException, aDetails);
    }

    return new FailedMailData (sID, aErrorDT, aSMTPSettings, aOriginalSentDT, aEmailData, aError);
  }
}

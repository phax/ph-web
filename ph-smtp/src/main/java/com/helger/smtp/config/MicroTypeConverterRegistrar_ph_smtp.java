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
package com.helger.smtp.config;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.IsSPIImplementation;
import com.helger.smtp.data.EmailAttachment;
import com.helger.smtp.data.EmailAttachmentList;
import com.helger.smtp.data.EmailAttachmentListMicroTypeConverter;
import com.helger.smtp.data.EmailAttachmentMicroTypeConverter;
import com.helger.smtp.data.EmailData;
import com.helger.smtp.data.EmailDataMicroTypeConverter;
import com.helger.smtp.failed.FailedMailData;
import com.helger.smtp.failed.FailedMailDataMicroTypeConverter;
import com.helger.smtp.settings.SMTPSettings;
import com.helger.smtp.settings.SMTPSettingsMicroTypeConverter;
import com.helger.xml.microdom.convert.IMicroTypeConverterRegistrarSPI;
import com.helger.xml.microdom.convert.IMicroTypeConverterRegistry;

/**
 * Register all MicroTypeConverter implementations of this project.
 *
 * @author Philip Helger
 */
@Immutable
@IsSPIImplementation
public final class MicroTypeConverterRegistrar_ph_smtp implements IMicroTypeConverterRegistrarSPI
{
  public void registerMicroTypeConverter (@NonNull final IMicroTypeConverterRegistry aRegistry)
  {
    aRegistry.registerMicroElementTypeConverter (EmailAttachment.class, new EmailAttachmentMicroTypeConverter ());
    aRegistry.registerMicroElementTypeConverter (EmailAttachmentList.class, new EmailAttachmentListMicroTypeConverter ());
    aRegistry.registerMicroElementTypeConverter (EmailData.class, new EmailDataMicroTypeConverter ());
    aRegistry.registerMicroElementTypeConverter (FailedMailData.class, new FailedMailDataMicroTypeConverter ());
    aRegistry.registerMicroElementTypeConverter (SMTPSettings.class, new SMTPSettingsMicroTypeConverter ());
  }
}

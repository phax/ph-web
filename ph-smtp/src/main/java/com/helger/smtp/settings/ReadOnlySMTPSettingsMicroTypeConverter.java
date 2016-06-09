/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.smtp.settings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.ContainsSoftMigration;
import com.helger.xml.microdom.IMicroElement;
import com.helger.xml.microdom.convert.IMicroTypeConverter;

public final class ReadOnlySMTPSettingsMicroTypeConverter implements IMicroTypeConverter
{
  @Nonnull
  public IMicroElement convertToMicroElement (@Nonnull final Object aSource,
                                              @Nullable final String sNamespaceURI,
                                              @Nonnull final String sTagName)
  {
    final ISMTPSettings aSMTPSettings = (ISMTPSettings) aSource;
    return SMTPSettingsMicroTypeConverter.convertToMicroElement (aSMTPSettings, sNamespaceURI, sTagName);
  }

  @Nonnull
  @ContainsSoftMigration
  public ReadOnlySMTPSettings convertToNative (@Nonnull final IMicroElement eSMTPSettings)
  {
    final SMTPSettings aSettings = SMTPSettingsMicroTypeConverter.convertToSMTPSettings (eSMTPSettings);
    return new ReadOnlySMTPSettings (aSettings);
  }
}

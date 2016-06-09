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
package com.helger.mail.config;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.mail.internet.InternetAddress;

import com.helger.commons.annotation.IsSPIImplementation;
import com.helger.commons.email.EmailAddress;
import com.helger.commons.typeconvert.ITypeConverterRegistrarSPI;
import com.helger.commons.typeconvert.ITypeConverterRegistry;

/**
 * Register all TypeConverter implementations of this project.
 *
 * @author Philip Helger
 */
@Immutable
@IsSPIImplementation
public final class TypeConverterRegistrar_ph_mail implements ITypeConverterRegistrarSPI
{
  public void registerTypeConverter (@Nonnull final ITypeConverterRegistry aRegistry)
  {
    aRegistry.registerTypeConverter (InternetAddress.class,
                                     EmailAddress.class,
                                     aSource -> new EmailAddress (aSource.getAddress (), aSource.getPersonal ()));
  }
}

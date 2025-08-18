/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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
package com.helger.httpclient.config;

import com.helger.annotation.style.IsSPIImplementation;
import com.helger.base.thirdparty.ELicense;
import com.helger.base.thirdparty.IThirdPartyModule;
import com.helger.base.thirdparty.IThirdPartyModuleProviderSPI;
import com.helger.base.thirdparty.ThirdPartyModule;
import com.helger.base.version.Version;

import jakarta.annotation.Nullable;

/**
 * Implement this SPI interface if your JAR file contains external third party modules.
 *
 * @author Philip Helger
 */
@IsSPIImplementation
public final class ThirdPartyModuleProvider_ph_httpclient implements IThirdPartyModuleProviderSPI
{
  public static final IThirdPartyModule HTTP_CORE = new ThirdPartyModule ("Apache HttpComponents Core",
                                                                          "Apache",
                                                                          ELicense.APACHE2,
                                                                          new Version (5, 3, 4),
                                                                          "https://hc.apache.org/");
  public static final IThirdPartyModule HTTP_CLIENT = new ThirdPartyModule ("Apache HttpComponents Client",
                                                                            "Apache",
                                                                            ELicense.APACHE2,
                                                                            new Version (5, 5),
                                                                            "https://hc.apache.org/");

  @Nullable
  public IThirdPartyModule [] getAllThirdPartyModules ()
  {
    return new IThirdPartyModule [] { HTTP_CORE, HTTP_CLIENT };
  }
}

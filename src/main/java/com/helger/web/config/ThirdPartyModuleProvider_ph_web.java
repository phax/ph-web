/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.config;

import javax.annotation.Nullable;

import com.helger.commons.annotations.IsSPIImplementation;
import com.helger.commons.thirdparty.ELicense;
import com.helger.commons.thirdparty.IThirdPartyModule;
import com.helger.commons.thirdparty.IThirdPartyModuleProviderSPI;
import com.helger.commons.thirdparty.ThirdPartyModule;
import com.helger.commons.version.Version;

/**
 * Implement this SPI interface if your JAR file contains external third party
 * modules.
 *
 * @author Philip Helger
 */
@IsSPIImplementation
public final class ThirdPartyModuleProvider_ph_web implements IThirdPartyModuleProviderSPI
{
  public static final IThirdPartyModule DNSJAVA = new ThirdPartyModule ("dnsjava",
                                                                        "Brian Wellington",
                                                                        ELicense.BSD,
                                                                        new Version (2, 1, 6),
                                                                        "http://www.xbill.org/dnsjava/",
                                                                        true);

  public static final IThirdPartyModule RHINO = new ThirdPartyModule ("Rhino",
                                                                      "Mozilla",
                                                                      ELicense.MPL20,
                                                                      new Version ("1.7.6"),
                                                                      "https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino",
                                                                      true);

  public static final IThirdPartyModule JSCH = new ThirdPartyModule ("JSch",
                                                                     "JCraft Inc.",
                                                                     ELicense.BSD,
                                                                     new Version (0, 1, 53),
                                                                     "http://www.jcraft.com/jsch/",
                                                                     true);

  @Nullable
  public IThirdPartyModule [] getAllThirdPartyModules ()
  {
    return new IThirdPartyModule [] { DNSJAVA, RHINO, JSCH };
  }
}

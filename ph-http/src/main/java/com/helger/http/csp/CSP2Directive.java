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
package com.helger.http.csp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;

/**
 * A single CSP 2.0 directive. It's a name-value-pair.
 *
 * @author Philip Helger
 * @deprecated Use {@link CSPDirective} directly
 */
@Deprecated (forRemoval = true, since = "10.4.0")
public class CSP2Directive extends CSPDirective
{
  public CSP2Directive (@Nonnull @Nonempty final String sName, @Nullable final AbstractCSPSourceList <?> aValue)
  {
    super (sName, aValue);
  }

  public CSP2Directive (@Nonnull @Nonempty final String sName, @Nullable final String sValue)
  {
    super (sName, sValue);
  }
}

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
package com.helger.http.csp;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * CSP 2.0 policy. It's a list of {@link CSP2Directive}.<br>
 * See http://www.w3.org/TR/CSP2/
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class CSP2Policy extends AbstractCSPPolicy <CSP2Directive>
{
  public CSP2Policy ()
  {}
}

/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.helger.security.messagedigest.EMessageDigestAlgorithm;

/**
 * Test class for class {@link CSP2Policy}.
 *
 * @author Philip Helger
 */
public final class CSP2PolicyTest
{
  @Test
  public void testBasic ()
  {
    assertEquals ("default-src 'self'",
                  new CSP2Policy ().addDirective (CSP2Directive.createDefaultSrc (new CSP2SourceList ().addKeywordSelf ()))
                                   .getAsString ());
    assertEquals ("default-src 'self'; img-src 'sha256-abc' 'nonce-123'; object-src media1.example.com media2.example.com *.cdn.example.com; script-src trustedscripts.example.com",
                  new CSP2Policy ().addDirective (CSP2Directive.createDefaultSrc (new CSP2SourceList ().addKeywordSelf ()))
                                   .addDirective (CSP2Directive.createImgSrc (new CSP2SourceList ().addHash (EMessageDigestAlgorithm.SHA_256,
                                                                                                             "abc")
                                                                                                   .addNonce ("123")))
                                   .addDirective (CSP2Directive.createObjectSrc (new CSP2SourceList ().addHost ("media1.example.com")
                                                                                                      .addHost ("media2.example.com")
                                                                                                      .addHost ("*.cdn.example.com")))
                                   .addDirective (CSP2Directive.createScriptSrc (new CSP2SourceList ().addHost ("trustedscripts.example.com")))
                                   .getAsString ());
    assertEquals ("default-src https: 'unsafe-inline' 'unsafe-eval'",
                  new CSP2Policy ().addDirective (CSP2Directive.createDefaultSrc (new CSP2SourceList ().addScheme ("https:")
                                                                                                       .addKeywordUnsafeInline ()
                                                                                                       .addKeywordUnsafeEval ()))
                                   .getAsString ());
    assertEquals ("default-src 'self'; script-src https://example.com/js/",
                  new CSP2Policy ().addDirective (CSP2Directive.createDefaultSrc (new CSP2SourceList ().addKeywordSelf ()))
                                   .addDirective (CSP2Directive.createScriptSrc (new CSP2SourceList ().addHost ("https://example.com/js/")))
                                   .getAsString ());
    assertEquals ("default-src 'none'",
                  new CSP2Policy ().addDirective (CSP2Directive.createDefaultSrc (new CSP2SourceList ().addKeywordNone ()))
                                   .addDirective (CSP2Directive.createScriptSrc (new CSP2SourceList ()))
                                   .getAsString ());
    assertEquals ("",
                  new CSP2Policy ().addDirective (CSP2Directive.createDefaultSrc (new CSP2SourceList ()))
                                   .addDirective (CSP2Directive.createScriptSrc (new CSP2SourceList ()))
                                   .getAsString ());
  }
}

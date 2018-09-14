/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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
package com.helger.web.scope.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Test class for class {@link RequestWebScope}
 *
 * @author Philip Helger
 */
public final class RequestWebScopeTest
{
  @Test
  public void testGetWithoutForbiddenChars ()
  {
    assertNull (RequestWebScope.getWithoutForbiddenChars (null));
    assertEquals ("", RequestWebScope.getWithoutForbiddenChars (""));
    assertEquals ("abc", RequestWebScope.getWithoutForbiddenChars ("abc"));
    assertEquals (" abc ", RequestWebScope.getWithoutForbiddenChars (" abc "));
    // Cut out 0x00
    assertEquals (" abc ", RequestWebScope.getWithoutForbiddenChars (" ab\u0000c "));
    // Cut out 0x0e
    assertEquals (" abc ", RequestWebScope.getWithoutForbiddenChars (" ab\u000ec "));
    // Cut out all
    assertEquals ("",
                  RequestWebScope.getWithoutForbiddenChars ("\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008" +
                                                            "\u000b\u000c" +
                                                            "\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f" +
                                                            "\ud800\udfff" +
                                                            "\ufffe\uffff"));
    assertEquals ("x",
                  RequestWebScope.getWithoutForbiddenChars ("\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008" +
                                                            "\u000b\u000c" +
                                                            "\u000e\u000f\u0010\u0011\u0012x\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f" +
                                                            "\ud800\udfff" +
                                                            "\ufffe\uffff"));
  }
}

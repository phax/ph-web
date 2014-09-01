/**
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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
package com.helger.web.http;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.equals.EqualsUtils;

public final class AcceptCharsetHandlerTest
{
  @Test
  public void testSimple ()
  {
    final AcceptCharsetList c = AcceptCharsetHandler.getAcceptCharsets ("UTF-8");
    assertNotNull (c);
    // Explicitly contained
    assertTrue (EqualsUtils.equals (1d, c.getQualityOfCharset ("UTF-8")));
    // Not contained
    assertTrue (EqualsUtils.equals (0d, c.getQualityOfCharset ("ISO-8859-15")));
    // Default charset
    assertTrue (EqualsUtils.equals (1d, c.getQualityOfCharset ("ISO-8859-1")));
  }

  @Test
  public void testSimpleWithQuality ()
  {
    final AcceptCharsetList c = AcceptCharsetHandler.getAcceptCharsets ("UTF-8;q=0.5");
    assertNotNull (c);
    // Explicitly contained
    assertTrue (EqualsUtils.equals (0.5d, c.getQualityOfCharset ("UTF-8")));
    // Not contained
    assertTrue (EqualsUtils.equals (0d, c.getQualityOfCharset ("ISO-8859-15")));
    // Default charset
    assertTrue (EqualsUtils.equals (1d, c.getQualityOfCharset ("ISO-8859-1")));
  }

  @Test
  public void testSimpleWithAll ()
  {
    final AcceptCharsetList c = AcceptCharsetHandler.getAcceptCharsets ("UTF-8,*");
    assertNotNull (c);
    // Explicitly contained
    assertTrue (EqualsUtils.equals (1d, c.getQualityOfCharset ("UTF-8")));
    // Not contained
    assertTrue (EqualsUtils.equals (1d, c.getQualityOfCharset ("ISO-8859-15")));
    // Default charset
    assertTrue (EqualsUtils.equals (1d, c.getQualityOfCharset ("ISO-8859-1")));
  }

  @Test
  public void testSimpleWithAllWithQuality ()
  {
    final AcceptCharsetList c = AcceptCharsetHandler.getAcceptCharsets ("UTF-8,*;q=0.9");
    assertNotNull (c);
    // Explicitly contained
    assertTrue (EqualsUtils.equals (1d, c.getQualityOfCharset ("UTF-8")));
    // Not contained
    assertTrue (EqualsUtils.equals (0.9d, c.getQualityOfCharset ("ISO-8859-15")));
    // Default charset
    assertTrue (EqualsUtils.equals (0.9d, c.getQualityOfCharset ("ISO-8859-1")));
  }

  @Test
  public void testSimpleWithQualityWithAllWithQuality ()
  {
    final AcceptCharsetList c = AcceptCharsetHandler.getAcceptCharsets ("UTF-8;q=0.2,*;q=0.9");
    assertNotNull (c);
    // Explicitly contained
    assertTrue (EqualsUtils.equals (0.2d, c.getQualityOfCharset ("UTF-8")));
    // Not contained
    assertTrue (EqualsUtils.equals (0.9d, c.getQualityOfCharset ("ISO-8859-15")));
    // Default charset
    assertTrue (EqualsUtils.equals (0.9d, c.getQualityOfCharset ("ISO-8859-1")));
  }
}

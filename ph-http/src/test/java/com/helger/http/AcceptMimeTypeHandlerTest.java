/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.equals.EqualsHelper;

/**
 * Test class for class {@link AcceptMimeTypeHandler}
 *
 * @author Philip Helger
 */
public final class AcceptMimeTypeHandlerTest
{
  @Test
  public void testChrome13 ()
  {
    final AcceptMimeTypeList c = AcceptMimeTypeHandler.getAcceptMimeTypes ("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    assertNotNull (c);
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("text/html")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("application/xhtml+xml")));
    assertTrue (EqualsHelper.equals (0.9d, c.getQualityOfMimeType ("application/xml")));
    assertTrue (EqualsHelper.equals (0.8d, c.getQualityOfMimeType ("text/other")));
  }

  @Test
  public void testFirefox1_5 ()
  {
    final AcceptMimeTypeList c = AcceptMimeTypeHandler.getAcceptMimeTypes ("text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
    assertNotNull (c);
    assertTrue (EqualsHelper.equals (0.9d, c.getQualityOfMimeType ("text/html")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("application/xhtml+xml")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("application/xml")));
    assertTrue (EqualsHelper.equals (0.5d, c.getQualityOfMimeType ("text/other")));

    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("text/xml")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("application/xml")));
  }

  @Test
  public void testFirefox6 ()
  {
    final AcceptMimeTypeList c = AcceptMimeTypeHandler.getAcceptMimeTypes ("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    assertNotNull (c);
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("text/html")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("application/xhtml+xml")));
    assertTrue (EqualsHelper.equals (0.9d, c.getQualityOfMimeType ("application/xml")));
    assertTrue (EqualsHelper.equals (0.8d, c.getQualityOfMimeType ("text/other")));
  }

  @Test
  public void testIE6 ()
  {
    final AcceptMimeTypeList c = AcceptMimeTypeHandler.getAcceptMimeTypes ("*/*");
    assertNotNull (c);
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("text/html")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("application/xhtml+xml")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("application/xml")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("text/other")));
  }

  @Test
  public void testIE8 ()
  {
    final AcceptMimeTypeList c = AcceptMimeTypeHandler.getAcceptMimeTypes ("image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, */*");
    assertNotNull (c);
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("text/html")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("application/xhtml+xml")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("application/xml")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("text/other")));
  }

  @Test
  public void testIE9 ()
  {
    final AcceptMimeTypeList c = AcceptMimeTypeHandler.getAcceptMimeTypes ("text/html, application/xhtml+xml, */*");
    assertNotNull (c);
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("text/html")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("application/xhtml+xml")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("application/xml")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("text/other")));
  }

  @Test
  public void testGenericSubtype ()
  {
    final AcceptMimeTypeList c = AcceptMimeTypeHandler.getAcceptMimeTypes ("text/*,application/html;q=0.9,application/*;q=0.8");
    assertNotNull (c);
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("text/html")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("text/*")));
    assertTrue (EqualsHelper.equals (1d, c.getQualityOfMimeType ("text/anythingelse")));
    assertTrue (EqualsHelper.equals (0.9d, c.getQualityOfMimeType ("application/html")));
    assertTrue (EqualsHelper.equals (0.8d, c.getQualityOfMimeType ("application/xml")));
    assertTrue (EqualsHelper.equals (0d, c.getQualityOfMimeType ("image/gif")));

    assertFalse (c.explicitlySupportsMimeType ("text/html"));
    assertTrue (c.explicitlySupportsMimeType ("text/*"));
    assertFalse (c.explicitlySupportsMimeType ("text/anythingelse"));
    assertTrue (c.explicitlySupportsMimeType ("application/html"));
    assertFalse (c.explicitlySupportsMimeType ("application/xml"));
    assertFalse (c.explicitlySupportsMimeType ("image/gif"));
  }
}

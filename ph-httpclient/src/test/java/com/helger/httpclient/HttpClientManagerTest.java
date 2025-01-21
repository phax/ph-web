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
package com.helger.httpclient;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ContentType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.wrapper.Wrapper;
import com.helger.httpclient.response.ResponseHandlerString;

/**
 * Test class for class {@link HttpClientManager}.
 *
 * @author Philip Helger
 */
public final class HttpClientManagerTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (HttpClientManagerTest.class);

  @Test
  public void testMostBasic () throws IOException
  {
    try (final HttpClientManager aHC = new HttpClientManager ())
    {
      final String sResponse = aHC.execute (new HttpGet ("http://www.orf.at"),
                                            new ResponseHandlerString (ContentType.TEXT_HTML));
      LOGGER.info ("Got HTTP response with " + sResponse.length () + " chars");
    }
  }

  @Test
  public void testBasicGet () throws IOException
  {
    try (final HttpClientManager aHC = new HttpClientManager ())
    {
      final Wrapper <Charset> aWCS = new Wrapper <> ();
      final ResponseHandlerString aRH = new ResponseHandlerString (ContentType.TEXT_HTML).setCharsetConsumer (aWCS::set);
      final String sResponse = aHC.execute (new HttpGet ("http://www.orf.at"), aRH);
      LOGGER.info ("Got HTTP response with " + sResponse.length () + " chars, using charset " + aWCS.get ());
    }
  }
}

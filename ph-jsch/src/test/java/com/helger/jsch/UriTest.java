/*
 * Copyright (C) 2016-2023 Philip Helger (www.helger.com)
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
package com.helger.jsch;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UriTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (UriTest.class);

  @Test
  public void testUri () throws URISyntaxException
  {
    final URI uri1 = new URI ("ssh.unix://ltheisen@localhost:22/root/path/");
    LOGGER.info ("uri1 is '" + uri1 + "'");
    final URI uri2 = uri1.resolve ("relative/part");
    LOGGER.info ("uri2 is '" + uri2 + "'");
    final URI uri3 = uri2.resolve ("/new/root/");
    LOGGER.info ("uri3 is '" + uri3 + "'");

    assertEquals ("ltheisen", uri1.getUserInfo ());
    assertEquals ("localhost", uri1.getHost ());
    assertEquals (22, uri1.getPort ());
  }
}

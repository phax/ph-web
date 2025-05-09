/*
 * Copyright (C) 2020-2025 Philip Helger (www.helger.com)
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
package com.helger.dns.ip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;

import org.junit.Test;

import com.helger.commons.mock.CommonsTestHelper;

/**
 * Test class for class {@link IPV4Addr}.
 * 
 * @author Philip Helger
 */
public final class IPV4AddrTest
{
  @Test
  public void testBasic ()
  {
    final IPV4Addr aAddr = new IPV4Addr (1, 2, 3, 4);
    assertEquals ("1.2.3.4", aAddr.getAsString ());

    final InetAddress aIA = aAddr.getAsInetAddress ();
    assertNotNull (aIA);

    final IPV4Addr aAddr2 = new IPV4Addr (aIA);
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (aAddr, aAddr2);
  }
}

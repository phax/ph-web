/*
 * Copyright (C) 2020-2026 Philip Helger (www.helger.com)
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
package com.helger.dns.naptr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.time.Duration;

import org.junit.Test;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import com.helger.collection.commons.CommonsArrayList;
import com.helger.dns.naptr.NaptrLookup.ELookupNetworkMode;
import com.helger.dns.naptr.NaptrLookup.NaptrLookupBuilder;

/**
 * Test class for class {@link NaptrLookup} and {@link NaptrLookupBuilder}.
 *
 * @author Philip Helger
 */
public class NaptrLookupTest
{
  @Test
  public void testELookupNetworkMode ()
  {
    assertTrue (ELookupNetworkMode.UDP_TCP.isUDP ());
    assertTrue (ELookupNetworkMode.UDP_TCP.isTCP ());

    assertTrue (ELookupNetworkMode.UDP.isUDP ());
    assertFalse (ELookupNetworkMode.UDP.isTCP ());

    assertFalse (ELookupNetworkMode.TCP.isUDP ());
    assertTrue (ELookupNetworkMode.TCP.isTCP ());
  }

  @Test
  public void testBuilderDefaults ()
  {
    final NaptrLookupBuilder aBuilder = NaptrLookup.builder ();
    assertNull (aBuilder.domainName ());
    assertNull (aBuilder.domainNameString ());
  }

  @Test
  public void testBuilderDomainNameString () throws TextParseException
  {
    final NaptrLookupBuilder aBuilder = NaptrLookup.builder ().domainName ("example.org");
    assertNotNull (aBuilder.domainName ());
    assertNotNull (aBuilder.domainNameString ());
  }

  @Test
  public void testBuilderDomainNameObject () throws TextParseException
  {
    final Name aName = Name.fromString ("example.org.");
    final NaptrLookupBuilder aBuilder = NaptrLookup.builder ().domainName (aName);
    assertEquals (aName, aBuilder.domainName ());
  }

  @Test
  public void testBuilderDomainNameNull ()
  {
    final NaptrLookupBuilder aBuilder = NaptrLookup.builder ().domainName ((Name) null);
    assertNull (aBuilder.domainName ());
  }

  @Test
  public void testBuilderMaxRetries () throws TextParseException
  {
    // Should build successfully
    final NaptrLookup aLookup = NaptrLookup.builder ().domainName ("example.org").maxRetries (5).build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderNoRetries () throws TextParseException
  {
    final NaptrLookup aLookup = NaptrLookup.builder ().domainName ("example.org").noRetries ().build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderTimeout () throws TextParseException
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .timeout (Duration.ofSeconds (5))
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderTimeoutMS () throws TextParseException
  {
    final NaptrLookup aLookup = NaptrLookup.builder ().domainName ("example.org").timeoutMS (5000).build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderTimeoutMSNegative () throws TextParseException
  {
    // Negative timeout -> null timeout
    final NaptrLookup aLookup = NaptrLookup.builder ().domainName ("example.org").timeoutMS (-1).build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderTimeoutNull () throws TextParseException
  {
    final NaptrLookup aLookup = NaptrLookup.builder ().domainName ("example.org").timeout (null).build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderLookupModes () throws TextParseException
  {
    for (final ELookupNetworkMode eMode : ELookupNetworkMode.values ())
    {
      final NaptrLookup aLookup = NaptrLookup.builder ().domainName ("example.org").lookupMode (eMode).build ();
      assertNotNull (aLookup);
    }
  }

  @Test
  public void testBuilderExecutionDurationWarn () throws TextParseException
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .executionDurationWarn (Duration.ofSeconds (2))
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderExecutionDurationWarnMS () throws TextParseException
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .executionDurationWarnMS (2000)
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderExecutionDurationWarnMSNegative () throws TextParseException
  {
    // Negative -> null (disables warning)
    final NaptrLookup aLookup = NaptrLookup.builder ().domainName ("example.org").executionDurationWarnMS (-1).build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderExecutionDurationWarnNull () throws TextParseException
  {
    final NaptrLookup aLookup = NaptrLookup.builder ().domainName ("example.org").executionDurationWarn (null).build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderDebugMode () throws TextParseException
  {
    final NaptrLookup aLookup = NaptrLookup.builder ().domainName ("example.org").debugMode (true).build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderAddExecutionTimeExceededHandler () throws TextParseException
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .addExecutionTimeExceededHandler (new LoggingNaptrLookupTimeExceededCallback (true))
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderAddExecutionTimeExceededHandlerNull () throws TextParseException
  {
    // null handler should be silently ignored
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .addExecutionTimeExceededHandler (null)
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderCustomDNSServer () throws Exception
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .customDNSServer (InetAddress.getByName ("8.8.8.8"))
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderCustomDNSServerNull () throws TextParseException
  {
    // Null clears custom DNS servers
    final NaptrLookup aLookup = NaptrLookup.builder ().domainName ("example.org").customDNSServer (null).build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderCustomDNSServersArray () throws Exception
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .customDNSServers (InetAddress.getByName ("8.8.8.8"),
                                                              InetAddress.getByName ("8.8.4.4"))
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderCustomDNSServersArrayNull () throws TextParseException
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .customDNSServers ((InetAddress []) null)
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderCustomDNSServersIterable () throws Exception
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .customDNSServers (new CommonsArrayList <> (InetAddress.getByName ("8.8.8.8")))
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderCustomDNSServersIterableNull () throws TextParseException
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .customDNSServers ((Iterable <InetAddress>) null)
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderAddCustomDNSServer () throws Exception
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .addCustomDNSServer (InetAddress.getByName ("8.8.8.8"))
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderAddCustomDNSServerNull () throws TextParseException
  {
    // null is silently ignored
    final NaptrLookup aLookup = NaptrLookup.builder ().domainName ("example.org").addCustomDNSServer (null).build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderAddCustomDNSServersArray () throws Exception
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .addCustomDNSServers (InetAddress.getByName ("8.8.8.8"),
                                                                 InetAddress.getByName ("8.8.4.4"))
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderAddCustomDNSServersArrayNull () throws TextParseException
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .addCustomDNSServers ((InetAddress []) null)
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderAddCustomDNSServersIterable () throws Exception
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .addCustomDNSServers (new CommonsArrayList <> (InetAddress.getByName ("8.8.8.8")))
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderAddCustomDNSServersIterableNull () throws TextParseException
  {
    final NaptrLookup aLookup = NaptrLookup.builder ()
                                           .domainName ("example.org")
                                           .addCustomDNSServers ((Iterable <InetAddress>) null)
                                           .build ();
    assertNotNull (aLookup);
  }

  @Test
  public void testBuilderBuildWithoutDomainName ()
  {
    try
    {
      NaptrLookup.builder ().build ();
      fail ("Expected IllegalStateException");
    }
    catch (final IllegalStateException ex)
    {
      // expected
    }
  }

  @Test
  public void testBuilderBuildWithNegativeRetries ()
  {
    try
    {
      NaptrLookup.builder ().domainName (Name.root).maxRetries (-1).build ();
      fail ("Expected IllegalStateException");
    }
    catch (final IllegalStateException ex)
    {
      // expected
    }
  }

  @Test
  public void testBuilderBuildWithNullLookupMode ()
  {
    try
    {
      NaptrLookup.builder ().domainName (Name.root).lookupMode (null).build ();
      fail ("Expected IllegalStateException");
    }
    catch (final IllegalStateException ex)
    {
      // expected
    }
  }

  @Test
  public void testBuilderDefaultConstants ()
  {
    assertEquals (1, NaptrLookupBuilder.DEFAULT_MAX_RETRIES);
    assertEquals (Duration.ofSeconds (1), NaptrLookupBuilder.DEFAULT_EXECUTION_DURATION_WARN);
    assertEquals (ELookupNetworkMode.UDP_TCP, NaptrLookupBuilder.DEFAULT_LOOKUP_MODE);
  }
}

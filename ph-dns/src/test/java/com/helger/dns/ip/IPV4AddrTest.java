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

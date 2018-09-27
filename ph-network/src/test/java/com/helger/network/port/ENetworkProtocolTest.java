package com.helger.network.port;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.string.StringHelper;

/**
 * Test class for class {@link ENetworkProtocol}.
 *
 * @author Philip Helger
 */
public final class ENetworkProtocolTest
{
  @Test
  public void testBasic ()
  {
    for (final ENetworkProtocol e : ENetworkProtocol.values ())
    {
      assertTrue (StringHelper.hasText (e.getID ()));
      assertSame (e, ENetworkProtocol.getFromIDOrNull (e.getID ()));
    }
  }
}

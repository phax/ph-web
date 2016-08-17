package com.helger.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test class for class {@link RFC5234_BNF}.
 *
 * @author Philip Helger
 */
public final class RFC5234_BNFTest
{
  @Test
  public void testIsAlpha ()
  {
    for (int i = 'a'; i <= 'z'; ++i)
      assertTrue (RFC5234_BNF.isAlpha (i));
    for (int i = 'A'; i <= 'Z'; ++i)
      assertTrue (RFC5234_BNF.isAlpha (i));
    assertFalse (RFC5234_BNF.isAlpha (0));
  }

  @Test
  public void testIsBit ()
  {
    assertTrue (RFC5234_BNF.isBit ('0'));
    assertTrue (RFC5234_BNF.isBit ('1'));
    assertFalse (RFC5234_BNF.isBit (0));
  }

  @Test
  public void testIsChar ()
  {
    for (int i = 1; i <= 0x7f; ++i)
      assertTrue (RFC5234_BNF.isChar (i));
    assertFalse (RFC5234_BNF.isChar (0));
  }

  @Test
  public void testIsCR ()
  {
    assertTrue (RFC5234_BNF.isCR ('\r'));
    assertFalse (RFC5234_BNF.isCR (0));
  }

  @Test
  public void testIsCtl ()
  {
    for (int i = 0; i < 0x1f; ++i)
      assertTrue (RFC5234_BNF.isCtl (i));
    assertTrue (RFC5234_BNF.isCtl (0x7f));
    assertFalse (RFC5234_BNF.isCtl (0x20));
  }

  @Test
  public void testIsDigit ()
  {
    for (int i = '0'; i < '9'; ++i)
      assertTrue (RFC5234_BNF.isDigit (i));
    assertFalse (RFC5234_BNF.isDigit (0));
  }

  @Test
  public void testIsDQuote ()
  {
    assertTrue (RFC5234_BNF.isDQuote ('"'));
    assertFalse (RFC5234_BNF.isDQuote (0));
  }

  @Test
  public void testIsHexDigit ()
  {
    for (int i = '0'; i < '9'; ++i)
      assertTrue (RFC5234_BNF.isHexDigit (i));
    for (int i = 'A'; i < 'F'; ++i)
      assertTrue (RFC5234_BNF.isHexDigit (i));
    for (int i = 'a'; i < 'f'; ++i)
      assertFalse (RFC5234_BNF.isHexDigit (i));
    assertFalse (RFC5234_BNF.isHexDigit (0));
  }

  @Test
  public void testIsHTab ()
  {
    assertTrue (RFC5234_BNF.isHTab (0x09));
    assertFalse (RFC5234_BNF.isHTab (0));
  }

  @Test
  public void testIsLF ()
  {
    assertTrue (RFC5234_BNF.isLF ('\n'));
    assertFalse (RFC5234_BNF.isLF (0));
  }

  @Test
  public void testIsOctet ()
  {
    for (int i = 0x00; i <= 0xff; ++i)
      assertTrue (RFC5234_BNF.isOctet (i));
    assertFalse (RFC5234_BNF.isOctet (-1));
    assertFalse (RFC5234_BNF.isOctet (256));
  }

  @Test
  public void testIsSP ()
  {
    assertTrue (RFC5234_BNF.isSP (0x20));
    assertFalse (RFC5234_BNF.isSP (0));
  }

  @Test
  public void testIsVChar ()
  {
    for (int i = 0x21; i <= 0x7e; ++i)
      assertTrue (RFC5234_BNF.isVChar (i));
    assertFalse (RFC5234_BNF.isVChar (0));
  }

  @Test
  public void testIsWSP ()
  {
    assertTrue (RFC5234_BNF.isWSP (0x09));
    assertTrue (RFC5234_BNF.isWSP (0x20));
    assertFalse (RFC5234_BNF.isWSP (0));
  }
}

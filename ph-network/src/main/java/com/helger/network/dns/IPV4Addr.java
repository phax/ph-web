/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.network.dns;

import java.io.Serializable;
import java.net.InetAddress;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;
import com.helger.commons.string.ToStringGenerator;

/**
 * Representation of a single IP V4 address.
 *
 * @author Philip Helger
 */
@Immutable
public class IPV4Addr implements Serializable
{
  /** A regular expression pattern to validate IPv4 addresses */
  public static final String PATTERN_IPV4 = "\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b";

  public static final int PARTS = 4;
  public static final int PART_MIN_VALUE = 0;
  public static final int PART_MAX_VALUE = 255;

  private final int m_nIP0;
  private final int m_nIP1;
  private final int m_nIP2;
  private final int m_nIP3;

  @Nonnegative
  private static int _validatePart (@Nonnegative final int n)
  {
    return ValueEnforcer.isBetweenInclusive (n, "IP part", PART_MIN_VALUE, PART_MAX_VALUE);
  }

  @Nonnegative
  private static int _validatePart (@Nullable final String s)
  {
    return _validatePart (StringParser.parseInt (s, CGlobal.ILLEGAL_UINT));
  }

  public IPV4Addr (@Nonnull final InetAddress aAddress)
  {
    this (aAddress.getAddress ());
  }

  public IPV4Addr (@Nonnull final byte [] aAddressBytes)
  {
    this (aAddressBytes[0] &
          PART_MAX_VALUE,
          aAddressBytes[1] & PART_MAX_VALUE,
          aAddressBytes[2] & PART_MAX_VALUE,
          aAddressBytes[3] & PART_MAX_VALUE);
  }

  /**
   * Constructor that creates an IP address from the 4 numbers.
   *
   * @param n1
   *        first number
   * @param n2
   *        second number
   * @param n3
   *        third number
   * @param n4
   *        fourth number
   */
  public IPV4Addr (@Nonnegative final int n1,
                   @Nonnegative final int n2,
                   @Nonnegative final int n3,
                   @Nonnegative final int n4)
  {
    m_nIP0 = _validatePart (n1);
    m_nIP1 = _validatePart (n2);
    m_nIP2 = _validatePart (n3);
    m_nIP3 = _validatePart (n4);
  }

  /**
   * @param sText
   *        The text interpretation of an IP address like "10.0.0.1".
   */
  private IPV4Addr (@Nonnull final String sText)
  {
    ValueEnforcer.notNull (sText, "Text");
    final String [] aParts = StringHelper.getExplodedArray ('.', sText);
    if (aParts.length != PARTS)
      throw new IllegalArgumentException ("Expected exactly " + PARTS + " parts");
    m_nIP0 = _validatePart (aParts[0]);
    m_nIP1 = _validatePart (aParts[1]);
    m_nIP2 = _validatePart (aParts[2]);
    m_nIP3 = _validatePart (aParts[3]);
  }

  @Nonnull
  @ReturnsMutableCopy
  public int [] getNumberParts ()
  {
    return ArrayHelper.newIntArray (m_nIP0, m_nIP1, m_nIP2, m_nIP3);
  }

  /**
   * @return The ID address as a usual string (e.g. "10.0.0.1")
   */
  @Nonnull
  public String getAsString ()
  {
    return new StringBuilder (15).append (m_nIP0)
                                 .append ('.')
                                 .append (m_nIP1)
                                 .append ('.')
                                 .append (m_nIP2)
                                 .append ('.')
                                 .append (m_nIP3)
                                 .toString ();
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final IPV4Addr rhs = (IPV4Addr) o;
    return m_nIP0 == rhs.m_nIP0 && m_nIP1 == rhs.m_nIP1 && m_nIP2 == rhs.m_nIP2 && m_nIP3 == rhs.m_nIP3;
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_nIP0).append (m_nIP1).append (m_nIP2).append (m_nIP3).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ip0", m_nIP0)
                                       .append ("ip1", m_nIP1)
                                       .append ("ip2", m_nIP2)
                                       .append ("ip3", m_nIP3)
                                       .toString ();
  }

  /**
   * Parse the provided IPv4 address from the text string (as e.g.
   * "192.168.0.1").
   *
   * @param sText
   *        The text to be parsed. May not be <code>null</code>.
   * @return The created object and never <code>null</code>.
   * @throws IllegalArgumentException
   *         If the passed string is not a valid IPv4 address
   */
  @Nonnull
  public static IPV4Addr parse (@Nonnull final String sText)
  {
    return new IPV4Addr (sText);
  }
}

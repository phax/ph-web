/**
 * Copyright (C) 2006-2014 phloc systems (www.phloc.com)
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
package com.helger.web.encoding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotations.PresentForCodeCoverage;
import com.helger.commons.codec.DecoderException;
import com.helger.commons.codec.EncoderException;
import com.helger.commons.i18n.CodepointIteratorCharArray;
import com.helger.commons.i18n.CodepointUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Implementation of the Punycode encoding scheme used by IDNA
 * 
 * @author Apache Abdera
 */
@Immutable
public final class Punycode
{
  private static final int BASE = 0x24; // 36
  private static final int TMIN = 0x01; // 1
  private static final int TMAX = 0x1A; // 26
  private static final int SKEW = 0x26; // 38
  private static final int DAMP = 0x02BC; // 700
  private static final int INITIAL_BIAS = 0x48; // 72
  private static final int INITIAL_N = 0x80; // 0x80
  private static final int DELIMITER = 0x2D; // 0x2D

  @PresentForCodeCoverage
  @SuppressWarnings ("unused")
  private static final Punycode s_aInstance = new Punycode ();

  private Punycode ()
  {}

  private static boolean _basic (final int cp)
  {
    return cp < 0x80;
  }

  private static boolean _delim (final int cp)
  {
    return cp == DELIMITER;
  }

  private static boolean _flagged (final int bcp)
  {
    return (bcp - 65) < 26;
  }

  private static int _decode_digit (final int cp)
  {
    return (cp - 48 < 10) ? cp - 22 : (cp - 65 < 26) ? cp - 65 : (cp - 97 < 26) ? cp - 97 : BASE;
  }

  private static int _t (final boolean c)
  {
    return c ? 1 : 0;
  }

  private static int _encode_digit (final int d, final boolean upper)
  {
    return (d + 22 + 75 * _t (d < 26)) - (_t (upper) << 5);
  }

  private static int _adapt (final int pdelta, final int numpoints, final boolean firsttime)
  {
    int delta = pdelta;
    int k;
    delta = (firsttime) ? delta / DAMP : delta >> 1;
    delta += delta / numpoints;
    for (k = 0; delta > ((BASE - TMIN) * TMAX) / 2; k += BASE)
    {
      delta /= BASE - TMIN;
    }
    return k + (BASE - TMIN + 1) * delta / (delta + SKEW);
  }

  @Nullable
  public static String encode (@Nullable final String s)
  {
    if (s == null)
      return null;
    return encode (s.toCharArray (), null);
  }

  public static String encode (@Nonnull final char [] aChars, @Nullable final boolean [] aCaseFlags)
  {
    final StringBuilder aSB = new StringBuilder ();
    final CodepointIteratorCharArray aIter = new CodepointIteratorCharArray (aChars);
    int n = INITIAL_N;
    int delta = 0;
    int bias = INITIAL_BIAS;
    int i = -1;
    while (aIter.hasNext ())
    {
      i = aIter.next ().getValue ();
      if (_basic (i))
      {
        if (aCaseFlags == null)
        {
          aSB.append ((char) i);
        }
      }
    }
    int h, b, m, q, k, t;
    b = aSB.length ();
    h = b;
    if (b > 0)
      aSB.append ((char) DELIMITER);
    while (h < aChars.length)
    {
      aIter.position (0);
      i = -1;
      m = Integer.MAX_VALUE;
      while (aIter.hasNext ())
      {
        i = aIter.next ().getValue ();
        if (i >= n && i < m)
          m = i;
      }
      if (m - n > (Integer.MAX_VALUE - delta) / (h + 1))
        throw new EncoderException ("Overflow");
      delta += (m - n) * (h + 1);
      n = m;
      aIter.position (0);
      i = -1;
      while (aIter.hasNext ())
      {
        i = aIter.next ().getValue ();
        if (i < n)
        {
          if (++delta == 0)
            throw new EncoderException ("Overflow");
        }
        if (i == n)
        {
          for (q = delta, k = BASE;; k += BASE)
          {
            t = k <= bias ? TMIN : k >= bias + TMAX ? TMAX : k - bias;
            if (q < t)
              break;
            aSB.append ((char) _encode_digit (t + (q - t) % (BASE - t), false));
            q = (q - t) / (BASE - t);
          }
          aSB.append ((char) _encode_digit (q, (aCaseFlags != null) ? aCaseFlags[aIter.position () - 1] : false));
          bias = _adapt (delta, h + 1, h == b);
          delta = 0;
          ++h;
        }
      }
      ++delta;
      ++n;
    }
    return aSB.toString ();
  }

  @Nullable
  public static String decode (@Nullable final String s)
  {
    if (s == null)
      return null;
    return decode (s.toCharArray (), null);
  }

  @SuppressFBWarnings ("QF_QUESTIONABLE_FOR_LOOP")
  @Nonnull
  public static String decode (@Nonnull final char [] aChars, @Nullable final boolean [] aCaseFlags)
  {
    final StringBuilder aSB = new StringBuilder ();
    int n, out, i, bias, b, j, in, oldi, w, k, digit, t;
    n = INITIAL_N;
    out = 0;
    i = 0;
    bias = INITIAL_BIAS;
    for (b = 0, j = 0; j < aChars.length; ++j)
      if (_delim (aChars[j]))
        b = j;
    for (j = 0; j < b; ++j)
    {
      if (aCaseFlags != null)
        aCaseFlags[out] = _flagged (aChars[j]);
      if (!_basic (aChars[j]))
        throw new DecoderException ("Bad Input");
      aSB.append (aChars[j]);
    }
    out = aSB.length ();
    for (in = (b > 0) ? b + 1 : 0; in < aChars.length; ++out)
    {
      for (oldi = i, w = 1, k = BASE;; k += BASE)
      {
        if (in > aChars.length)
          throw new DecoderException ("Bad input");
        digit = _decode_digit (aChars[in++]);
        if (digit >= BASE)
          throw new DecoderException ("Bad input");
        if (digit > (Integer.MAX_VALUE - i) / w)
          throw new DecoderException ("Overflow");
        i += digit * w;
        t = (k <= bias) ? TMIN : (k >= bias + TMAX) ? TMAX : k - bias;
        if (digit < t)
          break;
        if (w > Integer.MAX_VALUE / (BASE - t))
          throw new DecoderException ("Overflow");
        w *= (BASE - t);
      }
      bias = _adapt (i - oldi, out + 1, oldi == 0);
      if (i / (out + 1) > Integer.MAX_VALUE - n)
        throw new DecoderException ("Overflow");
      n += i / (out + 1);
      i %= (out + 1);
      if (aCaseFlags != null)
      {
        // not sure if this is right
        System.arraycopy (aCaseFlags, i, aCaseFlags, i + Character.charCount (n), aCaseFlags.length - i);
      }
      CodepointUtils.insert (aSB, i++, n);
    }
    return aSB.toString ();
  }
}

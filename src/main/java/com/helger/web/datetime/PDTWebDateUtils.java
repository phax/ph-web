/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.datetime;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.helger.commons.annotations.PresentForCodeCoverage;
import com.helger.commons.collections.pair.IReadonlyPair;
import com.helger.commons.collections.pair.ReadonlyPair;
import com.helger.datetime.PDTFactory;
import com.helger.datetime.config.PDTConfig;
import com.helger.datetime.format.PDTFormatter;
import com.helger.datetime.format.PDTFromString;

/**
 * A helper class that parses Dates out of Strings with date time in RFC822 and
 * W3CDateTime formats plus the variants Atom (0.3) and RSS (0.9, 0.91, 0.92,
 * 0.93, 0.94, 1.0 and 2.0) specificators added to those formats.
 * <p/>
 * It uses the JDK java.text.SimpleDateFormat class attempting the parse using a
 * mask for each one of the possible formats.
 * <p/>
 * Original work Copyright 2004 Sun Microsystems, Inc.
 *
 * @author Alejandro Abdelnur (original; mainly the formatting masks)
 * @author Philip Helger (major modification)
 */
@Immutable
public final class PDTWebDateUtils
{
  private static final String FORMAT_RFC822 = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
  private static final String FORMAT_W3C = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /*
   * order is like this because the SimpleDateFormat.parse does not fail with
   * exception if it can parse a valid date out of a substring of the full
   * string given the mask so we have to check the most complete format first,
   * then it fails with exception
   */
  private static final String [] RFC822_MASKS = { "EEE, dd MMM yy HH:mm:ss",
                                                 "EEE, dd MMM yy HH:mm",
                                                 "dd MMM yy HH:mm:ss",
                                                 "dd MMM yy HH:mm" };

  /*
   * order is like this because the SimpleDateFormat.parse does not fail with
   * exception if it can parse a valid date out of a substring of the full
   * string given the mask so we have to check the most complete format first,
   * then it fails with exception
   */
  private static final String [] W3CDATETIME_MASKS = { "yyyy-MM-dd'T'HH:mm:ss.SSS",
                                                      "yyyy-MM-dd't'HH:mm:ss.SSS",
                                                      "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                                      "yyyy-MM-dd't'HH:mm:ss.SSS'z'",
                                                      "yyyy-MM-dd'T'HH:mm:ss",
                                                      "yyyy-MM-dd't'HH:mm:ss",
                                                      "yyyy-MM-dd'T'HH:mm:ss",
                                                      "yyyy-MM-dd't'HH:mm:ss",
                                                      FORMAT_W3C,
                                                      "yyyy-MM-dd't'HH:mm:ss'z'",
                                                      "yyyy-MM-dd'T'HH:mm",
                                                      /*
                                                       * Applies to the
                                                       * following 2: together
                                                       * with logic in the
                                                       * parseW3CDateTime they
                                                       * handle W3C dates
                                                       * without time forcing
                                                       * them to be GMT
                                                       */
                                                      "yyyy-MM'T'HH:mm",
                                                      "yyyy'T'HH:mm",
                                                      "yyyy-MM-dd't'HH:mm",
                                                      "yyyy-MM-dd'T'HH:mm'Z'",
                                                      "yyyy-MM-dd't'HH:mm'z'",
                                                      "yyyy-MM-dd",
                                                      "yyyy-MM",
                                                      "yyyy" };

  private static final Locale LOCALE_TO_USE = Locale.US;

  @PresentForCodeCoverage
  private static final PDTWebDateUtils s_aInstance = new PDTWebDateUtils ();

  private PDTWebDateUtils ()
  {}

  /**
   * Parses a Date out of a string using an array of masks.
   * <p/>
   * It uses the masks in order until one of them succeeds or all fail.
   * <p/>
   *
   * @param aMasks
   *        array of masks to use for parsing the string
   * @param sDate
   *        string to parse for a date.
   * @param aDTZ
   *        The date/time zone to use. Optional.
   * @return the Date represented by the given string using one of the given
   *         masks. It returns <b>null</b> if it was not possible to parse the
   *         the string with any of the masks.
   */
  @Nullable
  private static DateTime _parseDateTimeUsingMask (@Nonnull final String [] aMasks,
                                                   @Nonnull final String sDate,
                                                   @Nullable final DateTimeZone aDTZ)
  {
    for (final String sMask : aMasks)
    {
      DateTimeFormatter aDTF = PDTFormatter.getForPattern (sMask, LOCALE_TO_USE)
                                           .withChronology (PDTConfig.getDefaultChronology ());
      if (aDTZ != null)
        aDTF = aDTF.withZone (aDTZ);
      final DateTime aDT = PDTFromString.getDateTimeFromString (sDate, aDTF);
      if (aDT != null)
        return aDT;
    }
    return null;
  }

  @Nullable
  private static LocalDateTime _parseLocalDateTimeUsingMask (@Nonnull final String [] aMasks,
                                                             @Nonnull final String sDate,
                                                             @Nullable final DateTimeZone aDTZ)
  {
    for (final String sMask : aMasks)
    {
      DateTimeFormatter aDTF = PDTFormatter.getForPattern (sMask, LOCALE_TO_USE)
                                           .withChronology (PDTConfig.getDefaultChronology ());
      if (aDTZ != null)
        aDTF = aDTF.withZone (aDTZ);
      final LocalDateTime aDT = PDTFromString.getLocalDateTimeFromString (sDate, aDTF);
      if (aDT != null)
        return aDT;
    }
    return null;
  }

  /**
   * Extract the time zone from the passed string. UTC and GMT are supported.
   *
   * @param sDate
   *        The date string.
   * @return A non-<code>null</code> pair, where the first element is the
   *         remaining string to be parsed (never <code>null</code>) and the
   *         second element is the extracted time zone (may be <code>null</code>
   *         ).
   */
  @Nonnull
  private static IReadonlyPair <String, DateTimeZone> _extractDateTimeZone (@Nonnull final String sDate)
  {
    final String [] aDTZ = { "UTC", "GMT" };
    for (final String sDTZ : aDTZ)
    {
      if (sDate.endsWith (" " + sDTZ))
        return ReadonlyPair.create (sDate.substring (0, sDate.length () - (1 + sDTZ.length ())),
                                    DateTimeZone.forID (sDTZ));
      if (sDate.endsWith (sDTZ))
        return ReadonlyPair.create (sDate.substring (0, sDate.length () - sDTZ.length ()), DateTimeZone.forID (sDTZ));
    }
    return ReadonlyPair.create (sDate, null);
  }

  /**
   * Parses a Date out of a String with a date in RFC822 format.
   * <p/>
   * It parsers the following formats:
   * <ul>
   * <li>"EEE, dd MMM yyyy HH:mm:ss z"</li>
   * <li>"EEE, dd MMM yyyy HH:mm z"</li>
   * <li>"EEE, dd MMM yy HH:mm:ss z"</li>
   * <li>"EEE, dd MMM yy HH:mm z"</li>
   * <li>"dd MMM yyyy HH:mm:ss z"</li>
   * <li>"dd MMM yyyy HH:mm z"</li>
   * <li>"dd MMM yy HH:mm:ss z"</li>
   * <li>"dd MMM yy HH:mm z"</li>
   * </ul>
   * <p/>
   * Refer to the java.text.SimpleDateFormat javadocs for details on the format
   * of each element.
   * <p/>
   *
   * @param sDate
   *        string to parse for a date. May be <code>null</code>.
   * @return the Date represented by the given RFC822 string. It returns
   *         <b>null</b> if it was not possible to parse the given string into a
   *         {@link DateTime} or if the passed {@link String} was
   *         <code>null</code>.
   */
  @Nullable
  public static DateTime getDateTimeFromRFC822 (@Nullable final String sDate)
  {
    if (sDate == null)
      return null;

    final IReadonlyPair <String, DateTimeZone> aPair = _extractDateTimeZone (sDate.trim ());
    return _parseDateTimeUsingMask (RFC822_MASKS, aPair.getFirst (), aPair.getSecond ());
  }

  /**
   * Parses a Date out of a String with a date in RFC822 format.
   * <p/>
   * It parsers the following formats:
   * <ul>
   * <li>"EEE, dd MMM yyyy HH:mm:ss z"</li>
   * <li>"EEE, dd MMM yyyy HH:mm z"</li>
   * <li>"EEE, dd MMM yy HH:mm:ss z"</li>
   * <li>"EEE, dd MMM yy HH:mm z"</li>
   * <li>"dd MMM yyyy HH:mm:ss z"</li>
   * <li>"dd MMM yyyy HH:mm z"</li>
   * <li>"dd MMM yy HH:mm:ss z"</li>
   * <li>"dd MMM yy HH:mm z"</li>
   * </ul>
   * <p/>
   * Refer to the java.text.SimpleDateFormat javadocs for details on the format
   * of each element.
   * <p/>
   *
   * @param sDate
   *        string to parse for a date. May be <code>null</code>.
   * @return the Date represented by the given RFC822 string. It returns
   *         <b>null</b> if it was not possible to parse the given string into a
   *         {@link DateTime} or if the passed {@link String} was
   *         <code>null</code>.
   */
  @Nullable
  public static LocalDateTime getLocalDateTimeFromRFC822 (@Nullable final String sDate)
  {
    if (sDate == null)
      return null;

    final IReadonlyPair <String, DateTimeZone> aPair = _extractDateTimeZone (sDate.trim ());
    return _parseLocalDateTimeUsingMask (RFC822_MASKS, aPair.getFirst (), aPair.getSecond ());
  }

  /**
   * Parses a Date out of a String with a date in W3C date-time format.
   * <p/>
   * It parsers the following formats:
   * <ul>
   * <li>"yyyy-MM-dd'T'HH:mm:ssz"</li>
   * <li>"yyyy-MM-dd'T'HH:mmz"</li>
   * <li>"yyyy-MM-dd"</li>
   * <li>"yyyy-MM"</li>
   * <li>"yyyy"</li>
   * </ul>
   * <p/>
   * Refer to the java.text.SimpleDateFormat javadocs for details on the format
   * of each element.
   * <p/>
   *
   * @param sDate
   *        string to parse for a date. May be <code>null</code>.
   * @return the Date represented by the given W3C date-time string. It returns
   *         <b>null</b> if it was not possible to parse the given string into a
   *         {@link DateTime} or if the input string was <code>null</code>.
   */
  @Nullable
  public static DateTime getDateTimeFromW3C (@Nullable final String sDate)
  {
    if (sDate == null)
      return null;

    final IReadonlyPair <String, DateTimeZone> aPair = _extractDateTimeZone (sDate.trim ());
    return _parseDateTimeUsingMask (W3CDATETIME_MASKS, aPair.getFirst (), aPair.getSecond ());
  }

  /**
   * Parses a Date out of a String with a date in W3C date-time format.
   * <p/>
   * It parsers the following formats:
   * <ul>
   * <li>"yyyy-MM-dd'T'HH:mm:ssz"</li>
   * <li>"yyyy-MM-dd'T'HH:mmz"</li>
   * <li>"yyyy-MM-dd"</li>
   * <li>"yyyy-MM"</li>
   * <li>"yyyy"</li>
   * </ul>
   * <p/>
   * Refer to the java.text.SimpleDateFormat javadocs for details on the format
   * of each element.
   * <p/>
   *
   * @param sDate
   *        string to parse for a date. May be <code>null</code>.
   * @return the Date represented by the given W3C date-time string. It returns
   *         <b>null</b> if it was not possible to parse the given string into a
   *         {@link DateTime} or if the input string was <code>null</code>.
   */
  @Nullable
  public static LocalDateTime getLocalDateTimeFromW3C (@Nullable final String sDate)
  {
    if (sDate == null)
      return null;

    final IReadonlyPair <String, DateTimeZone> aPair = _extractDateTimeZone (sDate.trim ());
    return _parseLocalDateTimeUsingMask (W3CDATETIME_MASKS, aPair.getFirst (), aPair.getSecond ());
  }

  /**
   * Parses a Date out of a String with a date in W3C date-time format or in a
   * RFC822 format.
   * <p>
   *
   * @param sDate
   *        string to parse for a date.
   * @return the Date represented by the given W3C date-time string. It returns
   *         <b>null</b> if it was not possible to parse the given string into a
   *         Date.
   */
  @Nullable
  public static DateTime getDateTimeFromW3COrRFC822 (@Nullable final String sDate)
  {
    DateTime aDateTime = getDateTimeFromW3C (sDate);
    if (aDateTime == null)
      aDateTime = getDateTimeFromRFC822 (sDate);
    return aDateTime;
  }

  /**
   * Parses a Date out of a String with a date in W3C date-time format or in a
   * RFC822 format.
   * <p>
   *
   * @param sDate
   *        string to parse for a date.
   * @return the Date represented by the given W3C date-time string. It returns
   *         <b>null</b> if it was not possible to parse the given string into a
   *         Date.
   */
  @Nullable
  public static LocalDateTime getLocalDateTimeFromW3COrRFC822 (@Nullable final String sDate)
  {
    LocalDateTime aDateTime = getLocalDateTimeFromW3C (sDate);
    if (aDateTime == null)
      aDateTime = getLocalDateTimeFromRFC822 (sDate);
    return aDateTime;
  }

  /**
   * create a RFC822 representation of a date.
   *
   * @param aDateTime
   *        Date to parse. If <code>null</code> the current date and time is
   *        used.
   * @return the RFC822 represented by the given Date.
   */
  @Nonnull
  public static String getAsStringRFC822 (@Nullable final DateTime aDateTime)
  {
    return PDTFormatter.getForPattern (FORMAT_RFC822, LOCALE_TO_USE)
                       .withZone (PDTConfig.getDateTimeZoneUTC ())
                       .print (aDateTime);
  }

  /**
   * create a RFC822 representation of a date time using UTC date time zone.
   *
   * @param aDateTime
   *        Date to parse. If <code>null</code> the current date and time is
   *        used.
   * @return the RFC822 represented by the given Date.
   */
  @Nonnull
  public static String getAsStringRFC822 (@Nullable final LocalDateTime aDateTime)
  {
    return PDTFormatter.getForPattern (FORMAT_RFC822, LOCALE_TO_USE)
                       .withZone (PDTConfig.getDateTimeZoneUTC ())
                       .print (aDateTime);
  }

  /**
   * create a W3C Date Time representation of a date.
   *
   * @param aDateTime
   *        Date to parse. If <code>null</code> the current date and time is
   *        used.
   * @return the W3C Date Time represented by the given Date.
   */
  @Nonnull
  public static String getAsStringW3C (@Nullable final DateTime aDateTime)
  {
    DateTimeFormatter aFormatter = PDTFormatter.getForPattern (FORMAT_W3C, LOCALE_TO_USE);
    if (aDateTime != null)
      aFormatter = aFormatter.withZone (aDateTime.getZone ());
    return aFormatter.print (aDateTime);
  }

  /**
   * create a W3C Date Time representation of a date time using UTC date time
   * zone.
   *
   * @param aDateTime
   *        Date to parse. If <code>null</code> the current date and time is
   *        used.
   * @return the W3C Date Time represented by the given Date.
   */
  @Nonnull
  public static String getAsStringW3C (@Nullable final LocalDateTime aDateTime)
  {
    return getAsStringW3C (aDateTime == null ? (DateTime) null : aDateTime.toDateTime (PDTConfig.getDateTimeZoneUTC ()));
  }

  /**
   * @return The current date time formatted using RFC 822
   */
  @Nonnull
  public static String getCurrentDateTimeAsStringRFC822 ()
  {
    // Important to use date time zone GMT as this is what the standard
    // printer emits!
    // Use no milli seconds as the standard printer does not print them!
    final DateTime aNow = PDTFactory.getCurrentDateTime ().withZone (DateTimeZone.forID ("GMT")).withMillisOfSecond (0);
    return getAsStringRFC822 (aNow);
  }

  /**
   * @return The current date time formatted using W3C format
   */
  @Nonnull
  public static String getCurrentDateTimeAsStringW3C ()
  {
    // Use no milli seconds as the standard printer does not print them!
    final DateTime aNow = PDTFactory.getCurrentDateTime ().withMillisOfSecond (0);
    return getAsStringW3C (aNow);
  }

  @Nonnull
  private static DateTimeFormatter _getXSDFormatterDateTime (@Nonnull final Chronology aChronology)
  {
    return ISODateTimeFormat.dateTime ().withChronology (aChronology);
  }

  @Nullable
  public static DateTime getDateTimeFromXSD (@Nullable final String sValue)
  {
    return getDateTimeFromXSD (sValue, ISOChronology.getInstanceUTC ());
  }

  @Nullable
  public static DateTime getDateTimeFromXSD (@Nullable final String sValue, @Nonnull final Chronology aChronology)
  {
    return PDTFromString.getDateTimeFromString (sValue, _getXSDFormatterDateTime (aChronology));
  }

  @Nullable
  public static LocalDateTime getLocalDateTimeFromXSD (@Nullable final String sValue)
  {
    // For LocalDateTime always use the default chronology
    return PDTFromString.getLocalDateTimeFromString (sValue,
                                                     _getXSDFormatterDateTime (PDTConfig.getDefaultChronology ()));
  }

  @Nonnull
  public static String getAsStringXSD (@Nullable final DateTime aDateTime)
  {
    return getAsStringXSD (ISOChronology.getInstanceUTC (), aDateTime);
  }

  @Nonnull
  public static String getAsStringXSD (@Nonnull final Chronology aChronology, @Nullable final DateTime aDateTime)
  {
    return _getXSDFormatterDateTime (aChronology).print (aDateTime);
  }

  @Nonnull
  public static String getAsStringXSD (@Nullable final LocalDateTime aLocalDateTime)
  {
    // For LocalDateTime always use the default chronology
    return _getXSDFormatterDateTime (PDTConfig.getDefaultChronology ()).print (aLocalDateTime);
  }

  @Nonnull
  private static DateTimeFormatter _getXSDFormatterDate ()
  {
    return ISODateTimeFormat.date ().withChronology (ISOChronology.getInstanceUTC ());
  }

  @Nullable
  public static LocalDate getLocalDateFromXSD (@Nullable final String sValue)
  {
    final DateTime aDT = PDTFromString.getDateTimeFromString (sValue, _getXSDFormatterDate ());
    return aDT == null ? null : aDT.withChronology (PDTConfig.getDefaultChronologyUTC ()).toLocalDate ();
  }

  @Nonnull
  public static String getAsStringXSD (@Nullable final LocalDate aLocalDate)
  {
    return _getXSDFormatterDate ().print (aLocalDate == null ? PDTFactory.getCurrentLocalDate () : aLocalDate);
  }
}

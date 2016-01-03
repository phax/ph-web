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

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalQuery;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.collection.pair.IPair;
import com.helger.commons.collection.pair.ReadOnlyPair;
import com.helger.commons.string.StringHelper;
import com.helger.commons.typeconvert.TypeConverter;
import com.helger.datetime.config.PDTConfig;
import com.helger.datetime.format.PDTFormatter;
import com.helger.datetime.format.PDTFromString;

/**
 * A helper class that parses Dates out of Strings with date time in RFC822 and
 * W3CDateTime formats plus the variants Atom (0.3) and RSS (0.9, 0.91, 0.92,
 * 0.93, 0.94, 1.0 and 2.0) specificators added to those formats.<br>
 * It uses the JDK java.text.SimpleDateFormat class attempting the parse using a
 * mask for each one of the possible formats.<br>
 * Original work Copyright 2004 Sun Microsystems, Inc.
 *
 * @author Alejandro Abdelnur (original; mainly the formatting masks)
 * @author Philip Helger (major modification)
 */
@Immutable
public final class PDTWebDateHelper
{
  private static final class Mask <T extends Temporal>
  {
    private final String m_sPattern;
    private final TemporalQuery <T> m_aQuery;

    Mask (@Nonnull @Nonempty final String sPattern, @Nonnull final TemporalQuery <T> aQuery)
    {
      m_sPattern = sPattern;
      m_aQuery = aQuery;
    }

    @Nonnull
    static Mask <ZonedDateTime> zonedDateTime (@Nonnull @Nonempty final String sPattern)
    {
      return new Mask <ZonedDateTime> (sPattern, ZonedDateTime::from);
    }

    @Nonnull
    static Mask <OffsetDateTime> offsetDateTime (@Nonnull @Nonempty final String sPattern)
    {
      return new Mask <OffsetDateTime> (sPattern, OffsetDateTime::from);
    }

    @Nonnull
    static Mask <LocalDateTime> localDateTime (@Nonnull @Nonempty final String sPattern)
    {
      return new Mask <LocalDateTime> (sPattern, LocalDateTime::from);
    }

    @Nonnull
    static Mask <LocalDate> localDate (@Nonnull @Nonempty final String sPattern)
    {
      return new Mask <LocalDate> (sPattern, LocalDate::from);
    }

    @Nonnull
    static Mask <YearMonth> yearMonth (@Nonnull @Nonempty final String sPattern)
    {
      return new Mask <YearMonth> (sPattern, YearMonth::from);
    }

    @Nonnull
    static Mask <Year> year (@Nonnull @Nonempty final String sPattern)
    {
      return new Mask <Year> (sPattern, Year::from);
    }
  }

  private static final Logger s_aLogger = LoggerFactory.getLogger (PDTWebDateHelper.class);
  // "XX" means "+HHmm"
  // "XXX" means "+HH:mm"
  private static final String ZONE_PATTERN = "XXX";
  private static final String FORMAT_RFC822 = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
  private static final String FORMAT_W3C = "yyyy-MM-dd'T'HH:mm:ss" + ZONE_PATTERN;

  /**
   * order is like this because the SimpleDateFormat.parse does not fail with
   * exception if it can parse a valid date out of a substring of the full
   * string given the mask so we have to check the most complete format first,
   * then it fails with exception. <br>
   * An RFC superseding 822 recommends to use yyyy instead of yy
   */
  private static final Mask <?> [] RFC822_MASKS = { Mask.localDateTime (FORMAT_RFC822),
                                                    Mask.localDateTime ("EEE, dd MMM yyyy HH:mm:ss"),
                                                    Mask.localDateTime ("EEE, dd MMM yy HH:mm:ss"),
                                                    Mask.localDateTime ("EEE, dd MMM yyyy HH:mm"),
                                                    Mask.localDateTime ("EEE, dd MMM yy HH:mm"),
                                                    Mask.localDateTime ("dd MMM yyyy HH:mm:ss"),
                                                    Mask.localDateTime ("dd MMM yy HH:mm:ss"),
                                                    Mask.localDateTime ("dd MMM yyyy HH:mm"),
                                                    Mask.localDateTime ("dd MMM yy HH:mm") };

  /*
   * order is like this because the SimpleDateFormat.parse does not fail with
   * exception if it can parse a valid date out of a substring of the full
   * string given the mask so we have to check the most complete format first,
   * then it fails with exception
   */
  private static final Mask <?> [] W3CDATETIME_MASKS = { Mask.offsetDateTime ("yyyy-MM-dd'T'HH:mm:ss.SSS" +
                                                                              ZONE_PATTERN),
                                                         Mask.offsetDateTime ("yyyy-MM-dd't'HH:mm:ss.SSS" +
                                                                              ZONE_PATTERN),
                                                         Mask.localDateTime ("yyyy-MM-dd'T'HH:mm:ss.SSS"),
                                                         Mask.localDateTime ("yyyy-MM-dd't'HH:mm:ss.SSS"),
                                                         Mask.offsetDateTime (FORMAT_W3C),
                                                         Mask.offsetDateTime ("yyyy-MM-dd't'HH:mm:ss" + ZONE_PATTERN),
                                                         Mask.localDateTime ("yyyy-MM-dd'T'HH:mm:ss"),
                                                         Mask.localDateTime ("yyyy-MM-dd't'HH:mm:ss"),
                                                         Mask.offsetDateTime ("yyyy-MM-dd'T'HH:mm" + ZONE_PATTERN),
                                                         Mask.offsetDateTime ("yyyy-MM-dd't'HH:mm" + ZONE_PATTERN),
                                                         Mask.localDateTime ("yyyy-MM-dd'T'HH:mm"),
                                                         Mask.localDateTime ("yyyy-MM-dd't'HH:mm"),
                                                         /*
                                                          * Applies to the
                                                          * following 2:
                                                          * together with logic
                                                          * in the
                                                          * parseW3CDateTime
                                                          * they handle W3C
                                                          * dates without time
                                                          * forcing them to be
                                                          * GMT
                                                          */
                                                         Mask.localDateTime ("yyyy-MM'T'HH:mm"),
                                                         Mask.localDateTime ("yyyy'T'HH:mm"),
                                                         Mask.localDate ("yyyy-MM-dd"),
                                                         Mask.yearMonth ("yyyy-MM"),
                                                         Mask.year ("yyyy") };

  private static final Locale LOCALE_TO_USE = Locale.US;

  @PresentForCodeCoverage
  private static final PDTWebDateHelper s_aInstance = new PDTWebDateHelper ();

  private PDTWebDateHelper ()
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
  private static OffsetDateTime _parseDateTimeUsingMask (@Nonnull final Mask <?> [] aMasks,
                                                         @Nonnull @Nonempty final String sDate,
                                                         @Nullable final ZoneId aDTZ)
  {
    for (final Mask <?> aMask : aMasks)
    {
      DateTimeFormatter aDTF = PDTFormatter.getForPattern (aMask.m_sPattern, LOCALE_TO_USE);
      if (aDTZ != null)
        aDTF = aDTF.withZone (aDTZ);
      try
      {
        final Temporal ret = aDTF.parse (sDate, aMask.m_aQuery);
        s_aLogger.info ("Parsed '" + sDate + "' with '" + aMask.m_sPattern + "' to " + ret.getClass ().getName ());
        return TypeConverter.convertIfNecessary (ret, OffsetDateTime.class);
      }
      catch (final DateTimeParseException ex)
      {
        s_aLogger.error ("Failed to parse '" + sDate + "' with '" + aMask.m_sPattern + "': " + ex.getMessage ());
      }
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
  private static IPair <String, ZoneId> _extractDateTimeZone (@Nonnull final String sDate)
  {
    final int nDateLen = sDate.length ();
    final String [] aDTZ = { "UTC", "GMT" };
    for (final String sDTZ : aDTZ)
    {
      if (sDate.endsWith (" " + sDTZ))
        return ReadOnlyPair.create (sDate.substring (0, nDateLen - (1 + sDTZ.length ())), ZoneId.of (sDTZ));
      if (sDate.endsWith (sDTZ))
        return ReadOnlyPair.create (sDate.substring (0, nDateLen - sDTZ.length ()), ZoneId.of (sDTZ));
    }
    if (sDate.endsWith ("Z"))
      return ReadOnlyPair.create (sDate.substring (0, nDateLen - 1), ZoneOffset.UTC);
    return ReadOnlyPair.create (sDate, null);
  }

  /**
   * Parses a Date out of a String with a date in RFC822 format. <br>
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
   * <p>
   * Refer to the java.text.SimpleDateFormat javadocs for details on the format
   * of each element.
   * </p>
   *
   * @param sDate
   *        string to parse for a date. May be <code>null</code>.
   * @return the Date represented by the given RFC822 string. It returns
   *         <b>null</b> if it was not possible to parse the given string into a
   *         {@link ZonedDateTime} or if the passed {@link String} was
   *         <code>null</code>.
   */
  @Nullable
  public static OffsetDateTime getDateTimeFromRFC822 (@Nullable final String sDate)
  {
    if (StringHelper.hasNoText (sDate))
      return null;

    final IPair <String, ZoneId> aPair = _extractDateTimeZone (sDate.trim ());
    return _parseDateTimeUsingMask (RFC822_MASKS, aPair.getFirst (), aPair.getSecond ());
  }

  /**
   * Parses a Date out of a String with a date in W3C date-time format. <br>
   * It parsers the following formats:
   * <ul>
   * <li>"yyyy-MM-dd'T'HH:mm:ssz"</li>
   * <li>"yyyy-MM-dd'T'HH:mmz"</li>
   * <li>"yyyy-MM-dd"</li>
   * <li>"yyyy-MM"</li>
   * <li>"yyyy"</li>
   * </ul>
   * <p>
   * Refer to the java.text.SimpleDateFormat javadocs for details on the format
   * of each element.
   * </p>
   *
   * @param sDate
   *        string to parse for a date. May be <code>null</code>.
   * @return the Date represented by the given W3C date-time string. It returns
   *         <b>null</b> if it was not possible to parse the given string into a
   *         {@link ZonedDateTime} or if the input string was <code>null</code>.
   */
  @Nullable
  public static OffsetDateTime getDateTimeFromW3C (@Nullable final String sDate)
  {
    if (StringHelper.hasNoText (sDate))
      return null;

    final IPair <String, ZoneId> aPair = _extractDateTimeZone (sDate.trim ());
    return _parseDateTimeUsingMask (W3CDATETIME_MASKS, aPair.getFirst (), aPair.getSecond ());
  }

  /**
   * Parses a Date out of a String with a date in W3C date-time format or in a
   * RFC822 format.
   *
   * @param sDate
   *        string to parse for a date.
   * @return the Date represented by the given W3C date-time string. It returns
   *         <b>null</b> if it was not possible to parse the given string into a
   *         Date.
   */
  @Nullable
  public static OffsetDateTime getDateTimeFromW3COrRFC822 (@Nullable final String sDate)
  {
    OffsetDateTime aDateTime = getDateTimeFromW3C (sDate);
    if (aDateTime == null)
      aDateTime = getDateTimeFromRFC822 (sDate);
    return aDateTime;
  }

  /**
   * Parses a Date out of a String with a date in W3C date-time format or in a
   * RFC822 format.
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
    final OffsetDateTime aDateTime = getDateTimeFromW3COrRFC822 (sDate);
    return aDateTime == null ? null : aDateTime.toLocalDateTime ();
  }

  /**
   * create a RFC822 representation of a date.
   *
   * @param aDateTime
   *        Date to print. May be <code>null</code>.
   * @return the RFC822 represented by the given Date. <code>null</code> if the
   *         parameter is <code>null</code>.
   */
  @Nullable
  public static String getAsStringRFC822 (@Nullable final ZonedDateTime aDateTime)
  {
    if (aDateTime == null)
      return null;
    return PDTFormatter.getForPattern (FORMAT_RFC822, LOCALE_TO_USE).withZone (ZoneOffset.UTC).format (aDateTime);
  }

  /**
   * create a RFC822 representation of a date.
   *
   * @param aDateTime
   *        Date to print. May be <code>null</code>.
   * @return the RFC822 represented by the given Date. <code>null</code> if the
   *         parameter is <code>null</code>.
   */
  @Nullable
  public static String getAsStringRFC822 (@Nullable final OffsetDateTime aDateTime)
  {
    if (aDateTime == null)
      return null;
    return PDTFormatter.getForPattern (FORMAT_RFC822, LOCALE_TO_USE).withZone (ZoneOffset.UTC).format (aDateTime);
  }

  /**
   * create a RFC822 representation of a date time using UTC date time zone.
   *
   * @param aDateTime
   *        Date to print. May be <code>null</code>.
   * @return the RFC822 represented by the given Date. <code>null</code> if the
   *         parameter is <code>null</code>.
   */
  @Nullable
  public static String getAsStringRFC822 (@Nullable final LocalDateTime aDateTime)
  {
    if (aDateTime == null)
      return null;
    return PDTFormatter.getForPattern (FORMAT_RFC822, LOCALE_TO_USE).withZone (ZoneOffset.UTC).format (aDateTime);
  }

  /**
   * create a W3C Date Time representation of a date.
   *
   * @param aDateTime
   *        Date to print. May not be <code>null</code>.
   * @return the W3C Date Time represented by the given Date.
   */
  @Nonnull
  public static String getAsStringW3C (@Nonnull final LocalDateTime aDateTime)
  {
    final DateTimeFormatter aFormatter = PDTFormatter.getForPattern (FORMAT_W3C, LOCALE_TO_USE);
    return aFormatter.format (aDateTime.withNano (0));
  }

  /**
   * create a W3C Date Time representation of a date time using UTC date time
   * zone.
   *
   * @param aDateTime
   *        Date to print. May not be <code>null</code>.
   * @return the W3C Date Time represented by the given Date.
   */
  @Nonnull
  public static String getAsStringW3C (@Nonnull final ZonedDateTime aDateTime)
  {
    final DateTimeFormatter aFormatter = PDTFormatter.getForPattern (FORMAT_W3C, LOCALE_TO_USE);
    return aFormatter.format (aDateTime.withNano (0));
  }

  /**
   * create a W3C Date Time representation of a date time using UTC date time
   * zone.
   *
   * @param aDateTime
   *        Date to print. May not be <code>null</code>.
   * @return the W3C Date Time represented by the given Date.
   */
  @Nonnull
  public static String getAsStringW3C (@Nonnull final OffsetDateTime aDateTime)
  {
    final DateTimeFormatter aFormatter = PDTFormatter.getForPattern (FORMAT_W3C, LOCALE_TO_USE);
    return aFormatter.format (aDateTime.withNano (0));
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
    final ZonedDateTime aNow = ZonedDateTime.now (Clock.systemUTC ()).withNano (0);
    return getAsStringRFC822 (aNow);
  }

  /**
   * @return The current date time formatted using W3C format
   */
  @Nonnull
  public static String getCurrentDateTimeAsStringW3C ()
  {
    // Use no milli seconds as the standard printer does not print them!
    final ZonedDateTime aNow = ZonedDateTime.now ().withNano (0);
    return getAsStringW3C (aNow);
  }

  @Nonnull
  private static DateTimeFormatter _getXSDFormatterDateTime (@Nonnull final ZoneId aZoneID)
  {
    return DateTimeFormatter.ISO_DATE_TIME.withZone (aZoneID);
  }

  @Nullable
  public static ZonedDateTime getDateTimeFromXSD (@Nullable final String sValue)
  {
    return getDateTimeFromXSD (sValue, ZoneOffset.UTC);
  }

  @Nullable
  public static ZonedDateTime getDateTimeFromXSD (@Nullable final String sValue, @Nonnull final ZoneId aZoneID)
  {
    return PDTFromString.getDateTimeFromString (sValue, _getXSDFormatterDateTime (aZoneID));
  }

  @Nullable
  public static LocalDateTime getLocalDateTimeFromXSD (@Nullable final String sValue)
  {
    // For LocalDateTime always use the default chronology
    return PDTFromString.getLocalDateTimeFromString (sValue, _getXSDFormatterDateTime (ZoneOffset.UTC));
  }

  @Nonnull
  public static String getAsStringXSD (@Nonnull final ZonedDateTime aDateTime)
  {
    return getAsStringXSD (ZoneOffset.UTC, aDateTime);
  }

  @Nonnull
  public static String getAsStringXSD (@Nonnull final ZoneId aZoneID, @Nonnull final ZonedDateTime aDateTime)
  {
    return _getXSDFormatterDateTime (aZoneID).format (aDateTime);
  }

  @Nonnull
  public static String getAsStringXSD (@Nullable final LocalDateTime aLocalDateTime)
  {
    // For LocalDateTime always use the default chronology
    return _getXSDFormatterDateTime (PDTConfig.getDefaultZoneId ()).format (aLocalDateTime);
  }

  @Nonnull
  private static DateTimeFormatter _getXSDFormatterDate ()
  {
    return DateTimeFormatter.ISO_DATE.withZone (ZoneOffset.UTC);
  }

  @Nullable
  public static LocalDate getLocalDateFromXSD (@Nullable final String sValue)
  {
    return PDTFromString.getLocalDateFromString (sValue, _getXSDFormatterDate ());
  }

  @Nonnull
  public static String getAsStringXSD (@Nonnull final LocalDate aLocalDate)
  {
    return _getXSDFormatterDate ().format (aLocalDate);
  }
}

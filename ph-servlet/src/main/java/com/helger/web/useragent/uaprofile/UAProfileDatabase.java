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
package com.helger.web.useragent.uaprofile;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.regex.Matcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.base64.Base64;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.CommonsHashMap;
import com.helger.commons.collection.ext.CommonsHashSet;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.collection.ext.ICommonsMap;
import com.helger.commons.collection.ext.ICommonsSet;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;
import com.helger.commons.url.URLHelper;
import com.helger.servlet.ServletHelper;
import com.helger.servlet.request.RequestHelper;

/**
 * Central cache for known UAProfiles.
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class UAProfileDatabase
{
  // See http://de.wikipedia.org/wiki/UAProf - references the device URL
  public static final String X_WAP_PROFILE = "X-Wap-Profile";
  public static final String PROFILE = "Profile";
  public static final String WAP_PROFILE = "Wap-Profile";
  public static final String MAN = "Man";
  public static final String OPT = "Opt";

  public static final String X_WAP_PROFILE_DIFF = "X-Wap-Profile-Diff";
  public static final String PROFILE_DIFF = "Profile-Diff";
  public static final String WAP_PROFILE_DIFF = "Wap-Profile-Diff";

  public static final int EXPECTED_MD5_DIGEST_LENGTH = 16;

  private static final String REQUEST_ATTR = UAProfileDatabase.class.getName ();
  private static final Logger s_aLogger = LoggerFactory.getLogger (UAProfileDatabase.class);

  private static final SimpleReadWriteLock s_aRWLock = new SimpleReadWriteLock ();
  @GuardedBy ("s_aRWLock")
  private static final ICommonsSet <UAProfile> s_aUniqueUAProfiles = new CommonsHashSet<> ();
  @GuardedBy ("s_aRWLock")
  private static Consumer <UAProfile> s_aNewUAProfileCallback;

  @PresentForCodeCoverage
  private static final UAProfileDatabase s_aInstance = new UAProfileDatabase ();

  private UAProfileDatabase ()
  {}

  @Nullable
  public static Consumer <UAProfile> getNewUAProfileCallback ()
  {
    return s_aRWLock.readLocked ( () -> s_aNewUAProfileCallback);
  }

  public static void setNewUAProfileCallback (@Nullable final Consumer <UAProfile> aCallback)
  {
    s_aRWLock.writeLocked ( () -> s_aNewUAProfileCallback = aCallback);
  }

  @Nullable
  private static String _getExtendedNamespaceValue (@Nonnull final String sOpt)
  {
    final Matcher aMatcher = RegExHelper.getMatcher (".+ns=(\\d+).*", sOpt);
    return aMatcher.matches () ? aMatcher.group (1) : null;
  }

  @Nonnull
  private static String _getUnifiedHeaderName (@Nonnull final String s)
  {
    return s.toLowerCase (Locale.US);
  }

  @Nullable
  private static String _getCleanedUp (@Nullable final String s)
  {
    if (StringHelper.hasNoText (s))
      return s;

    // trim string
    String sValue = s.trim ();

    // Cut surrounding quotes (if any)
    if (StringHelper.getFirstChar (sValue) == '"')
      sValue = sValue.substring (1);
    if (StringHelper.getLastChar (sValue) == '"')
      sValue = sValue.substring (0, sValue.length () - 1);
    return sValue;
  }

  @Nonnull
  private static ICommonsMap <Integer, String> _getProfileDiffData (@Nonnull final HttpServletRequest aHttpRequest,
                                                                    final String sExtNSValue)
  {
    // Determine the profile diffs to use
    Enumeration <String> aProfileDiffs = RequestHelper.getRequestHeaders (aHttpRequest, X_WAP_PROFILE_DIFF);
    if (!aProfileDiffs.hasMoreElements ())
    {
      aProfileDiffs = RequestHelper.getRequestHeaders (aHttpRequest, PROFILE_DIFF);
      if (!aProfileDiffs.hasMoreElements ())
        aProfileDiffs = RequestHelper.getRequestHeaders (aHttpRequest, WAP_PROFILE_DIFF);
    }

    // Parse the diffs
    final ICommonsMap <Integer, String> aProfileDiffData = new CommonsHashMap<> ();
    while (aProfileDiffs.hasMoreElements ())
    {
      String sProfileDiff = aProfileDiffs.nextElement ();
      sProfileDiff = sProfileDiff.trim ();

      // Find the profile diff index (e.g. '1;<?xml....')
      final int nSemicolonIndex = sProfileDiff.indexOf (';');
      if (nSemicolonIndex == -1)
      {
        s_aLogger.warn ("Failed to find ';' in profile diff header value '" + sProfileDiff + "'!");
        continue;
      }
      final String sProfileDiffIndex = sProfileDiff.substring (0, nSemicolonIndex);
      final int nIndex = StringParser.parseInt (sProfileDiffIndex, CGlobal.ILLEGAL_UINT);
      if (nIndex == CGlobal.ILLEGAL_UINT)
      {
        s_aLogger.warn ("Failed to convert UAProf difference index '" + sProfileDiffIndex + "' to a number!");
        continue;
      }

      // Cut the leading number
      sProfileDiff = sProfileDiff.substring (nSemicolonIndex + 1).trim ();
      aProfileDiffData.put (Integer.valueOf (nIndex), sProfileDiff);
    }

    if (aProfileDiffData.isEmpty () && sExtNSValue != null)
    {
      // Scan for CCPP profile diff data
      final String sPrefix = _getUnifiedHeaderName (sExtNSValue + "-Profile-Diff-");

      // Extract all matching headers, in case non-consecutive numbers are used
      final Enumeration <String> aAllHeaders = RequestHelper.getRequestHeaderNames (aHttpRequest);
      while (aAllHeaders.hasMoreElements ())
      {
        final String sHeaderName = _getUnifiedHeaderName (aAllHeaders.nextElement ());
        if (sHeaderName.startsWith (sPrefix))
        {
          // We found a matching profile-diff header (e.g. "80-Profile-Diff-1")
          final int nIndex = StringParser.parseInt (sHeaderName.substring (sPrefix.length ()), CGlobal.ILLEGAL_UINT);
          if (nIndex != CGlobal.ILLEGAL_UINT)
          {
            // Handle profile diff
            String sProfileDiff = aHttpRequest.getHeader (sHeaderName);
            sProfileDiff = _getCleanedUp (sProfileDiff);
            aProfileDiffData.put (Integer.valueOf (nIndex), sProfileDiff);
          }
          else
            s_aLogger.warn ("Failed to extract numerical number from header name '" + sHeaderName + "'");
        }
      }
    }
    return aProfileDiffData;
  }

  @Nullable
  public static UAProfile getUAProfileFromRequest (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "httpRequest");

    // Determine the main profile to use
    String sExtNSValue = null;
    Enumeration <String> aProfiles = RequestHelper.getRequestHeaders (aHttpRequest, X_WAP_PROFILE);
    if (!aProfiles.hasMoreElements ())
    {
      aProfiles = RequestHelper.getRequestHeaders (aHttpRequest, PROFILE);
      if (!aProfiles.hasMoreElements ())
      {
        aProfiles = RequestHelper.getRequestHeaders (aHttpRequest, WAP_PROFILE);
        if (!aProfiles.hasMoreElements ())
        {
          // Check CCPP headers
          String sExt = aHttpRequest.getHeader (OPT);
          if (sExt == null)
            sExt = aHttpRequest.getHeader (MAN);
          if (sExt != null)
          {
            sExtNSValue = _getExtendedNamespaceValue (sExt);
            if (sExtNSValue != null)
            {
              aProfiles = RequestHelper.getRequestHeaders (aHttpRequest, sExtNSValue + "-Profile");
              if (!aProfiles.hasMoreElements ())
                s_aLogger.warn ("Found CCPP header namespace '" + sExtNSValue + "' but found no profile header!");
            }
            else
              s_aLogger.warn ("Failed to extract namespace value from CCPP header '" + sExt + "'");
          }
        }
      }
    }

    // Parse profile headers
    final ICommonsList <String> aProfileData = new CommonsArrayList<> ();
    final ICommonsMap <Integer, byte []> aProfileDiffDigests = new CommonsHashMap<> ();
    while (aProfiles.hasMoreElements ())
    {
      String sProfile = aProfiles.nextElement ();
      sProfile = _getCleanedUp (sProfile);
      if (StringHelper.hasText (sProfile))
      {
        // Start tokenizing. Example (with stripped leading and trailing
        // quotes):
        // http://www.ex.com/hw," "1-CWccARHXxtYJE+rKkoD8ng==
        final StringTokenizer aTokenizer = new StringTokenizer (sProfile, "\",");
        while (aTokenizer.hasMoreTokens ())
        {
          final String sToken = aTokenizer.nextToken ().trim ();
          if (StringHelper.hasText (sToken))
          {
            final Matcher aMatcher = RegExHelper.getMatcher ("^(\\d+)-(.+)$", sToken);
            if (aMatcher.matches ())
            {
              // It seems to be a profile diff digest
              final String sDiffIndex = aMatcher.group (1);
              final String sDiffDigest = aMatcher.group (2);
              final int nDiffIndex = StringParser.parseInt (sDiffIndex, CGlobal.ILLEGAL_UINT);
              if (nDiffIndex != CGlobal.ILLEGAL_UINT)
              {
                if (StringHelper.hasText (sDiffDigest))
                {
                  final byte [] aDigest = Base64.safeDecode (sDiffDigest);
                  if (aDigest != null)
                  {
                    // MD5 hashes have 16 bytes!
                    if (aDigest.length == EXPECTED_MD5_DIGEST_LENGTH)
                      aProfileDiffDigests.put (Integer.valueOf (nDiffIndex), aDigest);
                    else
                      s_aLogger.warn ("Decoded Base64 profile diff digest has an illegal length of " + aDigest.length);
                  }
                  else
                    s_aLogger.warn ("Failed to decode Base64 profile diff digest '" +
                                    sDiffDigest +
                                    "' from token '" +
                                    sToken +
                                    "'");
                }
                else
                  s_aLogger.warn ("Found no diff digest in token '" + sToken + "'");
              }
              else
                s_aLogger.warn ("Failed to parse profile diff index from '" + sToken + "'");
            }
            else
            {
              // Assume it is a URL
              if (URLHelper.getAsURL (sToken) != null)
                aProfileData.add (sToken);
              else
                s_aLogger.error ("Failed to convert profile token '" + sToken + "' to a URL!");
            }
          }
        }
      }
    }

    if (aProfileData.isEmpty () && aProfileDiffDigests.isEmpty ())
    {
      // No UAProfile found -> no need to look for differences
      return null;
    }

    // Read diffs
    final ICommonsMap <Integer, String> aProfileDiffData = _getProfileDiffData (aHttpRequest, sExtNSValue);

    // Merge data and digest
    final ICommonsMap <Integer, UAProfileDiff> aProfileDiffs = new CommonsHashMap<> ();
    for (final Map.Entry <Integer, String> aEntry : aProfileDiffData.entrySet ())
    {
      final Integer aIndex = aEntry.getKey ();
      final byte [] aDigest = aProfileDiffDigests.get (aIndex);
      if (aDigest != null)
      {
        // Found a matching entry
        aProfileDiffs.put (aIndex, new UAProfileDiff (aEntry.getValue (), aDigest));
      }
      else
        s_aLogger.warn ("Found profile diff data but no digest for index " + aIndex);
    }

    // Consistency check
    for (final Integer aIndex : aProfileDiffDigests.keySet ())
      if (!aProfileDiffData.containsKey (aIndex))
        s_aLogger.warn ("Found profile diff digest but no data for index " + aIndex);

    if (aProfileData.isEmpty () && aProfileDiffs.isEmpty ())
    {
      // This can happen if a diff digest was found, but the diff data is
      // missing!
      return null;
    }

    // And we're done
    return new UAProfile (aProfileData, aProfileDiffs);
  }

  /**
   * Get the user agent object from the given HTTP request.
   *
   * @param aHttpRequest
   *        The HTTP request to extract the information from.
   * @return A non-<code>null</code> user agent object.
   */
  @Nonnull
  public static UAProfile getUAProfile (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    UAProfile aUAProfile = (UAProfile) aHttpRequest.getAttribute (REQUEST_ATTR);
    if (aUAProfile == null)
    {
      // Extract HTTP header from request
      aUAProfile = getUAProfileFromRequest (aHttpRequest);
      if (aUAProfile == null)
        aUAProfile = UAProfile.EMPTY;

      ServletHelper.setRequestAttribute (aHttpRequest, REQUEST_ATTR, aUAProfile);
      if (aUAProfile.isSet ())
      {
        final UAProfile aFinalUAProfile = aUAProfile;
        s_aRWLock.writeLocked ( () -> {
          if (s_aUniqueUAProfiles.add (aFinalUAProfile))
          {
            if (s_aLogger.isDebugEnabled ())
              s_aLogger.debug ("Found UA-Profile info: " + aFinalUAProfile.toString ());

            if (s_aNewUAProfileCallback != null)
              s_aNewUAProfileCallback.accept (aFinalUAProfile);
          }
        });
      }
    }
    return aUAProfile;
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsSet <UAProfile> getAllUniqueUAProfiles ()
  {
    return s_aRWLock.readLocked ( () -> s_aUniqueUAProfiles.getClone ());
  }
}

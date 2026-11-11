/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
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
package com.helger.servlet.logging;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.PresentForCodeCoverage;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.http.CHttpHeader;
import com.helger.http.header.HttpHeaderMap;

/**
 * Helper class to mask the values of HTTP headers that contain credentials or session information
 * (like <code>Authorization</code> or <code>Cookie</code>) before they are logged.
 *
 * @author Philip Helger
 * @since 11.5.0
 */
@Immutable
public final class SensitiveHeaderHelper
{
  /** The value used instead of the original value of a sensitive header */
  public static final String MASKED_VALUE = "***";

  private static final String [] SENSITIVE_HEADERS = { CHttpHeader.AUTHORIZATION,
                                                       CHttpHeader.PROXY_AUTHORIZATION,
                                                       CHttpHeader.COOKIE,
                                                       CHttpHeader.SET_COOKIE };

  @PresentForCodeCoverage
  private static final SensitiveHeaderHelper INSTANCE = new SensitiveHeaderHelper ();

  private SensitiveHeaderHelper ()
  {}

  /**
   * Check if the provided HTTP header name denotes a header with sensitive content. The check is
   * case insensitive.
   *
   * @param sHeaderName
   *        The header name to check. May be <code>null</code>.
   * @return <code>true</code> if the value of the header must not be logged, <code>false</code> if
   *         not.
   */
  public static boolean isSensitiveHeader (@Nullable final String sHeaderName)
  {
    if (sHeaderName != null)
      for (final String sSensitive : SENSITIVE_HEADERS)
        if (sSensitive.equalsIgnoreCase (sHeaderName))
          return true;
    return false;
  }

  /**
   * Get a copy of the provided header map, in which the values of all sensitive headers are
   * replaced with {@link #MASKED_VALUE}.
   *
   * @param aHeaderMap
   *        The source header map. May not be <code>null</code>.
   * @return A new header map and never <code>null</code>.
   * @see #isSensitiveHeader(String)
   */
  @NonNull
  @ReturnsMutableCopy
  public static HttpHeaderMap getMaskedHeaderMap (@NonNull final HttpHeaderMap aHeaderMap)
  {
    ValueEnforcer.notNull (aHeaderMap, "HeaderMap");

    final HttpHeaderMap ret = new HttpHeaderMap ();
    for (final Map.Entry <String, ? extends List <String>> aEntry : aHeaderMap.getAllHeaders ().entrySet ())
    {
      final String sName = aEntry.getKey ();
      final boolean bSensitive = isSensitiveHeader (sName);
      for (final String sValue : aEntry.getValue ())
        ret.addHeader (sName, bSensitive ? MASKED_VALUE : sValue);
    }
    return ret;
  }
}

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
package com.helger.useragent.uaprofile;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.collection.ext.CommonsTreeMap;
import com.helger.commons.collection.ext.ICommonsSortedMap;
import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;

/**
 * This class represents a single UA profile.
 *
 * @author Philip Helger
 */
@Immutable
public class UAProfile implements Serializable
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (UAProfile.class);
  public static final UAProfile EMPTY = new UAProfile ();

  private final String m_sProfileUrl;
  private final ICommonsSortedMap <Integer, UAProfileDiff> m_aProfileDiffData;

  private UAProfile ()
  {
    m_sProfileUrl = null;
    m_aProfileDiffData = null;
  }

  public UAProfile (@Nullable final List <String> aProfileUrls,
                    @Nullable final Map <Integer, UAProfileDiff> aProfileDiffData)
  {
    final int nUrls = CollectionHelper.getSize (aProfileUrls);
    final int nDiffs = CollectionHelper.getSize (aProfileDiffData);
    if (nUrls == 0 && nDiffs == 0)
      throw new IllegalArgumentException ("Neither profile nor diff data found!");
    if (nUrls > 1)
      s_aLogger.warn ("Found more than one profile URL: " + aProfileUrls);
    m_sProfileUrl = CollectionHelper.getFirstElement (aProfileUrls);
    m_aProfileDiffData = CollectionHelper.isEmpty (aProfileDiffData) ? null : new CommonsTreeMap<> (aProfileDiffData);
  }

  /**
   * @return The best match UA profile URL to use. May be <code>null</code> if
   *         only profile diffs are present.
   */
  @Nullable
  public String getProfileURL ()
  {
    return m_sProfileUrl;
  }

  @Nonnegative
  public int getDiffCount ()
  {
    // May be null
    return CollectionHelper.getSize (m_aProfileDiffData);
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsSortedMap <Integer, UAProfileDiff> getProfileDiffData ()
  {
    // May be null
    return new CommonsTreeMap<> (m_aProfileDiffData);
  }

  public boolean isSet ()
  {
    return m_sProfileUrl != null || m_aProfileDiffData != null;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final UAProfile rhs = (UAProfile) o;
    return EqualsHelper.equals (m_sProfileUrl, rhs.m_sProfileUrl) &&
           EqualsHelper.equals (m_aProfileDiffData, rhs.m_aProfileDiffData);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sProfileUrl).append (m_aProfileDiffData).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).appendIfNotNull ("profileUrl", m_sProfileUrl)
                                       .appendIfNotNull ("profileDiff", m_aProfileDiffData)
                                       .toString ();
  }
}

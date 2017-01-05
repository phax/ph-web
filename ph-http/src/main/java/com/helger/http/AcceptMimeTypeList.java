/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.http;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.mime.IMimeType;

/**
 * Represents a list of Accept HTTP header values
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class AcceptMimeTypeList extends AbstractQValueList <IMimeType>
{
  public AcceptMimeTypeList ()
  {}

  public void addMimeType (@Nonnull final IMimeType aMimeType, @Nonnegative final double dQuality)
  {
    ValueEnforcer.notNull (aMimeType, "MimeType");
    if (aMimeType.hasAnyParameters ())
      throw new IllegalArgumentException ("MimeTypes used here may not contain any parameter!");

    m_aMap.put (aMimeType, new QValue (dQuality));
  }

  /**
   * Return the associated quality of the given MIME type using the fallback
   * mechanism.
   *
   * @param aMimeType
   *        The charset name to query. May not be <code>null</code>.
   * @return The {@link QValue} of the mime type
   */
  @Nonnull
  public QValue getQValueOfMimeType (@Nonnull final IMimeType aMimeType)
  {
    ValueEnforcer.notNull (aMimeType, "MimeType");

    // Extract only the real MIME type, without any parameters!
    final IMimeType aRealMimeType = aMimeType.getCopyWithoutParameters ();

    QValue aQuality = m_aMap.get (aRealMimeType);
    if (aQuality == null)
    {
      // Check for "contenttype/*"
      aQuality = m_aMap.get (aRealMimeType.getContentType ().buildMimeType ("*"));
      if (aQuality == null)
      {
        // If not explicitly given, check for "*"
        aQuality = m_aMap.get (AcceptMimeTypeHandler.ANY_MIMETYPE);
        if (aQuality == null)
        {
          // Neither charset nor "*" nor "*/*" is present
          return QValue.MIN_QVALUE;
        }
      }
    }
    return aQuality;
  }

  /**
   * Return the associated quality of the given MIME type using the fallback
   * mechanism.
   *
   * @param sMimeType
   *        The MIME type name to query. May be <code>null</code>.
   * @return 0 means not accepted, 1 means fully accepted. If the passed MIME
   *         type is invalid, the "not accepted" quality is returned.
   */
  public double getQualityOfMimeType (@Nonnull final String sMimeType)
  {
    return getQualityOfMimeType (AcceptMimeTypeHandler.safeParseMimeType (sMimeType));
  }

  /**
   * Return the associated quality of the given MIME type using the fallback
   * mechanism.
   *
   * @param aMimeType
   *        The MIME type to query. May be <code>null</code>.
   * @return 0 means not accepted, 1 means fully accepted. If the passed MIME
   *         type is <code>null</code>, the "not accepted" quality is returned.
   */
  public double getQualityOfMimeType (@Nullable final IMimeType aMimeType)
  {
    if (aMimeType == null)
      return QValue.MIN_QUALITY;
    return getQValueOfMimeType (aMimeType).getQuality ();
  }

  /**
   * Check if the passed MIME type is supported, incl. fallback handling
   *
   * @param sMimeType
   *        The MIME type to check
   * @return <code>true</code> if it is supported, <code>false</code> if not
   */
  public boolean supportsMimeType (@Nonnull final String sMimeType)
  {
    return supportsMimeType (AcceptMimeTypeHandler.safeParseMimeType (sMimeType));
  }

  /**
   * Check if the passed MIME type is supported, incl. fallback handling
   *
   * @param aMimeType
   *        The MIME type to check. May be <code>null</code>.
   * @return <code>true</code> if it is supported, <code>false</code> if not
   */
  public boolean supportsMimeType (@Nullable final IMimeType aMimeType)
  {
    if (aMimeType == null)
      return false;
    return getQValueOfMimeType (aMimeType).isAboveMinimumQuality ();
  }

  /**
   * Check if the passed MIME type is supported, without considering fallback
   * MIME types (xxx/*)
   *
   * @param sMimeType
   *        The MIME type to check
   * @return <code>true</code> if it is supported, <code>false</code> if not
   */
  public boolean explicitlySupportsMimeType (@Nonnull final String sMimeType)
  {
    return explicitlySupportsMimeType (AcceptMimeTypeHandler.safeParseMimeType (sMimeType));
  }

  /**
   * Check if the passed MIME type is supported, without considering fallback
   * MIME types (xxx/*)
   *
   * @param aMimeType
   *        The MIME type to check. May be <code>null</code>.
   * @return <code>true</code> if it is supported, <code>false</code> if not
   */
  public boolean explicitlySupportsMimeType (@Nullable final IMimeType aMimeType)
  {
    if (aMimeType == null)
      return false;
    final QValue aQuality = m_aMap.get (aMimeType);
    return aQuality != null && aQuality.isAboveMinimumQuality ();
  }
}

/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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
package com.helger.http.basicauth;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.string.ToStringGenerator;
import com.helger.http.HttpStringHelper;

@NotThreadSafe
public class BasicAuthServerBuilder implements Serializable
{
  private String m_sRealm;

  public BasicAuthServerBuilder ()
  {}

  /**
   * Set the realm to be used.
   *
   * @param sRealm
   *        The realm to be used. May not be <code>null</code> and should not be
   *        empty.
   * @return this
   */
  @Nonnull
  public BasicAuthServerBuilder setRealm (@Nonnull final String sRealm)
  {
    ValueEnforcer.isTrue (HttpStringHelper.isQuotedTextContent (sRealm), () -> "Realm is invalid: " + sRealm);

    m_sRealm = sRealm;
    return this;
  }

  public boolean isValid ()
  {
    return m_sRealm != null;
  }

  @Nonnull
  @Nonempty
  public String build ()
  {
    if (!isValid ())
      throw new IllegalStateException ("Built Basic auth is not valid!");
    final StringBuilder ret = new StringBuilder (HttpBasicAuth.HEADER_VALUE_PREFIX_BASIC);
    if (m_sRealm != null)
      ret.append (" realm=").append (HttpStringHelper.getQuotedTextString (m_sRealm));
    return ret.toString ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("realm", m_sRealm).getToString ();
  }
}

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
package com.helger.http.tls;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;

/**
 * Standalone implementation of {@link ITLSConfigurationMode}.
 *
 * @author Philip Helger
 * @since 9.0.5
 */
public class TLSConfigurationMode implements ITLSConfigurationMode
{
  // Order is important
  private final ICommonsList <ETLSVersion> m_aTLSVersions;
  private final ICommonsList <String> m_aCipherSuites;

  public TLSConfigurationMode (@Nonnull @Nonempty final ETLSVersion [] aTLSVersions,
                               @Nonnull @Nonempty final String [] aCipherSuites)
  {
    ValueEnforcer.notNull (aTLSVersions, "TLSVersions");
    ValueEnforcer.notNull (aCipherSuites, "CipherSuites");
    m_aTLSVersions = new CommonsArrayList <> (aTLSVersions);
    m_aCipherSuites = new CommonsArrayList <> (aCipherSuites);
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <ETLSVersion> getAllTLSVersions ()
  {
    return m_aTLSVersions.getClone ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <String> getAllTLSVersionIDs ()
  {
    return m_aTLSVersions.getAllMapped (ETLSVersion::getID);
  }

  @Nonnull
  @ReturnsMutableCopy
  public String [] getAllTLSVersionIDsAsArray ()
  {
    return getAllTLSVersionIDs ().toArray (new String [m_aTLSVersions.size ()]);
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <String> getAllCipherSuites ()
  {
    return m_aCipherSuites.getClone ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public String [] getAllCipherSuitesAsArray ()
  {
    return m_aCipherSuites.toArray (new String [m_aCipherSuites.size ()]);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final TLSConfigurationMode rhs = (TLSConfigurationMode) o;
    return m_aTLSVersions.equals (rhs.m_aTLSVersions) && m_aCipherSuites.equals (rhs.m_aCipherSuites);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aTLSVersions).append (m_aCipherSuites).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("TLSVersions", m_aTLSVersions)
                                       .append ("CipherSuites", m_aCipherSuites)
                                       .getToString ();
  }
}

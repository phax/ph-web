/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.servlet.mock;

import java.util.Enumeration;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.NotThreadSafe;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.state.EChange;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.commons.CommonsLinkedHashMap;
import com.helger.collection.commons.ICommonsOrderedMap;
import com.helger.collection.enumeration.EnumerationHelper;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

/**
 * Mock implementation of the {@link ServletConfig} interface.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class MockServletConfig implements ServletConfig
{
  private final ServletContext m_aSC;
  private final String m_sServletName;
  private final ICommonsOrderedMap <String, String> m_aServletInitParams = new CommonsLinkedHashMap <> ();

  /**
   * Constructor without servlet init parameters.
   *
   * @param aSC
   *        Base servlet context. May not be <code>null</code>.
   * @param sServletName
   *        Name of the servlet. May neither be <code>null</code> nor empty.
   */
  public MockServletConfig (@NonNull final ServletContext aSC, @NonNull @Nonempty final String sServletName)
  {
    this (aSC, sServletName, null);
  }

  /**
   * Constructor
   *
   * @param aSC
   *        Base servlet context. May not be <code>null</code>.
   * @param sServletName
   *        Name of the servlet. May neither be <code>null</code> nor empty.
   * @param aServletInitParams
   *        The map with all servlet init parameters. May be <code>null</code> or empty.
   */
  public MockServletConfig (@NonNull final ServletContext aSC,
                            @NonNull @Nonempty final String sServletName,
                            @Nullable final Map <String, String> aServletInitParams)
  {
    m_aSC = ValueEnforcer.notNull (aSC, "ServletContext");
    m_sServletName = ValueEnforcer.notEmpty (sServletName, "ServletName");
    if (aServletInitParams != null)
      m_aServletInitParams.putAll (aServletInitParams);
  }

  @NonNull
  @Nonempty
  public String getServletName ()
  {
    return m_sServletName;
  }

  @NonNull
  public ServletContext getServletContext ()
  {
    return m_aSC;
  }

  @Nullable
  public String getInitParameter (@Nullable final String sName)
  {
    return m_aServletInitParams.get (sName);
  }

  @NonNull
  public Enumeration <String> getInitParameterNames ()
  {
    return EnumerationHelper.getEnumeration (m_aServletInitParams.keySet ());
  }

  public void addInitParameter (@NonNull @Nonempty final String sName, @NonNull final String sValue)
  {
    ValueEnforcer.notEmpty (sName, "Name");
    ValueEnforcer.notNull (sValue, "Value");
    m_aServletInitParams.put (sName, sValue);
  }

  @NonNull
  public EChange removeInitParameter (@Nullable final String sName)
  {
    return EChange.valueOf (m_aServletInitParams.remove (sName) != null);
  }

  @NonNull
  @Nonempty
  @ReturnsMutableCopy
  public ICommonsOrderedMap <String, String> getAllInitParameters ()
  {
    return m_aServletInitParams.getClone ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("servletContext", m_aSC)
                                       .append ("servletName", m_sServletName)
                                       .append ("servletInitParams", m_aServletInitParams)
                                       .getToString ();
  }
}

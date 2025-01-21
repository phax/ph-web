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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.IteratorHelper;
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.state.EChange;
import com.helger.commons.string.ToStringGenerator;

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
  public MockServletConfig (@Nonnull final ServletContext aSC, @Nonnull @Nonempty final String sServletName)
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
   *        The map with all servlet init parameters. May be <code>null</code>
   *        or empty.
   */
  public MockServletConfig (@Nonnull final ServletContext aSC,
                            @Nonnull @Nonempty final String sServletName,
                            @Nullable final Map <String, String> aServletInitParams)
  {
    m_aSC = ValueEnforcer.notNull (aSC, "ServletContext");
    m_sServletName = ValueEnforcer.notEmpty (sServletName, "ServletName");
    if (aServletInitParams != null)
      m_aServletInitParams.putAll (aServletInitParams);
  }

  @Nonnull
  @Nonempty
  public String getServletName ()
  {
    return m_sServletName;
  }

  @Nonnull
  public ServletContext getServletContext ()
  {
    return m_aSC;
  }

  @Nullable
  public String getInitParameter (@Nullable final String sName)
  {
    return m_aServletInitParams.get (sName);
  }

  @Nonnull
  public Enumeration <String> getInitParameterNames ()
  {
    return IteratorHelper.getEnumeration (m_aServletInitParams.keySet ());
  }

  public void addInitParameter (@Nonnull @Nonempty final String sName, @Nonnull final String sValue)
  {
    ValueEnforcer.notEmpty (sName, "Name");
    ValueEnforcer.notNull (sValue, "Value");
    m_aServletInitParams.put (sName, sValue);
  }

  @Nonnull
  public EChange removeInitParameter (@Nullable final String sName)
  {
    return EChange.valueOf (m_aServletInitParams.remove (sName) != null);
  }

  @Nonnull
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

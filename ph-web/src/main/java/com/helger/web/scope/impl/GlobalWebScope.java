/**
 * Copyright (C) 2014-2019 Philip Helger (www.helger.com)
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
package com.helger.web.scope.impl;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletContext;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;
import com.helger.scope.GlobalScope;
import com.helger.servlet.ServletContextPathHolder;
import com.helger.web.scope.IGlobalWebScope;

/**
 * Implementation of the {@link IGlobalWebScope} interface for web applications.
 * <br>
 * Note: for synchronization issues, this class does not store the attributes in
 * the passed {@link ServletContext} but in a separate map.
 *
 * @author Philip Helger
 */
@ThreadSafe
public class GlobalWebScope extends GlobalScope implements IGlobalWebScope
{
  // Because of transient field
  private static final long serialVersionUID = 15665138713664L;

  private final LocalDateTime m_aCreationDT;
  private final transient ServletContext m_aSC;

  @Nonnull
  @Nonempty
  private static String _createScopeID (@Nonnull final ServletContext aServletContext)
  {
    ValueEnforcer.notNull (aServletContext, "ServletContext");
    String ret = aServletContext.getServletContextName ();
    if (ret == null)
    {
      // <display-name> element is missing in web.xml
      ret = "ph-global-web-scope";
    }
    return ret;
  }

  /**
   * Create a new {@link GlobalWebScope}. No objects are copied from the passed
   * {@link ServletContext} so this must be one of the very first action
   *
   * @param aServletContext
   *        The servlet context to use. May not be <code>null</code>.
   */
  public GlobalWebScope (@Nonnull final ServletContext aServletContext)
  {
    super (_createScopeID (aServletContext));

    m_aCreationDT = PDTFactory.getCurrentLocalDateTime ();
    m_aSC = aServletContext;
    ServletContextPathHolder.setServletContextPath (aServletContext.getContextPath ());
  }

  @Nonnull
  public final LocalDateTime getCreationDateTime ()
  {
    return m_aCreationDT;
  }

  @Override
  protected void postDestroy ()
  {
    super.postDestroy ();
    ServletContextPathHolder.clearContextPath ();
  }

  @Nonnull
  public final ServletContext getServletContext ()
  {
    return m_aSC;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (!super.equals (o))
      return false;
    final GlobalWebScope rhs = (GlobalWebScope) o;
    return m_aSC.getContextPath ().equals (rhs.getServletContext ().getContextPath ());
  }

  @Override
  public int hashCode ()
  {
    return HashCodeGenerator.getDerived (super.hashCode ()).append (m_aSC.getContextPath ()).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ()).append ("ServletContext", m_aSC).getToString ();
  }
}

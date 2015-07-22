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
package com.helger.web.scope;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.scope.GlobalScope;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.scope.mgr.MetaWebScopeFactory;

/**
 * Implementation of the {@link IGlobalWebScope} interface for web applications.<br>
 * Note: for synchronization issues, this class does not store the attributes in
 * the passed {@link ServletContext} but in a separate map.
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class GlobalWebScope extends GlobalScope implements IGlobalWebScope
{
  // Because of transient field
  private static final long serialVersionUID = 15665138713664L;
  private static final Logger s_aLogger = LoggerFactory.getLogger (GlobalWebScope.class);

  private final transient ServletContext m_aSC;
  private String m_sCustomContextPath;

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

    m_aSC = aServletContext;
  }

  @Override
  @Nonnull
  protected IApplicationWebScope createApplicationScope (@Nonnull @Nonempty final String sApplicationID)
  {
    return MetaWebScopeFactory.getWebScopeFactory ().createApplicationScope (sApplicationID);
  }

  @Override
  @Nullable
  public IApplicationWebScope getApplicationScope (@Nonnull @Nonempty final String sApplicationID,
                                                   final boolean bCreateIfNotExisting)
  {
    return (IApplicationWebScope) super.getApplicationScope (sApplicationID, bCreateIfNotExisting);
  }

  @Nonnull
  public ServletContext getServletContext ()
  {
    return m_aSC;
  }

  /**
   * Manually change the context path to be used. Normally there is no need to
   * call the method, because the context path is automatically determined from
   * the {@link ServletContext} or from the underlying request. This method is
   * only needed, if a web application is proxied by e.g. an Apache httpd and
   * the context path between httpd and Java web application server is
   * different!
   *
   * @param sContextPath
   *        The context path of the web application, or "" for the default
   *        (root) context. May not be <code>null</code>.
   */
  public void setCustomContextPath (@Nonnull final String sContextPath)
  {
    ValueEnforcer.notNull (sContextPath, "ContextPath");
    m_sCustomContextPath = sContextPath;
    s_aLogger.info ("The context path was manually overridden to use '" + sContextPath + "'!");
  }

  /**
   * @return <code>true</code> if a custom context path was set.
   * @see #setCustomContextPath(String)
   */
  public boolean hasCustomContextPath ()
  {
    return m_sCustomContextPath != null;
  }

  @Override
  @Nonnull
  public String getContextPath ()
  {
    String ret = m_sCustomContextPath;
    if (ret == null)
      ret = m_aSC.getContextPath ();
    return ret;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (!super.equals (o))
      return false;
    final GlobalWebScope rhs = (GlobalWebScope) o;
    return getContextPath ().equals (rhs.getContextPath ());
  }

  @Override
  public int hashCode ()
  {
    return HashCodeGenerator.getDerived (super.hashCode ()).append (getContextPath ()).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ())
                            .append ("ServletContext", m_aSC)
                            .appendIfNotNull ("CustomContextPath", m_sCustomContextPath)
                            .toString ();
  }
}

/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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
package com.helger.servlet;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.string.ToStringGenerator;
import com.helger.servlet.request.RequestHelper;

/**
 * This singleton instance represents default server information for locations
 * where no request context is available (e.g. in scheduled tasks)
 *
 * @author Philip Helger
 */
@Immutable
public class StaticServerInfo
{
  private static final Logger LOGGER = LoggerFactory.getLogger (StaticServerInfo.class);

  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  @GuardedBy ("RW_LOCK")
  private static StaticServerInfo s_aDefault;

  private final String m_sScheme;
  private final String m_sServerName;
  private final int m_nServerPort;
  private final String m_sContextPath;
  private final String m_sFullServerPath;
  private final String m_sFullServerAndContextPath;

  protected StaticServerInfo (@Nonnull @Nonempty final String sScheme,
                              @Nonnull @Nonempty final String sServerName,
                              @Nonnegative final int nServerPort,
                              @Nonnull final String sContextPath)
  {
    m_sScheme = ValueEnforcer.notEmpty (sScheme, "Scheme");
    m_sServerName = ValueEnforcer.notEmpty (sServerName, "ServerName");
    m_nServerPort = RequestHelper.getServerPortToUse (sScheme, nServerPort);
    // may be empty!!
    m_sContextPath = ValueEnforcer.notNull (sContextPath, "ContextPath");

    m_sFullServerPath = RequestHelper.getFullServerName (sScheme, sServerName, nServerPort).toString ();
    m_sFullServerAndContextPath = m_sFullServerPath + sContextPath;
  }

  /**
   * @return The scheme. E.g. "http" or "https".
   */
  @Nonnull
  public String getScheme ()
  {
    return m_sScheme;
  }

  /**
   * @return The server name without scheme. E.g. "www.helger.com"
   */
  @Nonnull
  public String getServerName ()
  {
    return m_sServerName;
  }

  /**
   * @return The server port we're running on. May be -1, if constructed from a
   *         URL and the passed scheme is neither "http" nor "https".
   */
  @CheckForSigned
  public int getServerPort ()
  {
    return m_nServerPort;
  }

  /**
   * @return <code>"/context"</code> or <code>""</code> (empty string)
   */
  @Nonnull
  public String getContextPath ()
  {
    return m_sContextPath;
  }

  /**
   * @return <code>scheme://server:port</code> or <code>scheme://server</code>
   *         if the default port was used
   */
  @Nonnull
  public String getFullServerPath ()
  {
    return m_sFullServerPath;
  }

  /**
   * This is a shortcut for
   * <code>getFullServerPath () + getContextPath ()</code>.
   *
   * @return <code>scheme://server:port/context</code> or
   *         <code>scheme://server:port</code> for the ROOT context.
   */
  @Nonnull
  public String getFullContextPath ()
  {
    return m_sFullServerAndContextPath;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("scheme", m_sScheme)
                                       .append ("serverName", m_sServerName)
                                       .append ("serverPort", m_nServerPort)
                                       .append ("contextPath", m_sContextPath)
                                       .append ("fullServerPath", m_sFullServerPath)
                                       .append ("fullServerAndContextPath", m_sFullServerAndContextPath)
                                       .getToString ();
  }

  public static boolean isSet ()
  {
    return RW_LOCK.readLockedBoolean ( () -> s_aDefault != null);
  }

  @Nonnull
  public static StaticServerInfo init (@Nonnull @Nonempty final String sScheme,
                                       @Nonnull @Nonempty final String sServerName,
                                       @Nonnegative final int nServerPort,
                                       @Nonnull final String sContextPath)
  {
    if (isSet ())
      throw new IllegalStateException ("Static server info already present!");

    final StaticServerInfo aDefault = new StaticServerInfo (sScheme, sServerName, nServerPort, sContextPath);
    LOGGER.info ("Static server information set: " + aDefault.toString ());
    RW_LOCK.writeLocked ( () -> s_aDefault = aDefault);
    return aDefault;
  }

  @Nonnull
  public static StaticServerInfo getInstance ()
  {
    final StaticServerInfo ret = RW_LOCK.readLockedGet ( () -> s_aDefault);
    if (ret == null)
      throw new IllegalStateException ("No default web server info present!");
    return ret;
  }
}

/**
 * Copyright (C) 2016-2020 Philip Helger (www.helger.com)
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
package com.helger.jsch.proxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.string.ToStringGenerator;
import com.helger.jsch.session.ISessionFactory;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SocketFactory;

public class SshProxy implements Proxy, AutoCloseable
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SshProxy.class);

  private final ISessionFactory m_aSessionFactory;
  private final Session m_aSession;
  private Channel m_aChannel;
  private InputStream m_aIS;
  private OutputStream m_aOS;

  public SshProxy (@Nonnull final ISessionFactory sessionFactory) throws JSchException
  {
    m_aSessionFactory = sessionFactory;
    m_aSession = sessionFactory.newSession ();
  }

  public void close ()
  {
    if (m_aSession != null && m_aSession.isConnected ())
      m_aSession.disconnect ();
  }

  public void connect (final SocketFactory socketFactory,
                       final String host,
                       final int port,
                       final int timeout) throws Exception
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("connecting session");
    m_aSession.connect ();

    m_aChannel = m_aSession.getStreamForwarder (host, port);
    m_aIS = m_aChannel.getInputStream ();
    m_aOS = m_aChannel.getOutputStream ();

    m_aChannel.connect (timeout);
  }

  public InputStream getInputStream ()
  {
    return m_aIS;
  }

  public OutputStream getOutputStream ()
  {
    return m_aOS;
  }

  public Socket getSocket ()
  {
    return null;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("SessionFactory", m_aSessionFactory.getAsString ()).getToString ();
  }
}

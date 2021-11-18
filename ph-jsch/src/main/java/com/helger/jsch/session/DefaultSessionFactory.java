/*
 * Copyright (C) 2016-2021 Philip Helger (www.helger.com)
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
package com.helger.jsch.session;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.string.StringHelper;
import com.helger.jsch.JSchInit;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * The default implementation of {@link com.helger.jsch.session.ISessionFactory
 * SessionFactory}. This class provides sane defaults for all
 * <i>conventional</i> configuration including
 * <ul>
 * <li><b>username</b>: System property <code>user.name</code></li>
 * <li><b>hostname</b>: localhost</li>
 * <li><b>port</b>: 22</li>
 * <li><b>.ssh directory:</b> System property <code>jsch.dotSsh</code>, or
 * system property <code>user.home</code> concatenated with
 * <code>"/.ssh"</code></li>
 * <li><b>known hosts:</b> System property <code>jsch.knownHosts.file</code> or,
 * .ssh directory concatenated with <code>"/known_hosts"</code>.</li>
 * <li><b>private keys:</b> First checks system property
 * <code>jsch.privateKey.files</code> split on <code>","</code>, otherwise, .ssh
 * directory concatenated with all 3 of <code>"/id_rsa"</code>,
 * <code>"/id_dsa"</code>, and <code>"/id_ecdsa"</code> if they exist.</li>
 * </ul>
 */
public class DefaultSessionFactory implements ISessionFactory
{
  public static final String PROPERTY_JSCH_DOT_SSH = "jsch.dotSsh";
  public static final String PROPERTY_JSCH_KNOWN_HOSTS_FILE = "jsch.knownHosts.file";
  public static final String PROPERTY_JSCH_PRIVATE_KEY_FILES = "jsch.privateKey.files";

  private static final Logger LOGGER = LoggerFactory.getLogger (DefaultSessionFactory.class);

  private final JSch m_aJSch;
  private ICommonsMap <String, String> m_aConfig;
  private String m_sHostname;
  private String m_sPassword;
  private int m_nPort = SSH_PORT;
  private Proxy m_aProxy;
  private UserInfo m_aUserInfo;
  private String m_sUsername;

  // Status vars
  private File m_aDotSshDir;

  /**
   * Creates a default DefaultSessionFactory.
   */
  public DefaultSessionFactory ()
  {
    this (null, null, -1);
  }

  /**
   * Constructs a DefaultSessionFactory with the supplied properties.
   *
   * @param username
   *        The username
   * @param hostname
   *        The hostname
   * @param port
   *        The port
   */
  public DefaultSessionFactory (final String username, final String hostname, final int port)
  {
    JSchInit.init ();
    m_aJSch = new JSch ();

    try
    {
      _setDefaultIdentities ();
    }
    catch (final JSchException e)
    {
      LOGGER.warn ("Unable to set default identities: ", e);
    }

    try
    {
      _setDefaultKnownHosts ();
    }
    catch (final JSchException e)
    {
      LOGGER.warn ("Unable to set default known_hosts: ", e);
    }

    if (username == null)
      m_sUsername = System.getProperty ("user.name").toLowerCase (Locale.US);
    else
      m_sUsername = username;

    if (hostname == null)
      m_sHostname = "localhost";
    else
      m_sHostname = hostname;

    if (port < 0)
      m_nPort = SSH_PORT;
    else
      m_nPort = port;
  }

  private DefaultSessionFactory (final JSch jsch, final String username, final String hostname, final int port, final Proxy proxy)
  {
    m_aJSch = jsch;
    m_sUsername = username;
    m_sHostname = hostname;
    m_nPort = port;
    m_aProxy = proxy;
  }

  private void _clearIdentityRepository () throws JSchException
  {
    // revert to default identity repo
    m_aJSch.setIdentityRepository (null);
    m_aJSch.removeAllIdentity ();
  }

  @Nonnull
  private File _dotSshDir ()
  {
    if (m_aDotSshDir == null)
    {
      final String dotSshString = System.getProperty (PROPERTY_JSCH_DOT_SSH);
      if (dotSshString != null)
        m_aDotSshDir = new File (dotSshString);
      else
        m_aDotSshDir = new File (new File (System.getProperty ("user.home")), ".ssh");
    }
    return m_aDotSshDir;
  }

  /**
   * Sets the configuration options for the sessions created by this factory.
   * This method will replace the current SessionFactory <code>config</code>
   * map. If you want to add, rather than replace, see
   * {@link #setConfig(String, String)}. All of these options will be added one
   * at a time using {@link com.jcraft.jsch.Session#setConfig(String, String)
   * Session.setConfig(String, String)}. Details on the supported options can be
   * found in the source for <code>com.jcraft.jsch.Session#applyConfig()</code>
   *
   * @param config
   *        The configuration options
   * @see com.jcraft.jsch.Session#setConfig(java.util.Hashtable)
   */
  public void setConfig (final ICommonsMap <String, String> config)
  {
    m_aConfig = config;
  }

  /**
   * Adds a single configuration options for the sessions created by this
   * factory. Details on the supported options can be found in the source for
   * <code>com.jcraft.jsch.Session#applyConfig()</code>
   *
   * @param key
   *        The name of the option
   * @param value
   *        The value of the option
   * @see #setConfig(ICommonsMap)
   * @see com.jcraft.jsch.Session#setConfig(java.util.Hashtable)
   */
  public void setConfig (@Nonnull final String key, @Nonnull final String value)
  {
    if (m_aConfig == null)
      m_aConfig = new CommonsHashMap <> ();
    m_aConfig.put (key, value);
  }

  @Override
  public String getHostname ()
  {
    return m_sHostname;
  }

  /**
   * Sets the hostname.
   *
   * @param hostname
   *        The hostname.
   */
  public void setHostname (final String hostname)
  {
    m_sHostname = hostname;
  }

  /**
   * Sets the {@code password} used to authenticate {@code username}. This mode
   * of authentication is not recommended as it would keep the password in
   * memory and if the application dies and writes a heap dump, it would be
   * available. Using {@link Identity} would be better, or even using ssh agent
   * support.
   *
   * @param password
   *        the password for {@code username}
   */
  public void setPassword (final String password)
  {
    m_sPassword = password;
  }

  @Override
  public int getPort ()
  {
    return m_nPort;
  }

  /**
   * Sets the port.
   *
   * @param port
   *        The port
   */
  public void setPort (final int port)
  {
    m_nPort = port;
  }

  @Override
  public Proxy getProxy ()
  {
    return m_aProxy;
  }

  /**
   * Sets the proxy through which all connections will be piped.
   *
   * @param proxy
   *        The proxy
   */
  public void setProxy (final Proxy proxy)
  {
    m_aProxy = proxy;
  }

  @Override
  public String getUsername ()
  {
    return m_sUsername;
  }

  /**
   * Sets the username.
   *
   * @param username
   *        The username
   */
  public void setUsername (final String username)
  {
    m_sUsername = username;
  }

  @Override
  public UserInfo getUserInfo ()
  {
    return m_aUserInfo;
  }

  /**
   * Sets the {@code UserInfo} for use with {@code keyboard-interactive}
   * authentication. This may be useful, however, setting the password with
   * {@link #setPassword(String)} is likely sufficient.
   *
   * @param userInfo
   *        User info
   * @see <a href=
   *      "http://www.jcraft.com/jsch/examples/UserAuthKI.java.html">Keyboard
   *      Interactive Authentication Example</a>
   */
  public void setUserInfo (final UserInfo userInfo)
  {
    m_aUserInfo = userInfo;
  }

  private void _setDefaultKnownHosts () throws JSchException
  {
    final String knownHosts = System.getProperty (PROPERTY_JSCH_KNOWN_HOSTS_FILE);
    if (knownHosts != null && !knownHosts.isEmpty ())
    {
      setKnownHosts (knownHosts);
    }
    else
    {
      final File knownHostsFile = new File (_dotSshDir (), "known_hosts");
      if (knownHostsFile.exists ())
      {
        setKnownHosts (knownHostsFile.getAbsolutePath ());
      }
    }
  }

  private void _setDefaultIdentities () throws JSchException
  {
    boolean identitiesSet = false;
    if (!identitiesSet)
    {
      final String privateKeyFilesString = System.getProperty (PROPERTY_JSCH_PRIVATE_KEY_FILES);
      if (StringHelper.hasText (privateKeyFilesString))
      {
        LOGGER.info ("Using local identities from " + PROPERTY_JSCH_PRIVATE_KEY_FILES + ": " + privateKeyFilesString);
        setIdentitiesFromPrivateKeys (StringHelper.getExploded (',', privateKeyFilesString));
        identitiesSet = true;
      }
    }
    if (!identitiesSet)
    {
      final List <String> privateKeyFiles = new ArrayList <> ();
      for (final File file : new File [] { new File (_dotSshDir (), "id_rsa"),
                                           new File (_dotSshDir (), "id_dsa"),
                                           new File (_dotSshDir (), "id_ecdsa") })
        if (file.exists ())
          privateKeyFiles.add (file.getAbsolutePath ());
      LOGGER.info ("Using local identities: " + privateKeyFiles);
      setIdentitiesFromPrivateKeys (privateKeyFiles);
    }
  }

  /**
   * Configures this factory to use a single identity authenticated by the
   * supplied private key. The private key should be the path to a private key
   * file in OpenSSH format. Clears out the current {@link IdentityRepository}
   * before adding this key.
   *
   * @param privateKey
   *        Path to a private key file
   * @throws JSchException
   *         If the key is invalid
   */
  public void setIdentityFromPrivateKey (final String privateKey) throws JSchException
  {
    _clearIdentityRepository ();
    m_aJSch.addIdentity (privateKey);
  }

  /**
   * Configures this factory to use a single identity authenticated by the
   * supplied private key and pass phrase. The private key should be the path to
   * a private key file in OpenSSH format. Clears out the current
   * {@link IdentityRepository} before adding this key.
   *
   * @param privateKey
   *        Path to a private key file
   * @param passPhrase
   *        Pass phrase for private key
   * @throws JSchException
   *         If the key is invalid
   */
  public void setIdentityFromPrivateKey (final String privateKey, final String passPhrase) throws JSchException
  {
    _clearIdentityRepository ();
    m_aJSch.addIdentity (privateKey, passPhrase);
  }

  /**
   * Configures this factory to use a list of identities authenticated by the
   * supplied private keys. The private keys should be the paths to a private
   * key files in OpenSSH format. Clears out the current
   * {@link IdentityRepository} before adding these keys.
   *
   * @param privateKeys
   *        A list of paths to private key files
   * @throws JSchException
   *         If one (or more) of the keys are invalid
   */
  public void setIdentitiesFromPrivateKeys (final List <String> privateKeys) throws JSchException
  {
    _clearIdentityRepository ();
    for (final String privateKey : privateKeys)
    {
      m_aJSch.addIdentity (privateKey);
    }
  }

  /**
   * Sets the {@link IdentityRepository} for this factory. This will replace any
   * current IdentityRepository, so you should be sure to call this before any
   * of the <code>setIdentit(y|ies)Xxx</code> if you plan on using both.
   *
   * @param identityRepository
   *        The identity repository
   * @see JSch#setIdentityRepository(IdentityRepository)
   */
  public void setIdentityRepository (final IdentityRepository identityRepository)
  {
    m_aJSch.setIdentityRepository (identityRepository);
  }

  /**
   * Sets the known hosts from the stream. Mostly useful if you distribute your
   * known_hosts in the jar for your application rather than allowing users to
   * manage their own known hosts.
   *
   * @param knownHosts
   *        A stream of known hosts
   * @throws JSchException
   *         If an I/O error occurs
   * @see JSch#setKnownHosts(InputStream)
   */
  public void setKnownHosts (final InputStream knownHosts) throws JSchException
  {
    m_aJSch.setKnownHosts (knownHosts);
  }

  /**
   * Sets the known hosts from a file at path <code>knownHosts</code>.
   *
   * @param knownHosts
   *        The path to a known hosts file
   * @throws JSchException
   *         If an I/O error occurs
   * @see JSch#setKnownHosts(String)
   */
  public void setKnownHosts (final String knownHosts) throws JSchException
  {
    m_aJSch.setKnownHosts (knownHosts);
  }

  @Override
  public Session newSession () throws JSchException
  {
    final Session session = m_aJSch.getSession (m_sUsername, m_sHostname, m_nPort);
    if (m_aConfig != null)
      for (final Map.Entry <String, String> aEntry : m_aConfig.entrySet ())
        session.setConfig (aEntry.getKey (), aEntry.getValue ());

    if (m_aProxy != null)
      session.setProxy (m_aProxy);

    if (m_sPassword != null)
      session.setPassword (m_sPassword);

    if (m_aUserInfo != null)
      session.setUserInfo (m_aUserInfo);

    return session;
  }

  @Override
  public AbstractSessionFactoryBuilder newSessionFactoryBuilder ()
  {
    return new AbstractSessionFactoryBuilder (m_aJSch, m_sUsername, m_sHostname, m_nPort, m_aProxy, m_aConfig, m_aUserInfo)
    {
      @Override
      public ISessionFactory build ()
      {
        final DefaultSessionFactory ret = new DefaultSessionFactory (m_aJsch, m_sUsername, m_sHostname, m_nPort, m_aProxy);
        ret.m_aConfig = m_aConfig;
        ret.m_sPassword = m_sPassword;
        ret.m_aUserInfo = m_aUserInfo;
        return ret;
      }
    };
  }
}

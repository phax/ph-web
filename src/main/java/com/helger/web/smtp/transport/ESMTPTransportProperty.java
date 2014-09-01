/**
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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
package com.helger.web.smtp.transport;

import javax.annotation.Nonnull;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.helger.commons.annotations.Nonempty;

/**
 * Available SMTP settings for javax-mail 1.5.0.<br>
 * <a href=
 * "https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html"
 * >Source</a>
 * 
 * @author Philip Helger
 */
public enum ESMTPTransportProperty
{
  /** Default user name for SMTP. */
  USER ("user", String.class),

  /** The SMTP server to connect to. */
  HOST ("host", String.class),

  /**
   * The SMTP server port to connect to, if the connect() method doesn't
   * explicitly specify one. Defaults to 25.
   */
  PORT ("port", int.class),

  /**
   * Socket connection timeout value in milliseconds. Default is infinite
   * timeout.
   */
  CONNECTIONTIMEOUT ("connectiontimeout", int.class),

  /** Socket I/O timeout value in milliseconds. Default is infinite timeout. */
  TIMEOUT ("timeout", int.class),

  /**
   * Email address to use for SMTP MAIL command. This sets the envelope return
   * address. Defaults to msg.getFrom() or InternetAddress.getLocalAddress().
   * NOTE: mail.smtp.user was previously used for this.
   */
  FROM ("from", String.class),

  /**
   * Local host name used in the SMTP HELO or EHLO command. Defaults to
   * InetAddress.getLocalHost().getHostName(). Should not normally need to be
   * set if your JDK and your name service are configured properly.
   */
  LOCALHOST ("localhost", String.class),

  /**
   * Local address (host name) to bind to when creating the SMTP socket.
   * Defaults to the address picked by the Socket class. Should not normally
   * need to be set, but useful with multi-homed hosts where it's important to
   * pick a particular local address to bind to.
   */
  LOCALADDRESS ("localaddress", String.class),

  /**
   * Local port number to bind to when creating the SMTP socket. Defaults to the
   * port number picked by the Socket class.
   */
  LOCALPORT ("localport", int.class),

  /**
   * If false, do not attempt to sign on with the EHLO command. Defaults to
   * true. Normally failure of the EHLO command will fallback to the HELO
   * command; this property exists only for servers that don't fail EHLO
   * properly or don't implement EHLO properly.
   */
  EHLO ("ehlo", boolean.class),

  /**
   * If true, attempt to authenticate the user using the AUTH command. Defaults
   * to false.
   */
  AUTH ("auth", boolean.class),

  /**
   * If set, lists the authentication mechanisms to consider, and the order in
   * which to consider them. Only mechanisms supported by the server and
   * supported by the current implementation will be used. The default is
   * "LOGIN PLAIN DIGEST-MD5 NTLM", which includes all the authentication
   * mechanisms supported by the current implementation.
   */
  AUTH_MECHANISMS ("auth.mechanisms", String.class),

  /** If true, prevents use of the AUTH LOGIN command. Default is false. */
  AUTH_LOGIN_DISABLE ("auth.login.disable", boolean.class),

  /** If true, prevents use of the AUTH PLAIN command. Default is false. */
  AUTH_PLAIN_DISABLE ("auth.plain.disable", boolean.class),

  /** If true, prevents use of the AUTH DIGEST-MD5 command. Default is false. */
  AUTH_DIGEST_MD5_DISABLE ("auth.digest-md5.disable", boolean.class),

  /** If true, prevents use of the AUTH NTLM command. Default is false. */
  AUTH_NTLM_DISABLE ("auth.ntlm.disable", boolean.class),

  /** The NTLM authentication domain. */
  AUTH_NTLM_DOMAIN ("auth.ntlm.domain", String.class),

  /**
   * NTLM protocol-specific flags. See
   * http://curl.haxx.se/rfc/ntlm.html#theNtlmFlags for details.
   */
  AUTH_NTLM_FLAGS ("auth.ntlm.flags", int.class),

  /**
   * The submitter to use in the AUTH tag in the MAIL FROM command. Typically
   * used by a mail relay to pass along information about the original submitter
   * of the message. See also the setSubmitter method of SMTPMessage. Mail
   * clients typically do not use this.
   */
  SUBMITTER ("submitter", String.class),

  /**
   * The NOTIFY option to the RCPT command. Either NEVER, or some combination of
   * SUCCESS, FAILURE, and DELAY (separated by commas).
   */
  DSN_NOTIFY ("dsn.notify", String.class),

  /** The RET option to the MAIL command. Either FULL or HDRS. */
  DSN_RET ("dsn.ret", String.class),

  /**
   * If set to true, and the server supports the 8BITMIME extension, text parts
   * of messages that use the"quoted-printable" or"base64" encodings are
   * converted to use"8bit" encoding if they follow the RFC2045 rules for 8bit
   * text.
   */
  ALLOW8BITMIME ("allow8bitmime", boolean.class),

  /**
   * If set to true, and a message has some valid and some invalid addresses,
   * send the message anyway, reporting the partial failure with a
   * SendFailedException. If set to false (the default), the message is not sent
   * to any of the recipients if there is an invalid recipient address.
   */
  SENDPARTIAL ("sendpartial", boolean.class),

  /**
   * If set to true, attempt to use the javax.security.sasl package to choose an
   * authentication mechanism for login. Defaults to false.
   */
  SASL_ENABLE ("sasl.enable", boolean.class),

  /** A space or comma separated list of SASL mechanism names to try to use. */
  SASL_MECHANISMS ("sasl.mechanisms", String.class),

  /**
   * The authorization ID to use in the SASL authentication. If not set, the
   * authentication ID (user name) is used.
   */
  SASL_AUTHORIZATIONID ("sasl.authorizationid", String.class),

  /** The realm to use with DIGEST-MD5 authentication. */
  SASL_REALM ("sasl.realm", String.class),

  /**
   * If set to false, the QUIT command is sent and the connection is immediately
   * closed. If set to true (the default), causes the transport to wait for the
   * response to the QUIT command.
   */
  QUITWAIT ("quitwait", boolean.class),

  /**
   * If set to true, causes the transport to include an
   * SMTPAddressSucceededException for each address that is successful. Note
   * also that this will cause a SendFailedException to be thrown from the
   * sendMessage method of SMTPTransport even if all addresses were correct and
   * the message was sent successfully.
   */
  REPORTSUCCESS ("reportsuccess", boolean.class),

  /**
   * If set to a class that implements the javax.net.SocketFactory interface,
   * this class will be used to create SMTP sockets. Note that this is an
   * instance of a class, not a name, and must be set using the put method, not
   * the setProperty method.
   */
  SOCKETFACTORY ("socketFactory", SocketFactory.class),

  /**
   * If set, specifies the name of a class that implements the
   * javax.net.SocketFactory interface. This class will be used to create SMTP
   * sockets.
   */
  SOCKETFACTORY_CLASS ("socketFactory.class", String.class),

  /**
   * If set to true, failure to create a socket using the specified socket
   * factory class will cause the socket to be created using the java.net.Socket
   * class. Defaults to true.
   */
  SOCKETFACTORY_FALLBACK ("socketFactory.fallback", boolean.class),

  /**
   * Specifies the port to connect to when using the specified socket factory.
   * If not set, the default port will be used.
   */
  SOCKETFACTORY_PORT ("socketFactory.port", int.class),

  /**
   * If set to true, use SSL to connect and use the SSL port by default.
   * Defaults to false for the"smtp" protocol and true for the"smtps" protocol.
   */
  SSL_ENABLE ("ssl.enable", boolean.class),

  /**
   * If set to true, check the server identity as specified by RFC 2595. These
   * additional checks based on the content of the server's certificate are
   * intended to prevent man-in-the-middle attacks. Defaults to false.
   */
  SSL_CHECKSERVERIDENTITY ("ssl.checkserveridentity", boolean.class),

  /**
   * If set, and a socket factory hasn't been specified, enables use of a
   * MailSSLSocketFactory. If set to"*", all hosts are trusted. If set to a
   * whitespace separated list of hosts, those hosts are trusted. Otherwise,
   * trust depends on the certificate the server presents.
   */
  SSL_TRUST ("ssl.trust", String.class),

  /**
   * If set to a class that extends the javax.net.ssl.SSLSocketFactory class,
   * this class will be used to create SMTP SSL sockets. Note that this is an
   * instance of a class, not a name, and must be set using the put method, not
   * the setProperty method.
   */
  SSL_SOCKETFACTORY ("ssl.socketFactory", SSLSocketFactory.class),

  /**
   * If set, specifies the name of a class that extends the
   * javax.net.ssl.SSLSocketFactory class. This class will be used to create
   * SMTP SSL sockets.
   */
  SSL_SOCKETFACTORY_CLASS ("ssl.socketFactory.class", String.class),

  /**
   * Specifies the port to connect to when using the specified socket factory.
   * If not set, the default port will be used.
   */
  SSL_SOCKETFACTORY_PORT ("ssl.socketFactory.port", int.class),

  /**
   * Specifies the SSL protocols that will be enabled for SSL connections. The
   * property value is a whitespace separated list of tokens acceptable to the
   * javax.net.ssl.SSLSocket.setEnabledProtocols method.
   */
  SSL_PROTOCOLS ("ssl.protocols", String.class),

  /**
   * Specifies the SSL cipher suites that will be enabled for SSL connections.
   * The property value is a whitespace separated list of tokens acceptable to
   * the javax.net.ssl.SSLSocket.setEnabledCipherSuites method.
   */
  SSL_CIPHERSUITES ("ssl.ciphersuites", String.class),

  /**
   * If true, enables the use of the STARTTLS command (if supported by the
   * server) to switch the connection to a TLS-protected connection before
   * issuing any login commands. Note that an appropriate trust store must
   * configured so that the client will trust the server's certificate. Defaults
   * to false.
   */
  STARTTLS_ENABLE ("starttls.enable", boolean.class),

  /**
   * If true, requires the use of the STARTTLS command. If the server doesn't
   * support the STARTTLS command, or the command fails, the connect method will
   * fail. Defaults to false.
   */
  STARTTLS_REQUIRED ("starttls.required", boolean.class),

  /**
   * Specifies the host name of a SOCKS5 proxy server that will be used for
   * connections to the mail server. (Note that this only works on JDK 1.5 or
   * newer.)
   */
  SOCKS_HOST ("socks.host", String.class),

  /**
   * Specifies the port number for the SOCKS5 proxy server. This should only
   * need to be used if the proxy server is not using the standard port number
   * of 1080.
   */
  SOCKS_PORT ("socks.port", String.class),

  /**
   * Extension string to append to the MAIL command. The extension string can be
   * used to specify standard SMTP service extensions as well as vendor-specific
   * extensions. Typically the application should use the SMTPTransport method
   * supportsExtension to verify that the server supports the desired service
   * extension. See RFC 1869 and other RFCs that define specific extensions.
   */
  MAILEXTENSION ("mailextension", String.class),

  /**
   * If set to true, use the RSET command instead of the NOOP command in the
   * isConnected method. In some cases sendmail will respond slowly after many
   * NOOP commands; use of RSET avoids this sendmail issue. Defaults to false.
   */
  USERSET ("userset", boolean.class),

  /**
   * If set to true (the default), insist on a 250 response code from the NOOP
   * command to indicate success. The NOOP command is used by the isConnected
   * method to determine if the connection is still alive. Some older servers
   * return the wrong response code on success, some servers don't implement the
   * NOOP command at all and so always return a failure code. Set this property
   * to false to handle servers that are broken in this way. Normally, when a
   * server times out a connection, it will send a 421 response code, which the
   * client will see as the response to the next command it issues. Some servers
   * send the wrong failure response code when timing out a connection. Do not
   * set this property to false when dealing with servers that are broken in
   * this way.
   */
  NOOP_STRICT ("noop.strict", boolean.class);

  private final String m_sPropertyName;
  private final Class <?> m_aPropertyValueClass;

  private ESMTPTransportProperty (@Nonnull @Nonempty final String sPropertyName,
                                  @Nonnull final Class <?> aPropertyValueClass)
  {
    m_sPropertyName = sPropertyName;
    m_aPropertyValueClass = aPropertyValueClass;
  }

  /**
   * @return The property name for regular SMTP transfer. Neither
   *         <code>null</code> nor empty.
   */
  @Nonnull
  @Nonempty
  public String getSMTPPropertyName ()
  {
    return "mail.smtp." + m_sPropertyName;
  }

  /**
   * @return The property name for secure SMTP transfer. Neither
   *         <code>null</code> nor empty.
   */
  @Nonnull
  @Nonempty
  public String getSMTPSPropertyName ()
  {
    return "mail.smtps." + m_sPropertyName;
  }

  /**
   * @return The property name for regular SMTP transfer. Neither
   *         <code>null</code> nor empty.
   */
  @Nonnull
  @Nonempty
  public String getPropertyName (final boolean bForSMTPS)
  {
    return bForSMTPS ? getSMTPSPropertyName () : getSMTPPropertyName ();
  }

  /**
   * @return The expected value class of this property. Never <code>null</code>.
   */
  @Nonnull
  public Class <?> getPropertyValueClass ()
  {
    return m_aPropertyValueClass;
  }
}

package com.helger.network.proxy.settings;

import java.net.InetSocketAddress;
import java.net.Proxy;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;

/**
 * Immutable default implementation of {@link IProxySettings}
 *
 * @author Philip Helger
 */
@Immutable
public final class ProxySettings implements IProxySettings
{
  private final Proxy.Type m_eProxyType;
  private final String m_sProxyHost;
  private final int m_nProxyPort;
  private final String m_sProxyUserName;
  private final String m_sProxyPassword;

  public ProxySettings (@Nonnull final Proxy.Type eProxyType,
                        @Nullable final String sProxyHost,
                        @Nonnegative final int nProxyPort,
                        @Nullable final String sProxyUserName,
                        @Nullable final String sProxyPassword)
  {
    ValueEnforcer.notNull (eProxyType, "ProxyType");
    m_eProxyType = eProxyType;
    m_sProxyHost = sProxyHost;
    m_nProxyPort = nProxyPort;
    m_sProxyUserName = sProxyUserName;
    m_sProxyPassword = sProxyPassword;
  }

  @Nonnull
  public Proxy.Type getProxyType ()
  {
    return m_eProxyType;
  }

  @Nullable
  public String getProxyHost ()
  {
    return m_sProxyHost;
  }

  @Nonnegative
  public int getProxyPort ()
  {
    return m_nProxyPort;
  }

  @Nullable
  public String getProxyUserName ()
  {
    return m_sProxyUserName;
  }

  @Nullable
  public String getProxyPassword ()
  {
    return m_sProxyPassword;
  }

  @Nonnull
  public Proxy getAsProxy (final boolean bResolveHostname)
  {
    switch (m_eProxyType)
    {
      case DIRECT:
        return Proxy.NO_PROXY;
      case HTTP:
      case SOCKS:
        return new Proxy (m_eProxyType,
                          bResolveHostname ? new InetSocketAddress (m_sProxyHost, m_nProxyPort)
                                           : InetSocketAddress.createUnresolved (m_sProxyHost, m_nProxyPort));
      default:
        throw new IllegalStateException ("Unsupported proxy type: " + m_eProxyType);
    }
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final ProxySettings rhs = (ProxySettings) o;
    return m_eProxyType.equals (rhs.m_eProxyType) &&
           EqualsHelper.equals (m_sProxyHost, rhs.m_sProxyHost) &&
           m_nProxyPort == rhs.m_nProxyPort &&
           EqualsHelper.equals (m_sProxyUserName, rhs.m_sProxyUserName) &&
           EqualsHelper.equals (m_sProxyPassword, rhs.m_sProxyPassword);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_eProxyType)
                                       .append (m_sProxyHost)
                                       .append (m_nProxyPort)
                                       .append (m_sProxyUserName)
                                       .append (m_sProxyPassword)
                                       .getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ProxyType", m_eProxyType)
                                       .append ("ProxyHost", m_sProxyHost)
                                       .append ("ProxyPort", m_nProxyPort)
                                       .append ("ProxyUserName", m_sProxyUserName)
                                       .appendPassword ("ProxyPassword")
                                       .getToString ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ProxySettings createNoProxySettings ()
  {
    return new ProxySettings (Proxy.Type.DIRECT, null, -1, null, null);
  }
}

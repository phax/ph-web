package com.helger.network.proxy.settings;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.URI;
import java.util.function.Supplier;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.CommonsLinkedHashSet;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.state.EChange;
import com.helger.commons.state.EHandled;

public final class ProxySettingsManager
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ProxySettingsManager.class);
  private static final SimpleReadWriteLock s_aRWLock = new SimpleReadWriteLock ();
  @GuardedBy ("s_aRWLock")
  private static final ICommonsList <IProxySettingsProvider> s_aList = new CommonsArrayList <> ();

  private ProxySettingsManager ()
  {}

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <IProxySettingsProvider> getAllProviders ()
  {
    return s_aRWLock.readLocked (s_aList::getClone);
  }

  public static void registerProvider (@Nonnull final IProxySettingsProvider aProvider)
  {
    ValueEnforcer.notNull (aProvider, "Provider");
    s_aRWLock.writeLocked ( () -> s_aList.add (aProvider));

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Registered proxy settings provider " + aProvider);
  }

  @Nonnull
  public static EChange unregisterProvider (@Nullable final IProxySettingsProvider aProvider)
  {
    if (aProvider == null)
      return EChange.UNCHANGED;

    final EChange eChange = s_aRWLock.writeLocked ( () -> s_aList.removeObject (aProvider));
    if (eChange.isChanged ())
      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Unregistered proxy settings provider " + aProvider);
    return eChange;
  }

  @Nonnull
  public static EChange removeAllProviders ()
  {
    final EChange eChange = s_aRWLock.writeLocked ((Supplier <EChange>) s_aList::removeAll);
    if (eChange.isChanged ())
      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Removed all proxy settings provider");
    return eChange;
  }

  /**
   * Find all proxy settings matching the provided parameters.
   *
   * @param aURI
   *        Destination URI
   * @return A non-<code>null</code> set with all matching proxy settings. A set
   *         is used to avoid that the same settings are used more than once.
   * @see #findAllProxySettings(String, String, int)
   */
  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsOrderedSet <IProxySettings> findAllProxySettings (@Nonnull final URI aURI)
  {
    return findAllProxySettings (aURI.getScheme (), aURI.getHost (), aURI.getPort ());
  }

  /**
   * Find all proxy settings matching the provided parameters.
   *
   * @param sProtocol
   *        Destination server protocol.
   * @param sHostName
   *        Destination host name
   * @param nPort
   *        Destination port
   * @return A non-<code>null</code> set with all matching proxy settings. A set
   *         is used to avoid that the same settings are used more than once.
   */
  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsOrderedSet <IProxySettings> findAllProxySettings (@Nullable final String sProtocol,
                                                                          @Nullable final String sHostName,
                                                                          @CheckForSigned final int nPort)
  {
    final ICommonsOrderedSet <IProxySettings> ret = new CommonsLinkedHashSet <> ();
    for (final IProxySettingsProvider aProvider : getAllProviders ())
      ret.addAll (aProvider.getAllProxySettings (sProtocol, sHostName, nPort));
    return ret;
  }

  @Nonnull
  public static EHandled onConnectionFailed (@Nonnull final URI aURI,
                                             @Nonnull final SocketAddress aAddr,
                                             @Nonnull final IOException ex)
  {
    final String sProtocol = aURI.getScheme ();
    final String sHostName = aURI.getHost ();
    final int nPort = aURI.getPort ();

    int nInvokedProviders = 0;

    // For all providers
    for (final IProxySettingsProvider aProvider : getAllProviders ())
    {
      final ICommonsList <IProxySettings> aMatches = aProvider.getAllProxySettings (sProtocol, sHostName, nPort);
      // For all matching proxies
      for (final IProxySettings aProxySettings : aMatches)
        if (aProxySettings.hasSocketAddress (aAddr))
        {
          // Found a matching proxy
          aProvider.onConnectionFailed (aProxySettings, aURI, aAddr, ex);
          nInvokedProviders++;
        }
    }

    return EHandled.valueOf (nInvokedProviders > 0);
  }
}

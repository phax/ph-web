package com.helger.network.proxy.settings;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.state.EChange;

public final class ProxySettingsManager
{
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
  }

  @Nonnull
  public static EChange unregisterProvider (@Nonnull final IProxySettingsProvider aProvider)
  {
    ValueEnforcer.notNull (aProvider, "Provider");
    return s_aRWLock.writeLocked ( () -> s_aList.removeObject (aProvider));
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <IProxySettings> findAllProxySettings (@Nullable final String sProtocol,
                                                                    @Nullable final String sHostName,
                                                                    @CheckForSigned final int nPort)
  {
    final ICommonsList <IProxySettings> ret = new CommonsArrayList <> ();
    for (final IProxySettingsProvider aProvider : getAllProviders ())
      ret.addAll (aProvider.getAllProxySettings (sProtocol, sHostName, nPort));
    return ret;
  }
}

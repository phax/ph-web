package com.helger.web.port;

import java.util.function.Supplier;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ext.CommonsHashMap;
import com.helger.commons.collection.ext.ICommonsMap;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.string.StringHelper;

public final class SchemeDefaultPortMapper
{
  /** The scheme for HTTP */
  public static final String SCHEME_FTP = "ftp";
  /** The scheme for HTTP */
  public static final String SCHEME_HTTP = "http";
  /** The scheme for HTTPS */
  public static final String SCHEME_HTTPS = "https";

  @PresentForCodeCoverage
  private static final SchemeDefaultPortMapper s_aInstance = new SchemeDefaultPortMapper ();

  private static final SimpleReadWriteLock s_aRWLock = new SimpleReadWriteLock ();
  @GuardedBy ("s_aRWLock")
  private static final ICommonsMap <String, Integer> s_aMap = new CommonsHashMap <> ();

  static
  {
    registerDefaultPort (SCHEME_FTP, 21);
    registerDefaultPort (SCHEME_HTTP, 80);
    registerDefaultPort (SCHEME_HTTPS, 443);
  }

  private SchemeDefaultPortMapper ()
  {}

  public static void registerDefaultPort (@Nonnull @Nonempty final String sSchemeName, @Nonnegative final int nPort)
  {
    ValueEnforcer.notEmpty (sSchemeName, "SchemeName");
    ValueEnforcer.isTrue (DefaultNetworkPorts.isValidPort (nPort), "Invalid port provided");

    s_aRWLock.writeLocked ( () -> {
      if (s_aMap.containsKey (sSchemeName))
        throw new IllegalArgumentException ("A default port for scheme '" + sSchemeName + "' is already registered!");
      s_aMap.put (sSchemeName, Integer.valueOf (nPort));
    });
  }

  public static int getDefaultPort (@Nullable final String sSchemeName, final int nDefault)
  {
    if (StringHelper.hasText (sSchemeName))
    {
      final Integer aDefaultPort = s_aRWLock.readLocked ((Supplier <Integer>) () -> s_aMap.get (sSchemeName));
      if (aDefaultPort != null)
        return aDefaultPort.intValue ();
    }
    return nDefault;
  }

  public static int getDefaultPortOrThrow (@Nullable final String sSchemeName)
  {
    final int nPort = getDefaultPort (sSchemeName, CNetworkPort.INVALID_PORT_NUMBER);
    if (nPort == CNetworkPort.INVALID_PORT_NUMBER)
      throw new IllegalArgumentException ("No default port present for scheme '" + sSchemeName + "'");
    return nPort;
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsMap <String, Integer> getAll ()
  {
    return s_aRWLock.readLocked ( () -> s_aMap.getClone ());
  }
}

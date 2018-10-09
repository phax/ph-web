package com.helger.http.tls;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.ICommonsList;

/**
 * TLS Configuration mode read-only interface.
 *
 * @author Philip Helger
 * @since 9.0.5
 */
public interface ITLSConfigurationMode extends Serializable
{
  /**
   * @return A list of supported TLS versions in the correct order. May not be
   *         <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableCopy
  ICommonsList <ETLSVersion> getAllTLSVersions ();

  /**
   * @return A list of the IDs of the supported TLS versions in the correct order.
   *         May not be <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableCopy
  default ICommonsList <String> getAllTLSVersionIDs ()
  {
    return getAllTLSVersions ().getAllMapped (ETLSVersion::getID);
  }

  /**
   * @return A list of the IDs of the supported TLS versions in the correct order.
   *         May be <code>null</code> if no TLS versions are defined.
   */
  @Nullable
  default String [] getAllTLSVersionIDsAsArray ()
  {
    final ICommonsList <String> aList = getAllTLSVersionIDs ();
    return aList.isEmpty () ? null : aList.toArray (new String [aList.size ()]);
  }

  /**
   * @return All cipher suites in the correct order. May not be <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableCopy
  ICommonsList <String> getAllCipherSuites ();

  /**
   * @return All cipher suites in the correct order. May be <code>null</code> if
   *         no cipher suite is defined.
   */
  @Nullable
  default String [] getAllCipherSuitesAsArray ()
  {
    final ICommonsList <String> aList = getAllCipherSuites ();
    return aList.isEmpty () ? null : aList.toArray (new String [aList.size ()]);
  }
}

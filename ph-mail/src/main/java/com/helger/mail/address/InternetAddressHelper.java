/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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
package com.helger.mail.address;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.email.EmailAddress;
import com.helger.commons.email.IEmailAddress;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

/**
 * This class handles a single email address. It is split into an address part
 * and an optional name. The personal name is optional and may be
 * <code>null</code>.
 *
 * @author Philip Helger
 */
@Immutable
public final class InternetAddressHelper
{
  @PresentForCodeCoverage
  private static final InternetAddressHelper INSTANCE = new InternetAddressHelper ();

  private InternetAddressHelper ()
  {}

  @Nonnull
  public static InternetAddress getAsInternetAddress (@Nonnull final IEmailAddress aAddress,
                                                      @Nullable final Charset aCharset) throws AddressException
  {
    return getAsInternetAddress (aAddress.getAddress (), aAddress.getPersonal (), aCharset);
  }

  @Nonnull
  public static InternetAddress getAsInternetAddress (@Nonnull final String sAddress,
                                                      @Nullable final String sPersonal,
                                                      @Nullable final Charset aCharset) throws AddressException
  {
    try
    {
      return getAsInternetAddress (sAddress, sPersonal, aCharset == null ? null : aCharset.name ());
    }
    catch (final UnsupportedEncodingException ex)
    {
      throw new IllegalStateException ("Charset " + aCharset + " is unknown!", ex);
    }
  }

  @Nonnull
  public static InternetAddress getAsInternetAddress (@Nonnull final IEmailAddress aAddress,
                                                      @Nullable final String sCharset) throws UnsupportedEncodingException,
                                                                                       AddressException
  {
    return getAsInternetAddress (aAddress.getAddress (), aAddress.getPersonal (), sCharset);
  }

  @Nonnull
  public static InternetAddress getAsInternetAddress (@Nonnull final String sAddress,
                                                      @Nullable final String sPersonal,
                                                      @Nullable final String sCharset) throws UnsupportedEncodingException,
                                                                                       AddressException
  {
    final InternetAddress ret = new InternetAddress (sAddress, sPersonal, sCharset);
    ret.validate ();
    return ret;
  }

  @Nonnull
  public static InternetAddress [] getAsInternetAddressArray (@Nonnull final ICommonsList <? extends IEmailAddress> aAddresses,
                                                              @Nullable final Charset aCharset) throws AddressException
  {
    ValueEnforcer.notNull (aAddresses, "Addresses");
    final InternetAddress [] ret = new InternetAddress [aAddresses.size ()];
    int i = 0;
    for (final IEmailAddress aMA : aAddresses)
      ret[i++] = InternetAddressHelper.getAsInternetAddress (aMA, aCharset);
    return ret;
  }

  /**
   * Convert the passed {@link InternetAddress} to an {@link EmailAddress}
   *
   * @param aInternetAddress
   *        Source object. May be <code>null</code>.
   * @return <code>null</code> if the source object is <code>null</code>.
   */
  @Nullable
  public static EmailAddress getAsEmailAddress (@Nullable final InternetAddress aInternetAddress)
  {
    return aInternetAddress == null ? null : new EmailAddress (aInternetAddress.getAddress (),
                                                               aInternetAddress.getPersonal ());
  }
}

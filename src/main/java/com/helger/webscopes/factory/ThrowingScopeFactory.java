/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.webscopes.factory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.annotations.Nonempty;
import com.helger.commons.annotations.UnsupportedOperation;
import com.helger.commons.scopes.MetaScopeFactory;
import com.helger.commons.scopes.domain.IApplicationScope;
import com.helger.commons.scopes.domain.IGlobalScope;
import com.helger.commons.scopes.domain.IRequestScope;
import com.helger.commons.scopes.domain.ISessionApplicationScope;
import com.helger.commons.scopes.domain.ISessionScope;
import com.helger.commons.scopes.factory.IScopeFactory;
import com.helger.commons.string.ToStringGenerator;

/**
 * A special {@link IScopeFactory} implementation that throws unsupported
 * operation exceptions for each scope type. This can be helpful to avoid
 * creating non-web scopes in web applicaiton. Note: an implementation of this
 * class must explicitly be set in {@link MetaScopeFactory}!
 * 
 * @author Philip Helger
 */
@NotThreadSafe
public class ThrowingScopeFactory implements IScopeFactory
{
  @UnsupportedOperation
  public IGlobalScope createGlobalScope (@Nonnull @Nonempty final String sScopeID)
  {
    throw new UnsupportedOperationException ("Cannot create a non-web global scope with ID '" + sScopeID + "'");
  }

  @UnsupportedOperation
  public IApplicationScope createApplicationScope (@Nonnull @Nonempty final String sScopeID)
  {
    throw new UnsupportedOperationException ("Cannot create a non-web application scope with ID '" + sScopeID + "'");
  }

  @UnsupportedOperation
  public ISessionScope createSessionScope (@Nonnull @Nonempty final String sScopeID)
  {
    throw new UnsupportedOperationException ("Cannot create a non-web session scope with ID '" + sScopeID + "'");
  }

  @UnsupportedOperation
  public ISessionApplicationScope createSessionApplicationScope (@Nonnull @Nonempty final String sScopeID)
  {
    throw new UnsupportedOperationException ("Cannot create a non-web session application scope with ID '" +
                                             sScopeID +
                                             "'");
  }

  @UnsupportedOperation
  public IRequestScope createRequestScope (@Nonnull @Nonempty final String sScopeID,
                                           @Nonnull @Nonempty final String sSessionID)
  {
    throw new UnsupportedOperationException ("Cannot create a non-web request scope with ID '" +
                                             sScopeID +
                                             "' and session ID '" +
                                             sSessionID +
                                             "'");
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).toString ();
  }

  /**
   * Install this scope factory into {@link MetaScopeFactory}.
   */
  public static void installToMetaScopeFactory ()
  {
    MetaScopeFactory.setScopeFactory (new ThrowingScopeFactory ());
  }
}

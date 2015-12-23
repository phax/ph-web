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
package com.helger.web.scope;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.scope.SessionApplicationScope;
import com.helger.commons.state.EChange;

/**
 * Represents a single "session application scope". This is a scope that is
 * specific to the selected application within the global scope and to the
 * current user session.
 *
 * @author Philip Helger
 */
@ThreadSafe
public class SessionApplicationWebScope extends SessionApplicationScope implements ISessionApplicationWebScope
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (SessionApplicationWebScope.class);

  public SessionApplicationWebScope (@Nonnull @Nonempty final String sScopeID)
  {
    super (sScopeID);
  }

  @Override
  @Nonnull
  public EChange setAttribute (@Nonnull final String sName, @Nullable final Object aNewValueValue)
  {
    if (aNewValueValue != null && !(aNewValueValue instanceof Serializable))
      s_aLogger.warn ("Value of class " + aNewValueValue.getClass ().getName () + " should implement Serializable!");

    return super.setAttribute (sName, aNewValueValue);
  }
}

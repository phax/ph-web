/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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
package com.helger.web.scope.util;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.lang.GenericReflection;
import com.helger.scope.IScope;
import com.helger.scope.mgr.ScopeManager;
import com.helger.web.scope.mgr.WebScopeManager;

/**
 * A specialized request field that uses a value stored in the session as the
 * default, in case no parameter is passed. If no value is in the session either
 * the hard coded default value is used.
 *
 * @author Philip Helger
 * @since 9.0.2
 */
public class SessionBackedRequestFieldDataMultiValue extends RequestFieldDataMultiValue
{
  public SessionBackedRequestFieldDataMultiValue (@Nonnull final String sFieldName)
  {
    super (sFieldName);
    _init ();
  }

  public SessionBackedRequestFieldDataMultiValue (@Nonnull final String sFieldName, @Nullable final Collection <String> aDefaultValues)
  {
    super (sFieldName, aDefaultValues);
    _init ();
  }

  /**
   * @return The name of the session scope variable that contains the stored
   *         value.
   */
  @Nonnull
  @Nonempty
  public String getSessionFieldName ()
  {
    return ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL + "requestfield.multi." + getFieldName ();
  }

  private void _init ()
  {
    // get the request value (without the default values)
    final ICommonsList <String> aRequestValues = super.getRequestValuesWithoutDefault ();
    // Allow empty values!
    if (aRequestValues != null)
    {
      // Remember in session - so a session can be created here
      WebScopeManager.getSessionScope (true).attrs ().putIn (getSessionFieldName (), aRequestValues);
    }
  }

  @Override
  public ICommonsList <String> getDefaultValues ()
  {
    final ICommonsList <String> aSuperDefaultValues = super.getDefaultValues ();
    // Get session scope only if it already exists - don't create one here!
    final IScope aSessionScope = WebScopeManager.getSessionScope (false);
    if (aSessionScope != null)
    {
      final Object aSessionValue = aSessionScope.attrs ().get (getSessionFieldName ());
      if (aSessionValue != null)
        return GenericReflection.uncheckedCast (aSessionValue);
    }
    return aSuperDefaultValues;
  }
}

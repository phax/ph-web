/*
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.scope.IScope;
import com.helger.scope.mgr.ScopeManager;
import com.helger.web.scope.mgr.WebScopeManager;

/**
 * A specialized request field that uses a value stored in the session as the
 * default, in case no parameter is passed. If no value is in the session either
 * the hard coded default value is used.
 *
 * @author Philip Helger
 */
public class SessionBackedRequestFieldData extends RequestFieldData
{
  public SessionBackedRequestFieldData (@Nonnull final String sFieldName)
  {
    super (sFieldName);
    _init ();
  }

  public SessionBackedRequestFieldData (@Nonnull final String sFieldName, @Nullable final String sDefaultValue)
  {
    super (sFieldName, sDefaultValue);
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
    return ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL + "requestfield." + getFieldName ();
  }

  private void _init ()
  {
    // get the request value (without the default values)
    final String sRequestValue = super.getRequestValueWithoutDefault ();
    // Allow empty values!
    if (sRequestValue != null)
    {
      // Remember in session - so a session can be created here
      WebScopeManager.getSessionScope (true).attrs ().putIn (getSessionFieldName (), sRequestValue);
    }
  }

  @Override
  public String getDefaultValue ()
  {
    final String sSuperDefaultValue = super.getDefaultValue ();
    // Get session scope only if it already exists - don't create one here!
    final IScope aSessionScope = WebScopeManager.getSessionScope (false);
    return aSessionScope == null ? sSuperDefaultValue : aSessionScope.attrs ().getAsString (getSessionFieldName (), sSuperDefaultValue);
  }
}

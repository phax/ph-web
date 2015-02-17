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
package com.helger.webscopes.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotations.Nonempty;
import com.helger.scopes.IScope;
import com.helger.webscopes.mgr.WebScopeManager;

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

  public SessionBackedRequestFieldData (@Nonnull final String sFieldName, final int nDefaultValue)
  {
    super (sFieldName, nDefaultValue);
    _init ();
  }

  @Nonnull
  @Nonempty
  public String getSessionFieldName ()
  {
    return "$ph.requestfield." + getFieldName ();
  }

  private void _init ()
  {
    // get the request method
    final String sRequestValue = super.getRequestValueWithoutDefault ();
    // Allow empty values!
    if (sRequestValue != null)
      WebScopeManager.getSessionScope (true).setAttribute (getSessionFieldName (), sRequestValue);
  }

  @Override
  public String getDefaultValue ()
  {
    final String sSuperDefaultValue = super.getDefaultValue ();
    final IScope aSessionScope = WebScopeManager.getSessionScope (false);
    return aSessionScope == null ? sSuperDefaultValue : aSessionScope.getAttributeAsString (getSessionFieldName (),
                                                                                            sSuperDefaultValue);
  }
}

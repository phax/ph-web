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
package com.helger.web.http;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.name.IHasName;

/**
 * HTTP 1.1 methods.<br>
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
 *
 * @author Philip Helger
 */
public enum EHTTPMethod implements IHasName
{
 OPTIONS ("OPTIONS"),
 GET ("GET"),
 HEAD ("HEAD"),
 POST ("POST"),
 PUT ("PUT"),
 DELETE ("DELETE"),
 TRACE ("TRACE"),
 CONNECT ("CONNECT");

  private final String m_sName;

  private EHTTPMethod (@Nonnull @Nonempty final String sName)
  {
    m_sName = sName;
  }

  @Nonnull
  @Nonempty
  public String getName ()
  {
    return m_sName;
  }

  public boolean isIdempodent ()
  {
    return this != POST && this != CONNECT;
  }

  public boolean isContentAllowed ()
  {
    return this != HEAD;
  }

  @Nullable
  public static EHTTPMethod getFromNameOrNull (@Nullable final String sName)
  {
    return EnumHelper.getFromNameCaseInsensitiveOrNull (EHTTPMethod.class, sName);
  }
}

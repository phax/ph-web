/**
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
package com.helger.servlet.logging;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;

final class LoggingRequest implements Serializable
{
  private String m_sSender;
  private String m_sMethod;
  private String m_sPath;
  private ICommonsMap <String, String> m_aParams;
  private HttpHeaderMap m_aHeaders;
  private String m_sBody;

  @Nullable
  public String getSender ()
  {
    return m_sSender;
  }

  public void setSender (@Nullable final String sSender)
  {
    m_sSender = sSender;
  }

  @Nullable
  public String getMethod ()
  {
    return m_sMethod;
  }

  public void setMethod (@Nullable final String sMethod)
  {
    m_sMethod = sMethod;
  }

  @Nullable
  public String getPath ()
  {
    return m_sPath;
  }

  public void setPath (@Nullable final String sPath)
  {
    m_sPath = sPath;
  }

  @Nullable
  public ICommonsMap <String, String> getParams ()
  {
    return m_aParams;
  }

  public void setParams (@Nullable final ICommonsMap <String, String> aParams)
  {
    m_aParams = aParams;
  }

  @Nullable
  public HttpHeaderMap getHeaders ()
  {
    return m_aHeaders;
  }

  public void setHeaders (@Nullable final HttpHeaderMap aHeaders)
  {
    m_aHeaders = aHeaders;
  }

  @Nullable
  public String getBody ()
  {
    return m_sBody;
  }

  public void setBody (@Nullable final String sBody)
  {
    m_sBody = sBody;
  }

  @Nonnull
  public IJsonObject getAsJson ()
  {
    final JsonObject ret = new JsonObject ();
    if (m_sSender != null)
      ret.add ("sender", m_sSender);
    if (m_sMethod != null)
      ret.add ("method", m_sMethod);
    if (m_sPath != null)
      ret.add ("path", m_sPath);
    if (m_aParams != null)
      ret.addJson ("params", new JsonObject ().addAll (m_aParams));
    if (m_aHeaders != null)
    {
      final IJsonObject aHeaders = new JsonObject ();
      m_aHeaders.forEachSingleHeader (aHeaders::add, true);
      ret.addJson ("headers", aHeaders);
    }
    if (m_sBody != null)
      ret.add ("body", m_sBody);
    return ret;
  }
}

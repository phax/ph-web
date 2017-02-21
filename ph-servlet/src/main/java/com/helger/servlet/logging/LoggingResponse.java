package com.helger.servlet.logging;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.http.HTTPHeaderMap;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;

final class LoggingResponse implements Serializable
{
  private int m_nStatus;
  private HTTPHeaderMap m_aHeaders;
  private String m_sBody;

  public int getStatus ()
  {
    return m_nStatus;
  }

  public void setStatus (final int nStatus)
  {
    m_nStatus = nStatus;
  }

  @Nullable
  public HTTPHeaderMap getHeaders ()
  {
    return m_aHeaders;
  }

  public void setHeaders (@Nullable final HTTPHeaderMap aHeaders)
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
    if (m_nStatus != 0)
      ret.add ("status", m_nStatus);
    if (m_aHeaders != null)
    {
      final IJsonObject aHeaders = new JsonObject ();
      m_aHeaders.forEachSingleHeader ( (k, v) -> aHeaders.add (k, v));
      ret.add ("headers", aHeaders);
    }
    if (m_sBody != null)
      ret.add ("body", m_sBody);
    return ret;
  }
}

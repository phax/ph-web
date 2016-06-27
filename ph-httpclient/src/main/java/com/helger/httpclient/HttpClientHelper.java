package com.helger.httpclient;

import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.entity.ContentType;

import com.helger.commons.string.StringHelper;
import com.helger.commons.url.ISimpleURL;
import com.helger.web.http.EHTTPMethod;

public final class HttpClientHelper
{
  private HttpClientHelper ()
  {}

  @Nonnull
  public static HttpRequestBase createRequest (@Nonnull final EHTTPMethod eHTTPMethod,
                                               @Nonnull final ISimpleURL aSimpleURL)
  {
    final String sURL = aSimpleURL.getAsStringWithEncodedParameters ();
    switch (eHTTPMethod)
    {
      case DELETE:
        return new HttpDelete (sURL);
      case GET:
        return new HttpGet (sURL);
      case HEAD:
        return new HttpHead (sURL);
      case OPTIONS:
        return new HttpOptions (sURL);
      case TRACE:
        return new HttpTrace (sURL);
      case POST:
        return new HttpPost (sURL);
      case PUT:
        return new HttpPut (sURL);
      default:
        throw new IllegalStateException ("Unsupported HTTP method: " + eHTTPMethod);
    }
  }

  @Nullable
  public static ContentType createContentType (@Nullable final String sContentType, @Nullable final Charset aCharset)
  {
    if (StringHelper.hasNoText (sContentType))
      return null;
    return ContentType.create (sContentType, aCharset);
  }
}
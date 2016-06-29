package com.helger.httpclient;

import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import com.helger.commons.string.StringHelper;
import com.helger.commons.url.ISimpleURL;
import com.helger.web.http.EHTTPMethod;

/**
 * Some utility methods for creating and handling Apache httpclient objects.
 *
 * @author Philip Helger
 */
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

  @Nonnull
  public static Charset getCharset (@Nonnull final ContentType aContentType)
  {
    final Charset ret = aContentType.getCharset ();
    return ret != null ? ret : HTTP.DEF_CONTENT_CHARSET;
  }

  @Nullable
  public static HttpContext createHttpContext (@Nullable final HttpHost aProxy)
  {
    if (aProxy == null)
      return null;
    final HttpClientContext ret = new HttpClientContext ();
    ret.setRequestConfig (RequestConfig.custom ().setProxy (aProxy).build ());
    return ret;
  }
}

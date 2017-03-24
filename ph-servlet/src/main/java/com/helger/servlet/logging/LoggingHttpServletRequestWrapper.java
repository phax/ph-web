package com.helger.servlet.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.helger.commons.charset.CharsetManager;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.ext.CommonsHashMap;
import com.helger.commons.collection.ext.ICommonsMap;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.string.StringHelper;
import com.helger.http.EHTTPMethod;

public class LoggingHttpServletRequestWrapper extends HttpServletRequestWrapper
{
  private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";
  private static final String METHOD_POST = EHTTPMethod.POST.getName ();

  private byte [] m_aContent;
  private final Map <String, String []> m_aParameterMap;
  private final HttpServletRequest m_aDelegate;

  public LoggingHttpServletRequestWrapper (@Nonnull final HttpServletRequest request)
  {
    super (request);
    m_aDelegate = request;
    if (isFormPost ())
      m_aParameterMap = request.getParameterMap ();
    else
      m_aParameterMap = Collections.emptyMap ();
  }

  @Override
  public ServletInputStream getInputStream () throws IOException
  {
    if (ArrayHelper.isEmpty (m_aContent))
    {
      return m_aDelegate.getInputStream ();
    }
    return new LoggingServletInputStream (m_aContent);
  }

  @Override
  public BufferedReader getReader () throws IOException
  {
    if (ArrayHelper.isEmpty (m_aContent))
    {
      return m_aDelegate.getReader ();
    }
    return new BufferedReader (new InputStreamReader (getInputStream (), _getCharset ()));
  }

  @Override
  public String getParameter (final String name)
  {
    if (ArrayHelper.isEmpty (m_aContent) || m_aParameterMap.isEmpty ())
    {
      return super.getParameter (name);
    }
    final String [] values = m_aParameterMap.get (name);
    if (values != null && values.length > 0)
    {
      return values[0];
    }
    return Arrays.toString (values);
  }

  @Override
  public Map <String, String []> getParameterMap ()
  {
    if (ArrayHelper.isEmpty (m_aContent) || m_aParameterMap.isEmpty ())
    {
      return super.getParameterMap ();
    }
    return m_aParameterMap;
  }

  @Override
  public Enumeration <String> getParameterNames ()
  {
    if (ArrayHelper.isEmpty (m_aContent) || m_aParameterMap.isEmpty ())
    {
      return super.getParameterNames ();
    }
    return new ParamNameEnumeration (m_aParameterMap.keySet ());
  }

  @Override
  public String [] getParameterValues (final String name)
  {
    if (ArrayHelper.isEmpty (m_aContent) || m_aParameterMap.isEmpty ())
    {
      return super.getParameterValues (name);
    }
    return m_aParameterMap.get (name);
  }

  @Nonnull
  private Charset _getCharset ()
  {
    final String sRequestEncoding = m_aDelegate.getCharacterEncoding ();
    final Charset aCharset = sRequestEncoding != null ? CharsetManager.getCharsetFromName (sRequestEncoding)
                                                      : StandardCharsets.UTF_8;
    return aCharset;
  }

  public String getContent ()
  {
    try
    {
      String sNormalizedContent;
      if (m_aParameterMap.isEmpty ())
      {
        m_aContent = StreamHelper.getAllBytes (m_aDelegate.getInputStream ());
        sNormalizedContent = new String (m_aContent, _getCharset ());
      }
      else
      {
        sNormalizedContent = getContentFromParameterMap (m_aParameterMap);
        m_aContent = sNormalizedContent.getBytes (_getCharset ());
      }
      return StringHelper.hasNoText (sNormalizedContent) ? "[EMPTY]" : sNormalizedContent;
    }
    catch (final IOException e)
    {
      throw new IllegalStateException (e);
    }
  }

  private String getContentFromParameterMap (final Map <String, String []> parameterMap)
  {
    final StringBuilder aSB = new StringBuilder ();
    for (final Map.Entry <String, String []> aEntry : parameterMap.entrySet ())
      for (final String sValue : aEntry.getValue ())
      {
        if (aSB.length () > 0)
          aSB.append ('&');
        aSB.append (aEntry.getKey ()).append ('=').append (sValue);
      }
    return aSB.toString ();
  }

  @Nonnull
  public ICommonsMap <String, String> getParameters ()
  {
    final ICommonsMap <String, String> ret = new CommonsHashMap<> ();
    for (final Map.Entry <String, String []> aEntry : getParameterMap ().entrySet ())
    {
      final String sKey = aEntry.getKey ();
      final String [] aValues = aEntry.getValue ();
      if (aValues.length == 0)
        ret.put (sKey, "[EMPTY]");
      else
        if (aValues.length == 1)
          ret.put (sKey, aValues[0]);
        else
          ret.put (sKey, Arrays.toString (aValues));
    }
    return ret;
  }

  public boolean isFormPost ()
  {
    final String sContentType = getContentType ();
    return sContentType != null &&
           sContentType.contains (FORM_CONTENT_TYPE) &&
           METHOD_POST.equalsIgnoreCase (getMethod ());
  }

  private static class ParamNameEnumeration implements Enumeration <String>
  {
    private final Iterator <String> m_aIt;

    private ParamNameEnumeration (final Set <String> values)
    {
      m_aIt = values != null ? values.iterator () : Collections.emptyIterator ();
    }

    @Override
    public boolean hasMoreElements ()
    {
      return m_aIt.hasNext ();
    }

    @Override
    public String nextElement ()
    {
      return m_aIt.next ();
    }
  }
}

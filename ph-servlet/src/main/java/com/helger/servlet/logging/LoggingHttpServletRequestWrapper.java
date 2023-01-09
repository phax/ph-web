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
import javax.annotation.Nullable;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.helger.commons.annotation.CodingStyleguideUnaware;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.charset.CharsetHelper;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.mime.CMimeType;
import com.helger.commons.string.StringHelper;

public class LoggingHttpServletRequestWrapper extends HttpServletRequestWrapper
{
  private static final String FORM_CONTENT_TYPE = CMimeType.APPLICATION_X_WWW_FORM_URLENCODED.getAsString ();
  private static final String METHOD_POST = EHttpMethod.POST.getName ();

  private byte [] m_aContent;
  @CodingStyleguideUnaware
  private final Map <String, String []> m_aParameterMap;
  private final HttpServletRequest m_aDelegate;

  public LoggingHttpServletRequestWrapper (@Nonnull final HttpServletRequest aRequest)
  {
    super (aRequest);
    m_aDelegate = aRequest;
    if (isFormPost ())
      m_aParameterMap = aRequest.getParameterMap ();
    else
      m_aParameterMap = Collections.emptyMap ();
  }

  @Override
  public ServletInputStream getInputStream () throws IOException
  {
    if (ArrayHelper.isEmpty (m_aContent))
      return m_aDelegate.getInputStream ();

    return new LoggingServletInputStream (m_aContent);
  }

  @Override
  public BufferedReader getReader () throws IOException
  {
    if (ArrayHelper.isEmpty (m_aContent))
      return m_aDelegate.getReader ();

    return new BufferedReader (new InputStreamReader (getInputStream (), _getCharset ()));
  }

  @Override
  @Nullable
  public String getParameter (@Nullable final String sName)
  {
    if (ArrayHelper.isEmpty (m_aContent) || m_aParameterMap.isEmpty ())
      return super.getParameter (sName);

    final String [] aValues = m_aParameterMap.get (sName);
    if (aValues != null && aValues.length > 0)
      return aValues[0];
    return Arrays.toString (aValues);
  }

  @Override
  @Nonnull
  public Map <String, String []> getParameterMap ()
  {
    if (ArrayHelper.isEmpty (m_aContent) || m_aParameterMap.isEmpty ())
      return super.getParameterMap ();

    return m_aParameterMap;
  }

  @Override
  @Nonnull
  public Enumeration <String> getParameterNames ()
  {
    if (ArrayHelper.isEmpty (m_aContent) || m_aParameterMap.isEmpty ())
      return super.getParameterNames ();

    return new ParamNameEnumeration (m_aParameterMap.keySet ());
  }

  @Override
  @Nullable
  public String [] getParameterValues (@Nullable final String sName)
  {
    if (ArrayHelper.isEmpty (m_aContent) || m_aParameterMap.isEmpty ())
      return super.getParameterValues (sName);

    return m_aParameterMap.get (sName);
  }

  @Nonnull
  private Charset _getCharset ()
  {
    final String sRequestEncoding = m_aDelegate.getCharacterEncoding ();
    return CharsetHelper.getCharsetFromNameOrDefault (sRequestEncoding, StandardCharsets.UTF_8);
  }

  @Nonnull
  public String getContent ()
  {
    try
    {
      final String sNormalizedContent;
      if (m_aParameterMap.isEmpty ())
      {
        m_aContent = StreamHelper.getAllBytes (m_aDelegate.getInputStream ());
        sNormalizedContent = new String (m_aContent, _getCharset ());
      }
      else
      {
        sNormalizedContent = _getContentFromParameterMap (m_aParameterMap);
        m_aContent = sNormalizedContent.getBytes (_getCharset ());
      }
      return StringHelper.hasNoText (sNormalizedContent) ? "[EMPTY]" : sNormalizedContent;
    }
    catch (final IOException e)
    {
      throw new IllegalStateException (e);
    }
  }

  @Nonnull
  private static String _getContentFromParameterMap (@Nonnull final Map <String, String []> aParameterMap)
  {
    final StringBuilder aSB = new StringBuilder ();
    for (final Map.Entry <String, String []> aEntry : aParameterMap.entrySet ())
      for (final String sValue : aEntry.getValue ())
      {
        if (aSB.length () > 0)
          aSB.append ('&');
        aSB.append (aEntry.getKey ()).append ('=').append (sValue);
      }
    return aSB.toString ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsMap <String, String> getParameters ()
  {
    final ICommonsMap <String, String> ret = new CommonsHashMap <> ();
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
    return sContentType != null && sContentType.contains (FORM_CONTENT_TYPE) && METHOD_POST.equalsIgnoreCase (getMethod ());
  }

  private static class ParamNameEnumeration implements Enumeration <String>
  {
    private final Iterator <String> m_aIt;

    private ParamNameEnumeration (@Nullable final Set <String> aValues)
    {
      m_aIt = aValues != null ? aValues.iterator () : Collections.emptyIterator ();
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

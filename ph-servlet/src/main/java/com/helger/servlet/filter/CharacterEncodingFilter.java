/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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
package com.helger.servlet.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.charset.CharsetHelper;
import com.helger.commons.mime.EMimeContentType;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;
import com.helger.servlet.ServletHelper;

/**
 * Special servlet filter that applies a certain encoding to a request and a
 * response. This must be a filter. Changing the request encoding does not work
 * in a servlet!
 *
 * @author Philip Helger
 */
public class CharacterEncodingFilter extends AbstractHttpServletFilter
{
  /** Name of the init parameter for the encoding */
  public static final String INITPARAM_ENCODING = "encoding";
  /** Name of the init parameter to force setting the request encoding */
  public static final String INITPARAM_FORCE_REQUEST_ENCODING = "forceRequestEncoding";
  /** Name of the init parameter to force setting the response encoding */
  public static final String INITPARAM_FORCE_RESPONSE_ENCODING = "forceResponseEncoding";
  /**
   * Name of the init parameter to force setting the request and response
   * encoding
   */
  public static final String INITPARAM_FORCE_ENCODING = "forceEncoding";

  /** The default encoding is UTF-8 */
  public static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name ();
  /** By default the encoding is not enforced. */
  public static final boolean DEFAULT_FORCE_ENCODING = false;
  // Cannot reference ScopeManager here!
  private static final String REQUEST_ATTR = "$ph-" + CharacterEncodingFilter.class.getName ();
  private static final Logger s_aLogger = LoggerFactory.getLogger (CharacterEncodingFilter.class);

  private String m_sEncoding = DEFAULT_ENCODING;
  private boolean m_bForceRequestEncoding = DEFAULT_FORCE_ENCODING;
  private boolean m_bForceResponseEncoding = DEFAULT_FORCE_ENCODING;

  public CharacterEncodingFilter ()
  {}

  /**
   * @return The encoding to be used by this filter. Neither <code>null</code>
   *         nor empty.
   */
  @Nonnull
  @Nonempty
  public final String getEncoding ()
  {
    return m_sEncoding;
  }

  public final void setEncoding (@Nonnull @Nonempty final String sEncoding)
  {
    ValueEnforcer.notEmpty (sEncoding, "Encoding");
    // Throws IllegalArgumentException in case it is unknown
    CharsetHelper.getCharsetFromName (sEncoding);
    m_sEncoding = sEncoding;
  }

  public final boolean isForceRequestEncoding ()
  {
    return m_bForceRequestEncoding;
  }

  public final void setForceRequestEncoding (final boolean bForce)
  {
    m_bForceRequestEncoding = bForce;
  }

  public final boolean isForceResponseEncoding ()
  {
    return m_bForceRequestEncoding;
  }

  public final void setForceResponseEncoding (final boolean bForce)
  {
    m_bForceResponseEncoding = bForce;
  }

  public final void setForceEncoding (final boolean bForce)
  {
    setForceRequestEncoding (bForce);
    setForceResponseEncoding (bForce);
  }

  @Override
  public void init () throws ServletException
  {
    super.init ();

    // encoding
    final String sEncoding = getFilterConfig ().getInitParameter (INITPARAM_ENCODING);
    if (StringHelper.hasText (sEncoding))
      setEncoding (sEncoding);

    // force request encoding?
    String sForce = getFilterConfig ().getInitParameter (INITPARAM_FORCE_REQUEST_ENCODING);
    if (sForce != null)
      setForceRequestEncoding (StringParser.parseBool (sForce));

    // force response encoding?
    sForce = getFilterConfig ().getInitParameter (INITPARAM_FORCE_RESPONSE_ENCODING);
    if (sForce != null)
      setForceResponseEncoding (StringParser.parseBool (sForce));

    // force encoding?
    sForce = getFilterConfig ().getInitParameter (INITPARAM_FORCE_ENCODING);
    if (sForce != null)
      setForceEncoding (StringParser.parseBool (sForce));
  }

  @Override
  public void doHttpFilter (@Nonnull final HttpServletRequest aRequest,
                            @Nonnull final HttpServletResponse aResponse,
                            @Nonnull final FilterChain aChain) throws IOException, ServletException
  {
    // Avoid double filtering
    boolean bPerform;
    if (aRequest.getAttribute (REQUEST_ATTR) == null)
    {
      bPerform = true;
      ServletHelper.setRequestAttribute (aRequest, REQUEST_ATTR, Boolean.TRUE);
    }
    else
      bPerform = false;

    if (bPerform)
    {
      final String sOldRequestEncoding = aRequest.getCharacterEncoding ();
      // We need this for all form data etc.
      if (sOldRequestEncoding == null || m_bForceRequestEncoding)
      {
        aRequest.setCharacterEncoding (m_sEncoding);
        if (aRequest.getCharacterEncoding () == null)
          s_aLogger.error ("Failed to set the request character encoding to '" + m_sEncoding + "'");
        else
          if (sOldRequestEncoding != null && !m_sEncoding.equalsIgnoreCase (sOldRequestEncoding))
          {
            /*
             * Request encoding should always be present (at least from
             * browsers)
             */
            s_aLogger.info ("Changed request encoding from '" + sOldRequestEncoding + "' to '" + m_sEncoding + "'");
          }
      }
    }

    // Next filter in the chain
    aChain.doFilter (aRequest, aResponse);

    if (bPerform)
    {
      // Maybe null e.g. for HTTP 304
      final String sContentType = aResponse.getContentType ();

      // Only apply to "text/" MIME types
      final boolean bCanApplyCharset = sContentType != null && EMimeContentType.TEXT.isTypeOf (sContentType);
      if (bCanApplyCharset)
      {
        final String sOldResponseEncoding = aResponse.getCharacterEncoding ();
        if (sOldResponseEncoding == null || m_bForceResponseEncoding)
        {
          aResponse.setCharacterEncoding (m_sEncoding);
          if (aResponse.getCharacterEncoding () == null)
            s_aLogger.error ("Failed to set the response character encoding to '" + m_sEncoding + "'");
          else
            if (sOldResponseEncoding != null && !m_sEncoding.equalsIgnoreCase (sOldResponseEncoding))
            {
              /*
               * Default response encoding in Jetty 9.x is "iso-8859-1" on
               * German Windows 7 machine
               */
              if (s_aLogger.isDebugEnabled ())
                s_aLogger.debug ("Changed response encoding from '" +
                                 sOldResponseEncoding +
                                 "' to '" +
                                 m_sEncoding +
                                 "' for MIME type '" +
                                 sContentType +
                                 "'");
            }
        }
      }
    }
  }
}

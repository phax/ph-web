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
package com.helger.http.csp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.text.util.ABNF;

/**
 * A single CSP 2.0 directive. It's a name-value-pair.
 *
 * @author Philip Helger
 */
public class CSP2Directive implements ICSPDirective
{
  private final String m_sName;
  private final String m_sValue;

  public static boolean isValidName (@Nullable final String sName)
  {
    if (StringHelper.hasNoText (sName))
    {
      // Empty name is not allowed
      return false;
    }
    final char [] aChars = sName.toCharArray ();
    for (final char c : aChars)
      if (!ABNF.isAlpha (c) && !ABNF.isDigit (c) && c != '-')
        return false;

    return true;
  }

  public static boolean isValidValue (@Nullable final String sValue)
  {
    if (StringHelper.hasNoText (sValue))
    {
      // Empty values are allowed
      return true;
    }
    final char [] aChars = sValue.toCharArray ();
    for (final char c : aChars)
      if (!ABNF.isWSP (c) && (!ABNF.isVChar (c) || c == ';' || c == ','))
        return false;

    return true;
  }

  public CSP2Directive (@Nonnull @Nonempty final String sName, @Nullable final CSP2SourceList aValue)
  {
    this (sName, aValue == null ? null : aValue.getAsString ());
  }

  public CSP2Directive (@Nonnull @Nonempty final String sName, @Nullable final String sValue)
  {
    ValueEnforcer.isTrue (isValidName (sName), () -> "The CSP directive name '" + sName + "' is invalid!");
    ValueEnforcer.isTrue (isValidValue (sValue), () -> "The CSP directive value '" + sValue + "' is invalid!");
    m_sName = sName;
    m_sValue = sValue;
  }

  @Nonnull
  @Nonempty
  public final String getName ()
  {
    return m_sName;
  }

  @Nullable
  public final String getValue ()
  {
    return m_sValue;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final CSP2Directive rhs = (CSP2Directive) o;
    return m_sName.equals (rhs.m_sName) && EqualsHelper.equals (m_sValue, rhs.m_sValue);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sName).append (m_sValue).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("name", m_sName).appendIfNotNull ("value", m_sValue).getToString ();
  }

  /**
   * The default-src is the default policy for loading content such as
   * JavaScript, Images, CSS, Fonts, AJAX requests, Frames, HTML5 Media.
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v1
   */
  @Nonnull
  public static CSP2Directive createDefaultSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("default-src", aValue);
  }

  /**
   * Defines valid sources of JavaScript.
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v1
   */
  @Nonnull
  public static CSP2Directive createScriptSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("script-src", aValue);
  }

  /**
   * Defines valid sources of stylesheets.
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v1
   */
  @Nonnull
  public static CSP2Directive createStyleSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("style-src", aValue);
  }

  /**
   * Defines valid sources of images.
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v1
   */
  @Nonnull
  public static CSP2Directive createImgSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("img-src", aValue);
  }

  /**
   * Applies to XMLHttpRequest (AJAX), WebSocket or EventSource. If not allowed
   * the browser emulates a 400 HTTP status code.
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v1
   */
  @Nonnull
  public static CSP2Directive createConnectSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("connect-src", aValue);
  }

  /**
   * Defines valid sources of fonts.
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v1
   */
  @Nonnull
  public static CSP2Directive createFontSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("font-src", aValue);
  }

  /**
   * Defines valid sources of plugins, eg &lt;object&gt;, &lt;embed&gt; or
   * &lt;applet&gt;.
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v1
   */
  @Nonnull
  public static CSP2Directive createObjectSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("object-src", aValue);
  }

  /**
   * Defines valid sources of audio and video, eg HTML5 &lt;audio&gt;,
   * &lt;video&gt; elements.
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v1
   */
  @Nonnull
  public static CSP2Directive createMediaSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("media-src", aValue);
  }

  /**
   * The sandbox directive specifies an HTML sandbox policy that the user agent
   * applies to the protected resource.
   *
   * @param sValue
   *        value
   * @return new directive
   * @since CSP v1
   */
  @Nonnull
  public static CSP2Directive createSandbox (@Nullable final String sValue)
  {
    return new CSP2Directive ("sandbox", sValue);
  }

  /**
   * The report-uri directive specifies a URI to which the user agent sends
   * reports about policy violation.
   *
   * @param sValue
   *        Report URI
   * @return new directive
   * @since CSP v1
   */
  @Nonnull
  public static CSP2Directive createReportURI (@Nullable final String sValue)
  {
    return new CSP2Directive ("report-uri", sValue);
  }

  /**
   * Restricts the URLs which can be used in a document's &lt;base&gt; element.
   * If this value is absent, then any URI is allowed. If this directive is
   * absent, the user agent will use the value in the &lt;base&gt; element.
   *
   * @param sValue
   *        value
   * @return new directive
   * @since CSP v2
   */
  @Nonnull
  public static CSP2Directive createBaseURI (@Nullable final String sValue)
  {
    return new CSP2Directive ("base-uri", sValue);
  }

  /**
   * Defines valid sources for web workers and nested browsing contexts loaded
   * using elements such as &lt;frame&gt; and &lt;iframe&gt;
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v2
   */
  @Nonnull
  public static CSP2Directive createChildSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("child-src", aValue);
  }

  /**
   * Defines valid sources that can be used as a HTML &lt;form&gt; action.
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v2
   */
  @Nonnull
  public static CSP2Directive createFormAction (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("form-action", aValue);
  }

  /**
   * Defines valid sources for embedding the resource using &lt;frame&gt;
   * &lt;iframe&gt; &lt;object&gt; &lt;embed&gt; &lt;applet&gt;. Setting this
   * directive to <code>'none'</code> should be roughly equivalent to
   * <code>X-Frame-Options: DENY</code>
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v2
   */
  @Nonnull
  public static CSP2Directive createFrameAncestors (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("frame-ancestors", aValue);
  }

  /**
   * Defines valid MIME types for plugins invoked via &lt;object&gt; and
   * &lt;embed&gt;. To load an &lt;applet&gt; you must specify
   * <code>application/x-java-applet</code>.<br>
   * Not supported in Firefox up to v61.
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v2
   */
  @Nonnull
  public static CSP2Directive createPluginTypes (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("plugin-types", aValue);
  }

  /**
   * Specifies valid sources of application manifest files.
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v3
   * @since 9.3.5
   */
  @Nonnull
  public static CSP2Directive createManifestSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("manifest-src", aValue);
  }

  /**
   * Specifies valid sources to be prefetched or prerendered (draft).
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v3
   * @since 9.3.5
   */
  @Nonnull
  public static CSP2Directive createPrefetchSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("prefetch-src", aValue);
  }

  /**
   * pecifies valid sources for Worker, SharedWorker, or ServiceWorker scripts.
   * (draft).
   *
   * @param aValue
   *        Value list to use. May be be <code>null</code>.
   * @return New {@link CSP2Directive}
   * @since CSP v3
   * @since 9.3.5
   */
  @Nonnull
  public static CSP2Directive createWorkerSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("worker-src", aValue);
  }
}

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
package com.helger.web.scope.impl;

import java.io.Serializable;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Enumeration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.attr.AttributeContainerAny;
import com.helger.commons.collection.attr.IAttributeContainerAny;
import com.helger.commons.collection.iterate.EmptyEnumeration;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.commons.id.factory.GlobalIDFactory;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.state.EChange;
import com.helger.commons.string.ToStringGenerator;
import com.helger.scope.AbstractScope;
import com.helger.scope.ScopeHelper;
import com.helger.scope.mgr.ScopeManager;
import com.helger.servlet.ServletContextPathHolder;
import com.helger.servlet.ServletSettings;
import com.helger.servlet.request.IRequestParamMap;
import com.helger.servlet.request.RequestHelper;
import com.helger.servlet.request.RequestParamMap;
import com.helger.web.scope.IRequestParamContainer;
import com.helger.web.scope.IRequestWebScope;

/**
 * A request web scopes that does not parse multipart requests.
 *
 * @author Philip Helger
 */
public class RequestWebScope extends AbstractScope implements IRequestWebScope
{
  /**
   * Special implementation if {@link IRequestParamContainer} based on
   * {@link AttributeContainerAny}.
   *
   * @author Philip Helger
   */
  public static class ParamContainer extends AttributeContainerAny <String> implements IRequestParamContainer
  {}

  /**
   * The param value cleanser interface to be used globally.
   *
   * @author Philip Helger
   * @since 9.0.6
   */
  @FunctionalInterface
  public static interface IParamValueCleanser extends Serializable
  {
    /**
     * Get the cleaned value of a parameter value.
     *
     * @param sParamName
     *        The current parameter name. May not be <code>null</code>.
     * @param nParamIndex
     *        The index of the value. If the parameter has multiple values this
     *        is respective index. If there is only one value, this is always 0
     *        (zero).
     * @param sParamValue
     *        The value to be cleaned. May be <code>null</code>.
     * @return The cleaned value. May also be <code>null</code>.
     */
    @Nullable
    String getCleanedValue (@Nonnull String sParamName, @Nonnegative int nParamIndex, @Nullable String sParamValue);
  }

  // Because of transient field
  private static final long serialVersionUID = 78563987233147L;

  private static final Logger LOGGER = LoggerFactory.getLogger (RequestWebScope.class);
  private static final String REQUEST_ATTR_SCOPE_INITED = ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL + "requestscope.inited";
  private static final SimpleReadWriteLock s_aRWLock = new SimpleReadWriteLock ();
  @GuardedBy ("s_aRWLock")
  private static IParamValueCleanser s_aParamValueCleanser = (n, i, v) -> getWithoutForbiddenCharsAndNormalized (v);

  private final LocalDateTime m_aCreationDT;
  protected final transient HttpServletRequest m_aHttpRequest;
  protected final transient HttpServletResponse m_aHttpResponse;
  private HttpHeaderMap m_aHeaders;
  private final ParamContainer m_aParams = new ParamContainer ();
  private IRequestParamMap m_aRequestParamMap;

  /**
   * @return The current value cleanser function. May be <code>null</code>. By
   *         default {@link #getWithoutForbiddenCharsAndNormalized(String)} is
   *         invoked.
   * @since 9.0.6
   * @see #setParamValueCleanser(IParamValueCleanser)
   */
  @Nullable
  public static IParamValueCleanser getParamValueCleanser ()
  {
    return s_aRWLock.readLockedGet ( () -> s_aParamValueCleanser);
  }

  /**
   * Set the param value cleanser function that is applied on all parameter
   * values. By default only
   * {@link #getWithoutForbiddenCharsAndNormalized(String)} is invoked.
   *
   * @param aParamValueCleanser
   *        The function to be applied. May be <code>null</code>. The function
   *        itself must be able to handle <code>null</code> values.
   * @since 9.0.6
   * @see #getParamValueCleanser()
   */
  public static void setParamValueCleanser (@Nullable final IParamValueCleanser aParamValueCleanser)
  {
    s_aRWLock.writeLockedGet ( () -> s_aParamValueCleanser = aParamValueCleanser);
  }

  @Nonnull
  @Nonempty
  private static String _createScopeID (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    return GlobalIDFactory.getNewIntID () + "@" + RequestHelper.getRequestURIDecoded (aHttpRequest);
  }

  public RequestWebScope (@Nonnull final HttpServletRequest aHttpRequest, @Nonnull final HttpServletResponse aHttpResponse)
  {
    super (_createScopeID (aHttpRequest));

    m_aCreationDT = PDTFactory.getCurrentLocalDateTime ();
    m_aHttpRequest = aHttpRequest;
    m_aHttpResponse = ValueEnforcer.notNull (aHttpResponse, "HttpResponse");

    // done initialization
    if (ScopeHelper.isDebugRequestScopeLifeCycle (LOGGER))
      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Created request web scope '" + super.getID () + "' of class " + ClassHelper.getClassLocalName (RequestWebScope.class),
                     ScopeHelper.getDebugStackTrace ());
  }

  @Nonnull
  public final LocalDateTime getCreationDateTime ()
  {
    return m_aCreationDT;
  }

  /**
   * Callback method to add special parameters.
   *
   * @return {@link EChange#CHANGED} if some attributes were added,
   *         <code>false</code> if not. If special attributes were added,
   *         existing attributes are kept and will not be overwritten with HTTP
   *         servlet request parameters!
   */
  @OverrideOnDemand
  @Nonnull
  protected EChange addSpecialRequestParams ()
  {
    return EChange.UNCHANGED;
  }

  /**
   * Check if the provided char is forbidden in a request value or not.
   *
   * @param c
   *        Char to check
   * @return <code>true</code> if it is forbidden, <code>false</code> if not.
   * @see #getWithoutForbiddenChars(String)
   * @since 9.0.6
   */
  public static boolean isForbiddenParamValueChar (final char c)
  {
    // INVALID_VALUE_CHAR_XML10 + 0x7f
    return (c >= 0x0 && c <= 0x8) ||
           (c >= 0xb && c <= 0xc) ||
           (c >= 0xe && c <= 0x1f) ||
           (c == 0x7f) ||
           (c >= 0xd800 && c <= 0xdfff) ||
           (c >= 0xfffe && c <= 0xffff);
  }

  /**
   * Remove all chars from the input that cannot be serialized as XML.
   *
   * @param s
   *        The source value. May be <code>null</code>.
   * @return <code>null</code> if the source value is <code>null</code>.
   * @see #isForbiddenParamValueChar(char)
   * @since 9.0.4
   */
  @Nullable
  public static String getWithoutForbiddenChars (@Nullable final String s)
  {
    if (s == null)
      return null;

    final StringBuilder aCleanValue = new StringBuilder (s.length ());
    int nForbidden = 0;

    for (final char c : s.toCharArray ())
      if (isForbiddenParamValueChar (c))
        nForbidden++;
      else
        aCleanValue.append (c);

    if (nForbidden == 0)
    {
      // Return "as-is"
      return s;
    }

    if (LOGGER.isWarnEnabled ())
      LOGGER.warn ("Removed " + nForbidden + " forbidden character(s) from a request parameter value!");

    return aCleanValue.toString ();
  }

  /**
   * First normalize the input according to Unicode rules, so that "O 0xcc 0x88"
   * (O with COMBINING DIAERESIS) becomes "Ö" (Capital O with umlaut).
   *
   * @param s
   *        Source string. May be <code>null</code>.
   * @return <code>null</code> if the source string is <code>null</code>.
   * @see Normalizer#normalize(CharSequence, java.text.Normalizer.Form)
   * @see #getWithoutForbiddenChars(String)
   * @since 9.0.6
   */
  @Nullable
  public static String getWithoutForbiddenCharsAndNormalized (@Nullable final String s)
  {
    String sValue = s;

    // Removed forbidden chars first
    if (sValue == null)
      return null;
    sValue = getWithoutForbiddenChars (sValue);

    // than normalize
    if (sValue == null)
      return null;
    sValue = Normalizer.normalize (sValue, Normalizer.Form.NFKC);

    return sValue;
  }

  public final void initScope ()
  {
    // Avoid double initialization of a scope, because for file uploads, the
    // parameters can only be extracted once!
    // As the parameters are stored directly in the HTTP request, we're not
    // loosing any data here!
    final IAttributeContainerAny <String> aAttrs = attrs ();
    if (aAttrs.getAndSetFlag (REQUEST_ATTR_SCOPE_INITED))
    {
      if (LOGGER.isWarnEnabled ())
        LOGGER.warn ("Scope was already inited: " + toString ());
      return;
    }

    final IRequestParamContainer aParams = params ();

    // where some extra items (like file items) handled?
    final boolean bAddedSpecialRequestParams = addSpecialRequestParams ().isChanged ();

    // Retrieve once (because locked)
    final IParamValueCleanser aParamValueCleanser = getParamValueCleanser ();

    // set parameters as attributes (handles GET and POST parameters)
    // This may throw an exception, if the payload is invalid
    Enumeration <String> aEnum;
    try
    {
      aEnum = m_aHttpRequest.getParameterNames ();
    }
    catch (final Exception ex)
    {
      aEnum = new EmptyEnumeration <> ();
    }
    while (aEnum.hasMoreElements ())
    {
      final String sParamName = aEnum.nextElement ();

      // Avoid double setting a parameter!
      // If an existing file item with this name is present, don't parse it as a
      // String again
      if (bAddedSpecialRequestParams && aParams.containsKey (sParamName))
        continue;

      // Check if it is a single value or not
      final String [] aParamValues = m_aHttpRequest.getParameterValues (sParamName);
      final int nParamValues = aParamValues.length;
      if (nParamValues == 1)
      {
        // Convert from String[] to String

        String sValue = aParamValues[0];
        if (aParamValueCleanser != null)
        {
          // Adopt value if needed
          sValue = aParamValueCleanser.getCleanedValue (sParamName, 0, sValue);
        }
        aParams.putIn (sParamName, sValue);
      }
      else
      {
        // Use String[] as is
        final String [] aPreProcessedValues = new String [nParamValues];
        for (int i = 0; i < nParamValues; ++i)
        {
          String sValue = aParamValues[i];
          if (aParamValueCleanser != null)
          {
            // Adopt value if needed
            sValue = aParamValueCleanser.getCleanedValue (sParamName, i, sValue);
          }
          aPreProcessedValues[i] = sValue;
        }

        aParams.putIn (sParamName, aPreProcessedValues);
      }
    }

    // done initialization
    if (ScopeHelper.isDebugRequestScopeLifeCycle (LOGGER))
      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Initialized request web scope '" + getID () + "' of class " + ClassHelper.getClassLocalName (this),
                     ScopeHelper.getDebugStackTrace ());
  }

  @Override
  protected void postDestroy ()
  {
    if (ScopeHelper.isDebugRequestScopeLifeCycle (LOGGER))
      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Destroyed request web scope '" + getID () + "' of class " + ClassHelper.getClassLocalName (this),
                     ScopeHelper.getDebugStackTrace ());
  }

  @Nonnull
  public final HttpHeaderMap headers ()
  {
    HttpHeaderMap ret = m_aHeaders;
    if (ret == null)
      ret = m_aHeaders = RequestHelper.getRequestHeaderMap (m_aHttpRequest);
    return ret;
  }

  @Nonnull
  @ReturnsMutableObject
  public final IRequestParamContainer params ()
  {
    return m_aParams;
  }

  @Nonnull
  public final IRequestParamMap getRequestParamMap ()
  {
    IRequestParamMap ret = m_aRequestParamMap;
    if (ret == null)
      ret = m_aRequestParamMap = RequestParamMap.create (params ());
    return ret;
  }

  /**
   * This is a heuristic method to determine whether a request is for a file
   * (e.g. x.jsp) or for a servlet. This method return <code>true</code> if the
   * last dot is after the last slash
   *
   * @param sServletPath
   *        The non-<code>null</code> servlet path to check
   * @return <code>true</code> if it is assumed that the request is file based,
   *         <code>false</code> if it can be assumed to be a regular servlet.
   */
  public static boolean isFileBasedRequest (@Nonnull final String sServletPath)
  {
    final int nLastDot = sServletPath.lastIndexOf ('.');
    if (nLastDot < 0)
      return false;
    final int nLastSlash = sServletPath.lastIndexOf ('/');
    if (nLastSlash < 0)
    {
      // for e.g. "abc.def"
      return true;
    }

    // true for e.g. "/path/paths/abc.def"
    // false for e.g. "/path/pa.th/def"
    return nLastDot > nLastSlash;
  }

  /**
   * @return Returns the portion of the request URI that indicates the context
   *         of the request. The context path always comes first in a request
   *         URI. The path starts with a "/" character but does not end with a
   *         "/" character. For servlets in the default (root) context, this
   *         method returns "". The container does not decode this string. E.g.
   *         <code>/context</code> or an empty string for the root context.
   *         Never with a trailing slash.
   * @see #getFullContextPath()
   */
  @Nonnull
  public String getContextPath ()
  {
    // Always use the context path from the global web scope because it can be
    // customized!
    return ServletContextPathHolder.getContextPath ();
  }

  @Nonnull
  public String getContextAndServletPath ()
  {
    final String sServletPath = getServletPath ();
    if (isFileBasedRequest (sServletPath))
      return getContextPath () + sServletPath;
    // For servlets that are not files, we need to append a trailing slash
    return getContextPath () + sServletPath + '/';
  }

  @Nonnull
  public String getFullContextAndServletPath ()
  {
    final String sServletPath = getServletPath ();
    if (isFileBasedRequest (sServletPath))
      return getFullContextPath () + sServletPath;
    // For servlets, we need to append a trailing slash
    return getFullContextPath () + sServletPath + '/';
  }

  @Nonnull
  public final HttpServletRequest getRequest ()
  {
    return m_aHttpRequest;
  }

  @Nonnull
  public final HttpServletResponse getResponse ()
  {
    return m_aHttpResponse;
  }

  @Nonnull
  public String encodeURL (@Nonnull final String sURL)
  {
    if (ServletSettings.isEncodeURLs ())
      return getResponse ().encodeURL (sURL);
    // Return "as-is"
    return sURL;
  }

  @Nonnull
  public String encodeRedirectURL (@Nonnull final String sURL)
  {
    if (ServletSettings.isEncodeURLs ())
      return getResponse ().encodeRedirectURL (sURL);
    // Return "as-is"
    return sURL;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    return super.equals (o);
  }

  @Override
  public int hashCode ()
  {
    return super.hashCode ();
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ())
                            .append ("HttpRequest", m_aHttpRequest)
                            .append ("HttpResponse", m_aHttpResponse)
                            .append ("Headers", m_aHeaders)
                            .append ("Params", m_aParams)
                            .append ("RequestParamMap", m_aRequestParamMap)
                            .getToString ();
  }
}

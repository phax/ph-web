/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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

import java.util.Enumeration;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.attr.AttributeContainerAny;
import com.helger.commons.collection.attr.IMutableAttributeContainerAny;
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
  private static final class ParamContainer extends AttributeContainerAny <String> implements IRequestParamContainer
  {}

  // Because of transient field
  private static final long serialVersionUID = 78563987233147L;

  private static final Logger s_aLogger = LoggerFactory.getLogger (RequestWebScope.class);
  private static final String REQUEST_ATTR_SCOPE_INITED = ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL +
                                                          "requestscope.inited";
  private static final String REQUEST_ATTR_REQUESTPARAMMAP = ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL +
                                                             "requestscope.requestparammap";

  protected final transient HttpServletRequest m_aHttpRequest;
  protected final transient HttpServletResponse m_aHttpResponse;
  private final ParamContainer m_aParams = new ParamContainer ();

  @Nonnull
  @Nonempty
  private static String _createScopeID (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    return GlobalIDFactory.getNewIntID () + "@" + RequestHelper.getRequestURI (aHttpRequest);
  }

  public RequestWebScope (@Nonnull final HttpServletRequest aHttpRequest,
                          @Nonnull final HttpServletResponse aHttpResponse)
  {
    super (_createScopeID (aHttpRequest));

    m_aHttpRequest = aHttpRequest;
    m_aHttpResponse = ValueEnforcer.notNull (aHttpResponse, "HttpResponse");

    // done initialization
    if (ScopeHelper.debugRequestScopeLifeCycle (s_aLogger))
      s_aLogger.info ("Created request web scope '" + getID () + "' of class " + ClassHelper.getClassLocalName (this),
                      ScopeHelper.getDebugStackTrace ());
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

  @OverrideOnDemand
  protected void postAttributeInit ()
  {}

  public final void initScope ()
  {
    // Avoid double initialization of a scope, because for file uploads, the
    // parameters can only be extracted once!
    // As the parameters are stored directly in the HTTP request, we're not
    // loosing any data here!
    final IMutableAttributeContainerAny <String> aAttrs = attrs ();
    if (aAttrs.getAndSetAttributeFlag (REQUEST_ATTR_SCOPE_INITED))
    {
      s_aLogger.warn ("Scope was already inited: " + toString ());
      return;
    }

    // where some extra items (like file items) handled?
    final IRequestParamContainer aParams = params ();
    final boolean bAddedSpecialRequestParams = addSpecialRequestParams ().isChanged ();

    // set parameters as attributes (handles GET and POST parameters)
    final Enumeration <?> aEnum = m_aHttpRequest.getParameterNames ();
    while (aEnum.hasMoreElements ())
    {
      final String sParamName = (String) aEnum.nextElement ();

      // Avoid double setting a parameter!
      if (bAddedSpecialRequestParams && aParams.containsKey (sParamName))
        continue;

      // Check if it is a single value or not
      final String [] aParamValues = m_aHttpRequest.getParameterValues (sParamName);
      if (aParamValues.length == 1)
      {
        // Convert from String[] to String
        aParams.putIn (sParamName, aParamValues[0]);
      }
      else
      {
        // Use String[] as is
        aParams.putIn (sParamName, aParamValues);
      }
    }

    postAttributeInit ();

    // done initialization
    if (ScopeHelper.debugRequestScopeLifeCycle (s_aLogger))
      s_aLogger.info ("Initialized request web scope '" +
                      getID () +
                      "' of class " +
                      ClassHelper.getClassLocalName (this),
                      ScopeHelper.getDebugStackTrace ());
  }

  @Override
  protected void postDestroy ()
  {
    if (ScopeHelper.debugRequestScopeLifeCycle (s_aLogger))
      s_aLogger.info ("Destroyed request web scope '" + getID () + "' of class " + ClassHelper.getClassLocalName (this),
                      ScopeHelper.getDebugStackTrace ());
  }

  @Nonnull
  @ReturnsMutableObject
  public final IRequestParamContainer params ()
  {
    return m_aParams;
  }

  @Nonnull
  public IRequestParamMap getRequestParamMap ()
  {
    // Check if a value is cached in the scope
    IRequestParamMap aRPM = attrs ().getCastedValue (REQUEST_ATTR_REQUESTPARAMMAP);
    if (aRPM == null)
    {
      // Request the map and put it in scope
      aRPM = RequestParamMap.create (params ());
      attrs ().putIn (REQUEST_ATTR_REQUESTPARAMMAP, aRPM);
    }
    return aRPM;
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
  public HttpServletRequest getRequest ()
  {
    return m_aHttpRequest;
  }

  @Nonnull
  public HttpServletResponse getResponse ()
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
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ())
                            .append ("HttpRequest", m_aHttpRequest)
                            .append ("HttpResponse", m_aHttpResponse)
                            .append ("Params", m_aParams)
                            .getToString ();
  }
}

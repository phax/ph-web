/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.collection.ext.ICommonsMap;
import com.helger.commons.id.factory.GlobalIDFactory;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.scope.AbstractMapBasedScope;
import com.helger.commons.scope.ScopeHelper;
import com.helger.commons.scope.mgr.ScopeManager;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.mgr.WebScopeManager;
import com.helger.web.scope.util.RequestHelper;
import com.helger.web.servlet.request.IRequestParamMap;
import com.helger.web.servlet.request.RequestParamMap;

/**
 * A request web scopes that does not parse multipart requests.
 *
 * @author Philip Helger
 */
public class RequestWebScopeNoMultipart extends AbstractMapBasedScope implements IRequestWebScope
{
  // Because of transient field
  private static final long serialVersionUID = 78563987233146L;

  private static final Logger s_aLogger = LoggerFactory.getLogger (RequestWebScopeNoMultipart.class);
  private static final String REQUEST_ATTR_SCOPE_INITED = ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL +
                                                          "requestscope.inited";
  private static final String REQUEST_ATTR_REQUESTPARAMMAP = ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL +
                                                             "requestscope.requestparammap";

  protected final transient HttpServletRequest m_aHttpRequest;
  protected final transient HttpServletResponse m_aHttpResponse;

  @Nonnull
  @Nonempty
  private static String _createScopeID (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    return GlobalIDFactory.getNewIntID () + "@" + RequestHelper.getRequestURI (aHttpRequest);
  }

  public RequestWebScopeNoMultipart (@Nonnull final HttpServletRequest aHttpRequest,
                                     @Nonnull final HttpServletResponse aHttpResponse)
  {
    super (_createScopeID (aHttpRequest));

    m_aHttpRequest = aHttpRequest;
    m_aHttpResponse = ValueEnforcer.notNull (aHttpResponse, "HttpResponse");

    // done initialization
    if (ScopeHelper.debugRequestScopeLifeCycle (s_aLogger))
      s_aLogger.info ("Created request web scope '" +
                      getID () +
                      "' of class " +
                      ClassHelper.getClassLocalName (this),
                      ScopeHelper.getDebugStackTrace ());
  }

  @OverrideOnDemand
  protected boolean addSpecialRequestAttributes ()
  {
    return false;
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
    if (getAndSetAttributeFlag (REQUEST_ATTR_SCOPE_INITED))
    {
      s_aLogger.warn ("Scope was already inited: " + toString ());
      return;
    }

    // where some extra items (like file items) handled?
    final boolean bAddedSpecialRequestAttrs = addSpecialRequestAttributes ();

    // set parameters as attributes (handles GET and POST parameters)
    final Enumeration <?> aEnum = m_aHttpRequest.getParameterNames ();
    while (aEnum.hasMoreElements ())
    {
      final String sParamName = (String) aEnum.nextElement ();

      // Avoid double setting a parameter!
      if (bAddedSpecialRequestAttrs && containsAttribute (sParamName))
        continue;

      // Check if it is a single value or not
      final String [] aParamValues = m_aHttpRequest.getParameterValues (sParamName);
      if (aParamValues.length == 1)
        setAttribute (sParamName, aParamValues[0]);
      else
        setAttribute (sParamName, aParamValues);
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
      s_aLogger.info ("Destroyed request web scope '" +
                      getID () +
                      "' of class " +
                      ClassHelper.getClassLocalName (this),
                      ScopeHelper.getDebugStackTrace ());
  }

  @Nullable
  public final String getSessionID (final boolean bCreateIfNotExisting)
  {
    final HttpSession aSession = getRequest ().getSession (bCreateIfNotExisting);
    return aSession == null ? null : aSession.getId ();
  }

  /**
   * Try to convert the passed value into a {@link ICommonsList} of
   * {@link String}. This method is only called, if the passed value is non-
   * <code>null</code>, if it is not an String array or a single String.
   *
   * @param sName
   *        The name of the parameter to be queried. Just for informational
   *        purposes.
   * @param aValue
   *        The retrieved non-<code>null</code> attribute value which is neither
   *        a String nor a String array.
   * @param aDefault
   *        The default value to be returned, in case no type conversion could
   *        be found.
   * @return The converted value or the default value.
   */
  @Nullable
  @OverrideOnDemand
  protected ICommonsList <String> getAttributeAsListCustom (@Nullable final String sName,
                                                            @Nonnull final Object aValue,
                                                            @Nullable final ICommonsList <String> aDefault)
  {
    return aDefault;
  }

  @Nullable
  public ICommonsList <String> getAttributeAsList (@Nullable final String sName,
                                                   @Nullable final ICommonsList <String> aDefault)
  {
    final Object aValue = getAttributeObject (sName);
    if (aValue instanceof String [])
    {
      // multiple values passed in the request
      return new CommonsArrayList <> ((String []) aValue);
    }
    if (aValue instanceof String)
    {
      // single value passed in the request
      return new CommonsArrayList <> ((String) aValue);
    }
    return getAttributeAsListCustom (sName, aValue, aDefault);
  }

  @Nonnull
  public IRequestParamMap getRequestParamMap ()
  {
    // Check if a value is cached in the scope
    IRequestParamMap aValue = getCastedAttribute (REQUEST_ATTR_REQUESTPARAMMAP);
    if (aValue == null)
    {
      // Use all attributes except the internal ones
      final ICommonsMap <String, Object> aAttrs = getAllAttributes ();
      // Remove all special internal attributes
      aAttrs.remove (REQUEST_ATTR_SCOPE_INITED);

      // Request the map and put it in scope
      aValue = RequestParamMap.create (aAttrs);
      setAttribute (REQUEST_ATTR_REQUESTPARAMMAP, aValue);
    }
    return aValue;
  }

  /**
   * This is a heuristic method to determine whether a request is for a file
   * (e.g. x.jsp) or for a servlet. It is assumed that regular servlets don't
   * have a '.' in their name!
   *
   * @param sServletPath
   *        The non-<code>null</code> servlet path to check
   * @return <code>true</code> if it is assumed that the request is file based,
   *         <code>false</code> if it can be assumed to be a regular servlet.
   */
  public static boolean isFileBasedRequest (@Nonnull final String sServletPath)
  {
    return sServletPath.indexOf ('.') >= 0;
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
    return WebScopeManager.getGlobalScope ().getContextPath ();
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
    return getResponse ().encodeURL (sURL);
  }

  @Nonnull
  public String encodeRedirectURL (@Nonnull final String sURL)
  {
    return getResponse ().encodeRedirectURL (sURL);
  }

  @Nonnull
  public OutputStream getOutputStream () throws IOException
  {
    return getResponse ().getOutputStream ();
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ())
                            .append ("httpRequest", m_aHttpRequest)
                            .append ("httpResponse", m_aHttpResponse)
                            .toString ();
  }
}

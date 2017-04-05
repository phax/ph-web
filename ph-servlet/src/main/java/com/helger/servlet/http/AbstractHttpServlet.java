package com.helger.servlet.http;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.EnumSet;

import javax.annotation.Nonnull;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ext.CommonsEnumMap;
import com.helger.commons.collection.ext.ICommonsMap;
import com.helger.commons.string.StringHelper;
import com.helger.http.CHTTPHeader;
import com.helger.http.EHTTPMethod;
import com.helger.http.EHTTPVersion;
import com.helger.servlet.ServletHelper;

/**
 * Abstract HTTP based servlet. Compared to the default
 * {@link javax.servlet.http.HttpServlet} this class uses a handler map with
 * {@link EHTTPMethod} as the key.
 *
 * @author Various
 */
public abstract class AbstractHttpServlet extends GenericServlet
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (AbstractHttpServlet.class);
  private final ICommonsMap <EHTTPMethod, IHttpServletHandler> m_aHandlers = new CommonsEnumMap <> (EHTTPMethod.class);

  /**
   * Does nothing, because this is an abstract class.
   */
  public AbstractHttpServlet ()
  {
    setHandler (EHTTPMethod.TRACE, new HttpTraceHandler ());
  }

  protected final void setHandler (@Nonnull final EHTTPMethod eHTTPMethod, @Nonnull final IHttpServletHandler aHandler)
  {
    ValueEnforcer.notNull (eHTTPMethod, "HTTPMethod");
    ValueEnforcer.notNull (aHandler, "Handler");
    m_aHandlers.put (eHTTPMethod, aHandler);
  }

  @Nonnull
  @ReturnsMutableCopy
  private EnumSet <EHTTPMethod> _getAllowedHTTPMethods ()
  {
    // Return all methods for which handlers are registered
    final EnumSet <EHTTPMethod> ret = EnumSet.copyOf (m_aHandlers.keySet ());
    if (ret.contains (EHTTPMethod.GET))
    {
      // If GET is supported, HEAD is also supported
      ret.add (EHTTPMethod.HEAD);
    }
    // OPTIONS is always supported
    ret.add (EHTTPMethod.OPTIONS);
    return ret;
  }

  @Nonnull
  private String _getAllowString ()
  {
    return StringHelper.getImplodedMapped (", ", _getAllowedHTTPMethods (), EHTTPMethod::getName);
  }

  private void _internalService (@Nonnull final HttpServletRequest aHttpRequest,
                                 @Nonnull final HttpServletResponse aHttpResponse,
                                 @Nonnull final EHTTPVersion eHTTPVersion,
                                 @Nonnull final EHTTPMethod eHTTPMethod) throws ServletException, IOException
  {
    final IHttpServletHandler aHandler = m_aHandlers.get (eHTTPMethod);
    if (aHandler != null)
    {
      // Invoke handler
      aHandler.handle (aHttpRequest, aHttpResponse, eHTTPVersion, eHTTPMethod);
    }
    else
      if (eHTTPMethod == EHTTPMethod.HEAD)
      {
        // Default HEAD handler
        final CountingOnlyHttpServletResponse aResponseWrapper = new CountingOnlyHttpServletResponse (aHttpResponse);
        _internalService (aHttpRequest, aResponseWrapper, eHTTPVersion, EHTTPMethod.GET);
        aResponseWrapper.setContentLengthAutomatically ();
      }
      else
        if (eHTTPMethod == EHTTPMethod.OPTIONS)
        {
          // Default OPTIONS handler

          // Build Allow response header - that's it
          aHttpResponse.setHeader (CHTTPHeader.ALLOW, _getAllowString ());
        }
        else
        {
          // Unsupported method
          aHttpResponse.setHeader (CHTTPHeader.ALLOW, _getAllowString ());
          if (eHTTPVersion == EHTTPVersion.HTTP_11)
            aHttpResponse.sendError (HttpServletResponse.SC_METHOD_NOT_ALLOWED);
          else
            aHttpResponse.sendError (HttpServletResponse.SC_BAD_REQUEST);
        }
  }

  /**
   * Dispatches client requests to the protected <code>service</code> method.
   * There's no need to override this method.
   *
   * @param req
   *        the {@link HttpServletRequest} object that contains the request the
   *        client made of the servlet
   * @param res
   *        the {@link HttpServletResponse} object that contains the response
   *        the servlet returns to the client
   * @exception IOException
   *            if an input or output error occurs while the servlet is handling
   *            the HTTP request
   * @exception ServletException
   *            if the HTTP request cannot be handled
   * @see javax.servlet.Servlet#service
   */
  @Override
  public final void service (@Nonnull final ServletRequest req,
                             @Nonnull final ServletResponse res) throws ServletException, IOException
  {
    ValueEnforcer.isInstanceOf (req, HttpServletRequest.class, "Non-HTTP servlet request");
    ValueEnforcer.isInstanceOf (req, HttpServletResponse.class, "Non-HTTP servlet response");

    final HttpServletRequest aHttpRequest = (HttpServletRequest) req;
    final HttpServletResponse aHttpResponse = (HttpServletResponse) res;

    // Determine HTTP version
    final String sProtocol = aHttpRequest.getProtocol ();
    final EHTTPVersion eHTTPVersion = EHTTPVersion.getFromNameOrNull (sProtocol);
    if (eHTTPVersion == null)
    {
      // HTTP version disallowed
      s_aLogger.error ("Request " +
                       ServletHelper.getRequestRequestURI (aHttpRequest) +
                       " has unsupported HTTP version (" +
                       sProtocol +
                       ")!");
      aHttpResponse.sendError (HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED);
    }
    else
    {
      // Determine HTTP method
      final String sMethod = aHttpRequest.getMethod ();
      final EHTTPMethod eHTTPMethod = EHTTPMethod.getFromNameOrNull (sMethod);
      if (eHTTPMethod == null)
      {
        // HTTP method unknown
        s_aLogger.error ("Request " +
                         ServletHelper.getRequestRequestURI (aHttpRequest) +
                         " has unsupported HTTP method (" +
                         sMethod +
                         ")!");
        aHttpResponse.sendError (HttpServletResponse.SC_NOT_IMPLEMENTED);
      }
      else
      {
        // Determine handler
        _internalService (aHttpRequest, aHttpResponse, eHTTPVersion, eHTTPMethod);
      }
    }
  }
}

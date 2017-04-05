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
package com.helger.web.servlets.scope;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.request.RequestScopeInitializer;
import com.helger.xml.serialize.write.XMLWriterSettings;

/**
 * A thin wrapper around an {@link HttpServlet} that encapsulates the correct
 * Scope handling before and after a request.<br>
 * It overrides all the protected "do*" methods from {@link HttpServlet} and
 * replaced them with protected "on*" methods that can be overridden. The "do*"
 * methods are final to avoid overriding the without the usage of scopes. The
 * default operations of the "on*" methods is to call the original "do*"
 * functionality from the parent class.
 *
 * @author Philip Helger
 */
public abstract class AbstractScopeAwareHttpServlet extends HttpServlet
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (AbstractScopeAwareHttpServlet.class);

  // Determine in "init" method
  private transient String m_sStatusApplicationID;

  /**
   * Default constructor
   */
  protected AbstractScopeAwareHttpServlet ()
  {}

  /**
   * @return The application ID for this servlet.
   */
  @OverrideOnDemand
  protected String getApplicationID ()
  {
    return ClassHelper.getClassLocalName (getClass ());
  }

  /**
   * Add custom init code in overridden implementations of this method.
   *
   * @throws ServletException
   *         to conform to the outer specifications
   */
  @OverrideOnDemand
  protected void onInit () throws ServletException
  {
    /* empty */
  }

  @Override
  public final void init () throws ServletException
  {
    super.init ();
    m_sStatusApplicationID = getApplicationID ();
    if (StringHelper.hasNoText (m_sStatusApplicationID))
      throw new InitializationException ("Failed retrieve a valid application ID! Please override getApplicationID()");
    onInit ();
  }

  /**
   * Add custom destruction code in overridden implementations of this method.
   */
  @OverrideOnDemand
  protected void onDestroy ()
  {
    /* empty */
  }

  @Override
  public final void destroy ()
  {
    onDestroy ();
  }

  /*
   * This method is required to ensure that the HTTP response is correctly
   * encoded. Normally this is done via the charset filter, but if a
   * non-existing URL is accessed then the error redirect happens without the
   * charset filter ever called.
   */
  private static void _ensureResponseCharset (@Nonnull final HttpServletResponse aHttpResponse)
  {
    if (aHttpResponse.getCharacterEncoding () == null)
    {
      s_aLogger.info ("Setting response charset to " + XMLWriterSettings.DEFAULT_XML_CHARSET);
      aHttpResponse.setCharacterEncoding (XMLWriterSettings.DEFAULT_XML_CHARSET);
    }
  }

  /**
   * Called before every request, independent of the method
   *
   * @param aHttpRequest
   *        The HTTP servlet request
   * @param aHttpResponse
   *        The HTTP servlet response
   * @return the created request scope
   */
  @OverrideOnDemand
  @OverridingMethodsMustInvokeSuper
  protected RequestScopeInitializer beforeRequest (@Nonnull final HttpServletRequest aHttpRequest,
                                                   @Nonnull final HttpServletResponse aHttpResponse)
  {
    final RequestScopeInitializer aRequestScopeInitializer = RequestScopeInitializer.create (m_sStatusApplicationID,
                                                                                             aHttpRequest,
                                                                                             aHttpResponse);
    _ensureResponseCharset (aHttpResponse);
    return aRequestScopeInitializer;
  }

  /**
   * Dummy interface matching the on... protected methods so I can pass them as
   * method references.
   *
   * @author Philip Helger
   * @since 8.7.5
   */
  private static interface IRunner
  {
    void run (@Nonnull final HttpServletRequest aHttpRequest,
              @Nonnull final HttpServletResponse aHttpResponse,
              @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException;
  }

  /**
   * Run a servlet request synchronously.
   *
   * @param aHttpRequest
   *        HTTP request
   * @param aHttpResponse
   *        HTTP response
   * @param aRunner
   *        Method reference to the protected "on..." method that does the main
   *        work
   * @throws ServletException
   *         On error
   * @throws IOException
   *         On error
   * @since 8.7.5
   */
  private void _runScoped (@Nonnull final HttpServletRequest aHttpRequest,
                           @Nonnull final HttpServletResponse aHttpResponse,
                           @Nonnull final IRunner aRunner) throws ServletException, IOException
  {
    final RequestScopeInitializer aRequestScopeInitializer = beforeRequest (aHttpRequest, aHttpResponse);
    try
    {
      aRunner.run (aHttpRequest, aHttpResponse, aRequestScopeInitializer.getRequestScope ());
    }
    finally
    {
      aRequestScopeInitializer.destroyScope ();
    }
  }

  /**
   * Implement HTTP DELETE
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onDelete (@Nonnull final HttpServletRequest aHttpRequest,
                           @Nonnull final HttpServletResponse aHttpResponse,
                           @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doDelete (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doDelete (@Nonnull final HttpServletRequest aHttpRequest,
                                 @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _runScoped (aHttpRequest, aHttpResponse, this::onDelete);
  }

  /**
   * Implement HTTP GET
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onGet (@Nonnull final HttpServletRequest aHttpRequest,
                        @Nonnull final HttpServletResponse aHttpResponse,
                        @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doGet (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doGet (@Nonnull final HttpServletRequest aHttpRequest,
                              @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _runScoped (aHttpRequest, aHttpResponse, this::onGet);
  }

  /**
   * Implement HTTP HEAD
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onHead (@Nonnull final HttpServletRequest aHttpRequest,
                         @Nonnull final HttpServletResponse aHttpResponse,
                         @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doHead (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doHead (@Nonnull final HttpServletRequest aHttpRequest,
                               @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _runScoped (aHttpRequest, aHttpResponse, this::onHead);
  }

  /**
   * Implement HTTP OPTIONS
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onOptions (@Nonnull final HttpServletRequest aHttpRequest,
                            @Nonnull final HttpServletResponse aHttpResponse,
                            @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doOptions (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doOptions (@Nonnull final HttpServletRequest aHttpRequest,
                                  @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _runScoped (aHttpRequest, aHttpResponse, this::onOptions);
  }

  /**
   * Implement HTTP POST
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onPost (@Nonnull final HttpServletRequest aHttpRequest,
                         @Nonnull final HttpServletResponse aHttpResponse,
                         @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doPost (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doPost (@Nonnull final HttpServletRequest aHttpRequest,
                               @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _runScoped (aHttpRequest, aHttpResponse, this::onPost);
  }

  /**
   * Implement HTTP PUT
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onPut (@Nonnull final HttpServletRequest aHttpRequest,
                        @Nonnull final HttpServletResponse aHttpResponse,
                        @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doPut (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doPut (@Nonnull final HttpServletRequest aHttpRequest,
                              @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _runScoped (aHttpRequest, aHttpResponse, this::onPut);
  }

  /**
   * Implement HTTP TRACE
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onTrace (@Nonnull final HttpServletRequest aHttpRequest,
                          @Nonnull final HttpServletResponse aHttpResponse,
                          @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doTrace (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doTrace (@Nonnull final HttpServletRequest aHttpRequest,
                                @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _runScoped (aHttpRequest, aHttpResponse, this::onTrace);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ApplicationID", m_sStatusApplicationID).getToString ();
  }
}

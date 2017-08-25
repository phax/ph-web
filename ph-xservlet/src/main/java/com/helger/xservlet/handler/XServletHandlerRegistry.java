/**
 * Copyright (C) 2016-2017 Philip Helger (www.helger.com)
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
package com.helger.xservlet.handler;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsEnumMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.functional.IThrowingConsumer;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.servlet.async.ServletAsyncSpec;
import com.helger.xservlet.handler.simple.IXServletSimpleHandler;
import com.helger.xservlet.handler.simple.XServletHandlerToSimpleHandler;

/**
 * Wrapper around a map from {@link EHttpMethod} to {@link IXServletHandler}.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
@NotThreadSafe
public class XServletHandlerRegistry implements Serializable
{
  /** The main handler map */
  private final ICommonsMap <EHttpMethod, IXServletHandler> m_aHandler = new CommonsEnumMap <> (EHttpMethod.class);

  public XServletHandlerRegistry ()
  {}

  /**
   * Register a handler for the provided HTTP method. If another handler is
   * already registered, the new registration overwrites the old one.
   *
   * @param eMethod
   *        The HTTP method to register for. May not be <code>null</code>.
   * @param aLowLevelHandler
   *        The handler to register. May not be <code>null</code>.
   * @param bAllowOverwrite
   *        if <code>true</code> existing handler can be overwritten, if
   *        <code>false</code> this method will throw an
   *        {@link IllegalStateException}.
   */
  public void registerHandler (@Nonnull final EHttpMethod eMethod,
                               @Nonnull final IXServletHandler aLowLevelHandler,
                               final boolean bAllowOverwrite)
  {
    ValueEnforcer.notNull (eMethod, "HTTPMethod");
    ValueEnforcer.notNull (aLowLevelHandler, "Handler");

    if (!bAllowOverwrite && m_aHandler.containsKey (eMethod))
    {
      // Programming error
      throw new IllegalStateException ("An HTTP handler for HTTP method " +
                                       eMethod +
                                       " is already registered: " +
                                       m_aHandler.get (eMethod));
    }
    m_aHandler.put (eMethod, aLowLevelHandler);
  }

  /**
   * Register a handler for the provided HTTP method. If another handler is
   * already registered, the new registration overwrites the old one.
   *
   * @param eMethod
   *        The HTTP method to register for. May not be <code>null</code>.
   * @param aLowLevelHandler
   *        The handler to register. May not be <code>null</code>.
   */
  public void registerHandler (@Nonnull final EHttpMethod eMethod, @Nonnull final IXServletHandler aLowLevelHandler)
  {
    registerHandler (eMethod, aLowLevelHandler, false);
  }

  public void registerHandler (@Nonnull final EHttpMethod eMethod, @Nonnull final IXServletSimpleHandler aSimpleHandler)
  {
    registerHandler (eMethod, ServletAsyncSpec.getSync (), aSimpleHandler);
  }

  public void registerHandler (@Nonnull final EHttpMethod eMethod,
                               @Nonnull final ServletAsyncSpec aAsyncSpec,
                               @Nonnull final IXServletSimpleHandler aSimpleHandler)
  {
    // Always invoke the simple handler
    IXServletHandler aRealHandler = new XServletHandlerToSimpleHandler (aSimpleHandler);

    // Add the async handler only in front if necessary
    if (aAsyncSpec.isAsynchronous ())
      aRealHandler = new XServletAsyncHandler (aAsyncSpec, aRealHandler);

    // Register as a regular handler
    registerHandler (eMethod, aRealHandler);
  }

  public void copyHandler (@Nonnull final EHttpMethod eFromMethod, @Nonnull @Nonempty final EHttpMethod... aToMethods)
  {
    ValueEnforcer.notNull (eFromMethod, "FromMethod");
    ValueEnforcer.notEmptyNoNullValue (aToMethods, "ToMethods");

    final IXServletHandler aFromHandler = getHandler (eFromMethod);
    if (aFromHandler != null)
      for (final EHttpMethod eToMethod : aToMethods)
        registerHandler (eToMethod, aFromHandler);
  }

  @Nonnull
  @ReturnsMutableCopy
  public EnumSet <EHttpMethod> getAllowedHTTPMethods ()
  {
    // Return all methods for which handlers are registered
    final EnumSet <EHttpMethod> ret = EnumSet.copyOf (m_aHandler.keySet ());
    if (!ret.contains (EHttpMethod.GET))
    {
      // If GET is not supported, HEAD is also not supported
      ret.remove (EHttpMethod.HEAD);
    }
    return ret;
  }

  @Nonnull
  public String getAllowedHttpMethodsString ()
  {
    return StringHelper.getImplodedMapped (", ", getAllowedHTTPMethods (), EHttpMethod::getName);
  }

  @Nullable
  public IXServletHandler getHandler (@Nonnull final EHttpMethod eHttpMethod)
  {
    return m_aHandler.get (eHttpMethod);
  }

  public void forEachHandler (@Nonnull final Consumer <? super IXServletHandler> aConsumer)
  {
    m_aHandler.forEachValue (aConsumer);
  }

  public <EXTYPE extends Throwable> void forEachHandlerThrowing (@Nonnull final IThrowingConsumer <? super IXServletHandler, EXTYPE> aConsumer) throws EXTYPE
  {
    for (final IXServletHandler aHandler : m_aHandler.values ())
      aConsumer.accept (aHandler);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Handler", m_aHandler).getToString ();
  }
}
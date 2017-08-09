package com.helger.xservlet.handler;

import java.io.Serializable;
import java.util.EnumSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsEnumMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.http.EHttpMethod;

/**
 * Wrapper around a map from {@link EHttpMethod} to
 * {@link IXServletLowLevelHandler}.
 *
 * @author Philip Helger
 */
public class XServletHandlerRegistry implements Serializable
{
  /** The main handler map */
  private final ICommonsMap <EHttpMethod, IXServletLowLevelHandler> m_aHandler = new CommonsEnumMap <> (EHttpMethod.class);

  public XServletHandlerRegistry ()
  {}

  /**
   * Register a handler for the provided HTTP method. If another handler is
   * already registered, the new registration overwrites the old one.
   *
   * @param eHTTPMethod
   *        The HTTP method to register for. May not be <code>null</code>.
   * @param aHandler
   *        The handler to register. May not be <code>null</code>.
   */
  public void registerHandler (@Nonnull final EHttpMethod eHTTPMethod, @Nonnull final IXServletLowLevelHandler aHandler)
  {
    ValueEnforcer.notNull (eHTTPMethod, "HTTPMethod");
    ValueEnforcer.notNull (aHandler, "Handler");

    if (m_aHandler.containsKey (eHTTPMethod))
      throw new IllegalStateException ("An HTTP handler for HTTP method " + eHTTPMethod + " is already registered!");
    m_aHandler.put (eHTTPMethod, aHandler);
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
  public IXServletLowLevelHandler getHandler (@Nonnull final EHttpMethod eHttpMethod)
  {
    return m_aHandler.get (eHttpMethod);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Handler", m_aHandler).getToString ();
  }
}

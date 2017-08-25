package com.helger.xservlet;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.lang.ICloneable;
import com.helger.http.EHttpReferrerPolicy;

/**
 * This class keeps all the settings that can be applied to all XServlet based
 * settings. The settings need to be applied per Servlet instance!<br>
 * The following things can be set here:
 * <ul>
 * <li>HTTP Referrer Policy - see {@link EHttpReferrerPolicy}</li>
 * </ul>
 *
 * @author Philip Helger
 */
public class XServletSettings implements Serializable, ICloneable <XServletSettings>
{
  // Maximum compatibility
  private EHttpReferrerPolicy m_eHttpReferrerPolicy = EHttpReferrerPolicy.NO_REFERRER;

  public XServletSettings ()
  {}

  public XServletSettings (@Nonnull final XServletSettings aOther)
  {
    ValueEnforcer.notNull (aOther, "Other");
    m_eHttpReferrerPolicy = aOther.m_eHttpReferrerPolicy;
  }

  public EHttpReferrerPolicy getHttpReferrerPolicy ()
  {
    return m_eHttpReferrerPolicy;
  }

  public void setHttpReferrerPolicy (@Nullable final EHttpReferrerPolicy eHttpReferrerPolicy)
  {
    m_eHttpReferrerPolicy = eHttpReferrerPolicy;
  }

  public boolean hasHttpReferrerPolicy ()
  {
    return m_eHttpReferrerPolicy != null;
  }

  @Nonnull
  @ReturnsMutableCopy
  public XServletSettings getClone ()
  {
    return new XServletSettings (this);
  }
}

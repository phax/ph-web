package com.helger.web.servlet.response;

/**
 * Defines the different redirect modes to be used in {@link UnifiedResponse}.
 *
 * @author Philip Helger
 */
public enum ERedirectMode
{
  /** Use the default httpServletResponse.sendRedirect */
  DEFAULT,
  /** Use HTTP 303/302 depending on the HTTP version used */
  POST_REDIRECT_GET;
}

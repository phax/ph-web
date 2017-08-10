package com.helger.xservlet.filter;

import javax.annotation.Nonnull;

import com.helger.commons.statistics.IMutableStatisticsHandlerKeyedTimer;
import com.helger.commons.statistics.StatisticsManager;
import com.helger.commons.timing.StopWatch;
import com.helger.web.scope.IRequestWebScope;
import com.helger.xservlet.AbstractXServlet;

/**
 * A special filter that performs timing of servlet execution. Each servlet
 * request requires it's own instance of this class!
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public final class XServletFilterTimer implements IXServletHighLevelFilter
{
  private final IMutableStatisticsHandlerKeyedTimer m_aTimer;
  private StopWatch m_aSW;

  public XServletFilterTimer (@Nonnull final AbstractXServlet aServlet)
  {
    m_aTimer = StatisticsManager.getKeyedTimerHandler (aServlet.getClass ().getName ());
  }

  public void beforeRequest (@Nonnull final IRequestWebScope aRequestScope)
  {
    m_aSW = StopWatch.createdStarted ();
  }

  public void afterRequest (@Nonnull final IRequestWebScope aRequestScope)
  {
    // Timer per HTTP method
    m_aTimer.addTime (aRequestScope.getHttpMethod ().getName (), m_aSW.stopAndGetMillis ());
  }
}

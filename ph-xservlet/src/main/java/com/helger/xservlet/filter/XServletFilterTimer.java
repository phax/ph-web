package com.helger.xservlet.filter;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;

import com.helger.commons.statistics.IMutableStatisticsHandlerKeyedTimer;
import com.helger.commons.statistics.StatisticsManager;
import com.helger.commons.timing.StopWatch;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xservlet.AbstractXServlet;

public final class XServletFilterTimer implements IXServletHighLevelFilter
{
  private final IMutableStatisticsHandlerKeyedTimer m_aTimer;
  private StopWatch m_aSW;

  public XServletFilterTimer (@Nonnull final AbstractXServlet aServlet)
  {
    m_aTimer = StatisticsManager.getKeyedTimerHandler (aServlet.getClass ().getName ());
  }

  public void beforeRequest (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                             @Nonnull final UnifiedResponse aUnifiedResponse) throws ServletException, IOException
  {
    m_aSW = StopWatch.createdStarted ();
  }

  public void afterRequest (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                            @Nonnull final UnifiedResponse aUnifiedResponse) throws ServletException, IOException
  {
    // Timer per HTTP method
    m_aTimer.addTime (aRequestScope.getHttpMethod ().getName (), m_aSW.stopAndGetMillis ());
  }
}
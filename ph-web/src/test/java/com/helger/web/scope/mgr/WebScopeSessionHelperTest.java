/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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
package com.helger.web.scope.mgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.scope.IScopeRenewalAware;
import com.helger.scope.mgr.ScopeSessionManager;
import com.helger.servlet.mock.MockHttpServletRequest;
import com.helger.web.scope.ISessionWebScope;
import com.helger.web.scope.mock.AbstractWebScopeAwareTestCase;

/**
 * Test class for class {@link WebScopeSessionHelper}.
 *
 * @author Philip Helger
 */
public final class WebScopeSessionHelperTest extends AbstractWebScopeAwareTestCase
{
  public static final class MockScopeRenewalAware implements IScopeRenewalAware
  {
    private final String m_sStr;

    public MockScopeRenewalAware (@Nullable final String s)
    {
      m_sStr = s;
    }

    @Nullable
    public String getString ()
    {
      return m_sStr;
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger (WebScopeSessionHelperTest.class);

  @Test
  public void testRenewSessionScopeEmpty ()
  {
    assertTrue (WebScopeSessionHelper.renewCurrentSessionScope (false).isUnchanged ());
    assertTrue (WebScopeSessionHelper.renewCurrentSessionScope (true).isUnchanged ());
  }

  @Test
  public void testRenewSessionScopeDefault ()
  {
    ISessionWebScope aWS = WebScopeManager.getSessionScope (true);
    aWS.attrs ().putIn ("a1", new MockScopeRenewalAware ("session1"));
    aWS.attrs ().putIn ("a2", new MockScopeRenewalAware ("session2"));
    aWS.attrs ().putIn ("a21", "session21");
    assertEquals (3, aWS.attrs ().size ());
    assertNotNull (aWS.getCreationDateTime ());

    // Main renew session (no session invalidation)
    final String sOldSessionID = aWS.getID ();
    assertTrue (WebScopeSessionHelper.renewCurrentSessionScope (false).isChanged ());

    // Check session scope (same underlying http session)
    aWS = WebScopeManager.getSessionScope (false);
    assertNotNull (aWS);
    assertEquals (aWS.getID (), sOldSessionID);
    assertEquals (2, aWS.attrs ().size ());

    // Main renew session (with session invalidation)
    assertTrue (WebScopeSessionHelper.renewCurrentSessionScope (true).isChanged ());

    // Check session scope (new underlying http session)
    aWS = WebScopeManager.getSessionScope (false);
    assertNotNull (aWS);
    assertFalse (aWS.getID ().equals (sOldSessionID));
    assertEquals (2, aWS.attrs ().size ());
  }

  @Test
  public void testMultipleSessions () throws InterruptedException
  {
    final int nMax = 10;
    final Thread [] aThreads = new Thread [nMax];
    final CountDownLatch aCDLStart = new CountDownLatch (nMax);
    final CountDownLatch aCDLGlobalChecks = new CountDownLatch (1);
    final CountDownLatch aCDLDone = new CountDownLatch (nMax);
    for (int i = 0; i < nMax; ++i)
    {
      final String sSessionID = "Session " + i;
      aThreads[i] = new Thread ("Mock " + i)
      {
        @Override
        public void run ()
        {
          try
          {
            // Create and setup the request
            final MockHttpServletRequest aRequest = new MockHttpServletRequest (getServletContext ());
            aRequest.setSessionID (sSessionID);

            // Create the session
            final HttpSession aHttpSession = aRequest.getSession (true);
            if (aHttpSession == null)
              throw new IllegalStateException ();
            final ISessionWebScope aSessionScope = WebScopeManager.getSessionScope (true);
            if (aSessionScope == null)
              throw new IllegalStateException ();
            if (aHttpSession != aSessionScope.getSession ())
              throw new IllegalStateException ();
            aSessionScope.attrs ().putIn ("x", new MockScopeRenewalAware ("bla"));
            aSessionScope.attrs ().putIn ("y", "bla");
            if (aSessionScope.attrs ().size () != 2)
              throw new IllegalStateException ();

            // Wait until all sessions are created
            aCDLStart.countDown ();

            // Wait until global checks are performed
            aCDLGlobalChecks.await ();

            // Renew the session scope
            final ISessionWebScope aNewSessionScope = WebScopeSessionHelper.renewSessionScope (aHttpSession);
            if (aNewSessionScope == null)
              throw new IllegalStateException ();
            if (aNewSessionScope == aSessionScope)
              throw new IllegalStateException ();
            if (aNewSessionScope.attrs ().size () != 1)
              throw new IllegalStateException ();
            if (!aNewSessionScope.attrs ().containsKey ("x"))
              throw new IllegalStateException ();
            if (!(aNewSessionScope.attrs ().get ("x") instanceof MockScopeRenewalAware))
              throw new IllegalStateException ();

            aRequest.invalidate ();
          }
          catch (final RuntimeException ex)
          {
            throw ex;
          }
          catch (final Exception ex)
          {
            throw new IllegalStateException (ex);
          }
          finally
          {
            aCDLDone.countDown ();
          }
        }
      };
    }
    for (int i = 0; i < nMax; ++i)
      aThreads[i].start ();

    // Wait until all sessions are retrieved
    aCDLStart.await ();
    assertEquals (nMax, ScopeSessionManager.getInstance ().getSessionCount ());
    aCDLGlobalChecks.countDown ();
    aCDLDone.await ();

    // Still all sessions present
    assertEquals (nMax, ScopeSessionManager.getInstance ().getSessionCount ());
    ScopeSessionManager.getInstance ().destroyAllSessions ();
    assertEquals (0, ScopeSessionManager.getInstance ().getSessionCount ());

    // End all requests
    for (int i = 0; i < nMax; ++i)
      aThreads[i].join ();

    LOGGER.info ("Test Done");
  }
}

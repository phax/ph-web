/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.webscopes.mgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

import org.junit.Test;

import com.helger.commons.scopes.IScopeRenewalAware;
import com.helger.commons.scopes.mgr.ScopeSessionManager;
import com.helger.web.mock.MockHttpServletRequest;
import com.helger.webscopes.domain.ISessionApplicationWebScope;
import com.helger.webscopes.domain.ISessionWebScope;
import com.helger.webscopes.mgr.WebScopeManager;
import com.helger.webscopes.mgr.WebScopeSessionHelper;
import com.helger.webscopes.mock.AbstractWebScopeAwareTestCase;

/**
 * Test class for class {@link WebScopeSessionHelper}.
 * 
 * @author Philip Helger
 */
public final class WebScopeSessionHelperTest extends AbstractWebScopeAwareTestCase
{
  public static final class MockScopeRenewalAware implements IScopeRenewalAware, Serializable
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
    aWS.setAttribute ("a1", new MockScopeRenewalAware ("session1"));
    aWS.setAttribute ("a2", new MockScopeRenewalAware ("session2"));
    aWS.setAttribute ("a21", "session21");
    assertEquals (3, aWS.getAllAttributes ().size ());

    // Contains renewal and non-renewal aware attrs
    ISessionApplicationWebScope aAWS1 = aWS.getSessionApplicationScope ("app1", true);
    aAWS1.setAttribute ("a3", new MockScopeRenewalAware ("session3"));
    aAWS1.setAttribute ("a4", new MockScopeRenewalAware ("session4"));
    aAWS1.setAttribute ("a41", "session41");
    assertEquals (3, aAWS1.getAllAttributes ().size ());

    // Contains only renewal aware attrs
    ISessionApplicationWebScope aAWS2 = aWS.getSessionApplicationScope ("app2", true);
    aAWS2.setAttribute ("a5", new MockScopeRenewalAware ("session5"));
    aAWS2.setAttribute ("a6", new MockScopeRenewalAware ("session6"));
    assertEquals (2, aAWS2.getAllAttributes ().size ());

    // Contains only non-renewal aware attrs
    ISessionApplicationWebScope aAWS3 = aWS.getSessionApplicationScope ("app3", true);
    aAWS3.setAttribute ("a7", "session7");
    aAWS3.setAttribute ("a8", "session8");
    assertEquals (2, aAWS3.getAllAttributes ().size ());

    assertEquals (3, aWS.getAllSessionApplicationScopes ().size ());

    // Main renew session (no session invalidation)
    final String sOldSessionID = aWS.getID ();
    assertTrue (WebScopeSessionHelper.renewCurrentSessionScope (false).isChanged ());

    // Check session scope (same underlying http session)
    aWS = WebScopeManager.getSessionScope (false);
    assertNotNull (aWS);
    assertEquals (aWS.getID (), sOldSessionID);
    assertEquals (2, aWS.getAllAttributes ().size ());

    // Main renew session (with session invalidation)
    assertTrue (WebScopeSessionHelper.renewCurrentSessionScope (true).isChanged ());

    // Check session scope (new underlying http session)
    aWS = WebScopeManager.getSessionScope (false);
    assertNotNull (aWS);
    assertFalse (aWS.getID ().equals (sOldSessionID));
    assertEquals (2, aWS.getAllAttributes ().size ());

    // Only 2 session application scopes had scope renewal aware attrs
    assertEquals (2, aWS.getAllSessionApplicationScopes ().size ());

    aAWS1 = aWS.getSessionApplicationScope ("app1", false);
    assertNotNull (aAWS1);
    assertEquals (2, aAWS1.getAllAttributes ().size ());

    aAWS2 = aWS.getSessionApplicationScope ("app2", false);
    assertNotNull (aAWS2);
    assertEquals (2, aAWS2.getAllAttributes ().size ());
    assertNotNull (aAWS2.getAttributeObject ("a5"));
    assertEquals ("session6", ((MockScopeRenewalAware) aAWS2.getAttributeObject ("a6")).getString ());

    // Had no scope renewal aware attrs:
    aAWS3 = aWS.getSessionApplicationScope ("app3", false);
    assertNull (aAWS3);
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
            assertNotNull (aHttpSession);
            final ISessionWebScope aSessionScope = WebScopeManager.getSessionScope (true);
            assertNotNull (aSessionScope);
            assertSame (aHttpSession, aSessionScope.getSession ());
            aSessionScope.setAttribute ("x", new MockScopeRenewalAware ("bla"));
            aSessionScope.setAttribute ("y", "bla");
            assertEquals (2, aSessionScope.getAttributeCount ());
            aSessionScope.getSessionApplicationScope ("app", true).setAttribute ("x", "y");
            assertEquals (1, aSessionScope.getSessionApplicationScopeCount ());

            // Wait until all sessions are created
            aCDLStart.countDown ();

            // Wait until global checks are performed
            aCDLGlobalChecks.await ();

            // Renew the session scope
            final ISessionWebScope aNewSessionScope = WebScopeSessionHelper.renewSessionScope (aHttpSession);
            assertNotNull (aNewSessionScope);
            assertTrue (aNewSessionScope != aSessionScope);
            assertEquals (1, aNewSessionScope.getAttributeCount ());
            assertTrue (aNewSessionScope.containsAttribute ("x"));
            assertTrue (aNewSessionScope.getAttributeObject ("x") instanceof MockScopeRenewalAware);
            assertEquals (0, aSessionScope.getSessionApplicationScopeCount ());

            aRequest.invalidate ();

          }
          catch (final Exception ex)
          {
            throw new RuntimeException (ex);
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

    m_aLogger.info ("Test Done");
  }
}

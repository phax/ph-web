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
package com.helger.network.port;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.timing.StopWatch;

/**
 * Test class for class {@link NetworkOnlineStatusDeterminator}.
 *
 * @author Philip Helger
 */
public final class NetworkOnlineStatusDeterminatorTest
{
  @Test
  public void testBasic ()
  {
    // Reset default values
    NetworkOnlineStatusDeterminator.setCacheDuration (NetworkOnlineStatusDeterminator.DEFAULT_CACHE_DURATION);
    NetworkOnlineStatusDeterminator.setConnectionTimeoutMilliseconds (NetworkOnlineStatusDeterminator.DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS);
    NetworkOnlineStatusDeterminator.resetCachedStatus ();
    StopWatch aSW = StopWatch.createdStarted ();
    final ENetworkOnlineStatus eStatus = NetworkOnlineStatusDeterminator.getNetworkStatus ();
    final long nTime1 = aSW.stopAndGetMillis ();
    assertNotNull (eStatus);
    assertTrue (eStatus.isDefined ());
    assertTrue (nTime1 < NetworkOnlineStatusDeterminator.DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS + 500);

    aSW = StopWatch.createdStarted ();
    final ENetworkOnlineStatus eStatus2 = NetworkOnlineStatusDeterminator.getNetworkStatus ();
    final long nTime2 = aSW.stopAndGetMillis ();
    assertNotNull (eStatus2);
    assertTrue (eStatus2.isDefined ());
    assertSame (eStatus, eStatus2);
    assertTrue (nTime2 < nTime1);
  }
}

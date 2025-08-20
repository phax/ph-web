/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.useragent;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.GuardedBy;
import com.helger.annotation.concurrent.ThreadSafe;
import com.helger.annotation.style.PresentForCodeCoverage;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.concurrent.SimpleReadWriteLock;
import com.helger.base.string.StringHelper;
import com.helger.collection.commons.CommonsHashSet;
import com.helger.collection.commons.ICommonsSet;
import com.helger.http.CHttpHeader;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Central cache for known user agents (see HTTP header field {@link CHttpHeader#USER_AGENT}).
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class UserAgentDatabase
{
  private static final Logger LOGGER = LoggerFactory.getLogger (UserAgentDatabase.class);

  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  @GuardedBy ("RW_LOCK")
  private static final ICommonsSet <String> UNIQUE_USER_AGENTS = new CommonsHashSet <> ();
  @GuardedBy ("RW_LOCK")
  private static Consumer <? super IUserAgent> s_aNewUserAgentCallback;

  @PresentForCodeCoverage
  private static final UserAgentDatabase INSTANCE = new UserAgentDatabase ();

  private UserAgentDatabase ()
  {}

  /**
   * Set an external callback to get notified when a new unique UserAgent was received.
   *
   * @param aCallback
   *        Callback to set. May be <code>null</code>. The parameters to this callback are always
   *        non-null.
   */
  public static void setUserAgentCallback (@Nullable final Consumer <? super IUserAgent> aCallback)
  {
    RW_LOCK.writeLocked ( () -> s_aNewUserAgentCallback = aCallback);
  }

  @Nullable
  public static IUserAgent getParsedUserAgent (@Nullable final String sUserAgent)
  {
    if (StringHelper.isEmpty (sUserAgent))
      return null;

    // Decrypt outside the lock
    final IUserAgent aUserAgent = UserAgentDecryptor.decryptUserAgentString (sUserAgent);

    final boolean bAdded = RW_LOCK.writeLockedBoolean ( () -> UNIQUE_USER_AGENTS.add (sUserAgent));
    if (bAdded)
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Found new UserAgent '" + sUserAgent + "'");

      RW_LOCK.readLocked ( () -> {
        if (s_aNewUserAgentCallback != null)
          s_aNewUserAgentCallback.accept (aUserAgent);
      });
    }
    return aUserAgent;
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsSet <String> getAllUniqueUserAgents ()
  {
    return RW_LOCK.readLockedGet (UNIQUE_USER_AGENTS::getClone);
  }
}

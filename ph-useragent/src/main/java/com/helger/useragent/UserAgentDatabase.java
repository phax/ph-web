/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsHashSet;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.string.StringHelper;

/**
 * Central cache for known user agents (see HTTP header field
 * {@link CHttpHeader#USER_AGENT}).
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class UserAgentDatabase
{
  private static final Logger LOGGER = LoggerFactory.getLogger (UserAgentDatabase.class);

  private static final SimpleReadWriteLock s_aRWLock = new SimpleReadWriteLock ();
  @GuardedBy ("s_aRWLock")
  private static final ICommonsSet <String> s_aUniqueUserAgents = new CommonsHashSet <> ();
  @GuardedBy ("s_aRWLock")
  private static Consumer <? super IUserAgent> s_aNewUserAgentCallback;

  @PresentForCodeCoverage
  private static final UserAgentDatabase s_aInstance = new UserAgentDatabase ();

  private UserAgentDatabase ()
  {}

  /**
   * Set an external callback to get notified when a new unique UserAgent was
   * received.
   *
   * @param aCallback
   *        Callback to set. May be <code>null</code>. The parameters to this
   *        callback are always non-null.
   */
  public static void setUserAgentCallback (@Nullable final Consumer <? super IUserAgent> aCallback)
  {
    s_aRWLock.writeLocked ( () -> s_aNewUserAgentCallback = aCallback);
  }

  @Nullable
  public static IUserAgent getParsedUserAgent (@Nullable final String sUserAgent)
  {
    if (StringHelper.hasNoText (sUserAgent))
      return null;

    // Decrypt outside the lock
    final IUserAgent aUserAgent = UserAgentDecryptor.decryptUserAgentString (sUserAgent);

    final boolean bAdded = s_aRWLock.writeLocked ( () -> s_aUniqueUserAgents.add (sUserAgent));
    if (bAdded)
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Found new UserAgent '" + sUserAgent + "'");

      s_aRWLock.readLocked ( () -> {
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
    return s_aRWLock.readLocked (s_aUniqueUserAgents::getClone);
  }
}

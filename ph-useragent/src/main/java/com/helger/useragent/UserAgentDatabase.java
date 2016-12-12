/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
import com.helger.commons.collection.ext.CommonsHashSet;
import com.helger.commons.collection.ext.ICommonsSet;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.string.StringHelper;
import com.helger.http.CHTTPHeader;

/**
 * Central cache for known user agents (see HTTP header field
 * {@link CHTTPHeader#USER_AGENT}).
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class UserAgentDatabase
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (UserAgentDatabase.class);

  private static final SimpleReadWriteLock s_aRWLock = new SimpleReadWriteLock ();
  @GuardedBy ("s_aRWLock")
  private static final ICommonsSet <String> s_aUniqueUserAgents = new CommonsHashSet<> ();
  @GuardedBy ("s_aRWLock")
  private static Consumer <IUserAgent> s_aNewUserAgentCallback;

  @PresentForCodeCoverage
  private static final UserAgentDatabase s_aInstance = new UserAgentDatabase ();

  private UserAgentDatabase ()
  {}

  public static void setUserAgentCallback (@Nullable final Consumer <IUserAgent> aCallback)
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
      if (s_aLogger.isDebugEnabled ())
        s_aLogger.debug ("Found new UserAgent '" + sUserAgent + "'");

      if (s_aNewUserAgentCallback != null)
        s_aNewUserAgentCallback.accept (aUserAgent);
    }
    return aUserAgent;
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsSet <String> getAllUniqueUserAgents ()
  {
    return s_aRWLock.readLocked ( () -> s_aUniqueUserAgents.getClone ());
  }
}

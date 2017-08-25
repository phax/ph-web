/**
 * Copyright (C) 2016-2017 Philip Helger (www.helger.com)
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
package com.helger.xservlet.forcedredirect;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.scope.singleton.AbstractSessionWebSingleton;

/**
 * Stores per-session the data to be used in a Post-Redirect-Get scenario.<br>
 * This class usually takes 'IHCNode' but because of the reverse dependencies,
 * this type cannot be used and is therefore abstracted as 'Serializable' in
 * this implementation. So all the time you read 'Serializable' consider it to
 * be 'IHCNode'.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
@ThreadSafe
public final class ForcedRedirectManager extends AbstractSessionWebSingleton
{
  public static final String REQUEST_PARAMETER_PRG_ACTIVE = "phprga";

  private static final Logger s_aLogger = LoggerFactory.getLogger (ForcedRedirectManager.class);

  @GuardedBy ("m_aRWLock")
  private final ICommonsMap <String, Serializable> m_aMap = new CommonsHashMap <> ();

  @Deprecated
  @UsedViaReflection
  public ForcedRedirectManager ()
  {}

  /**
   * @return The instance to use. Never <code>null</code>.
   */
  @Nonnull
  public static ForcedRedirectManager getInstance ()
  {
    return getSessionSingleton (ForcedRedirectManager.class);
  }

  /**
   * @return The instance to use if already instantiated. Maybe
   *         <code>null</code>.
   */
  @Nullable
  public static ForcedRedirectManager getInstanceIfInstantiated ()
  {
    return getSessionSingletonIfInstantiated (ForcedRedirectManager.class);
  }

  public void createForcedRedirect (@Nonnull final ForcedRedirectException ex)
  {
    ValueEnforcer.notNull (ex, "Exception");
    m_aRWLock.writeLocked ( () -> m_aMap.put (ex.getSourceMenuItemID (), ex.getContent ()));

    if (GlobalDebug.isDebugMode ())
      s_aLogger.info ("Creating forced redirect from '" +
                      ex.getSourceMenuItemID () +
                      "' to URL " +
                      ex.getRedirectTargetURL ().getAsStringWithEncodedParameters ());
  }

  @Nullable
  public Serializable getContent (@Nullable final String sMenuItemID)
  {
    if (StringHelper.hasNoText (sMenuItemID))
      return null;

    // Get only
    return m_aRWLock.readLocked ( () -> m_aMap.get (sMenuItemID));
  }

  public boolean hasContent (@Nullable final String sMenuItemID)
  {
    if (StringHelper.hasNoText (sMenuItemID))
      return false;

    // Get only
    return m_aRWLock.readLocked ( () -> m_aMap.get (sMenuItemID) != null);
  }

  @Nullable
  public static Serializable getLastForcedRedirectContent (@Nullable final String sMenuItemID)
  {
    if (StringHelper.hasNoText (sMenuItemID))
      return null;

    final ForcedRedirectManager aMgr = getInstanceIfInstantiated ();
    if (aMgr == null)
      return null;

    // Get only
    return aMgr.getContent (sMenuItemID);
  }

  @Nullable
  public Serializable getAndRemoveContent (@Nullable final String sMenuItemID)
  {
    if (StringHelper.hasNoText (sMenuItemID))
      return null;

    // Get in write lock
    final Serializable ret = m_aRWLock.writeLocked ( () -> m_aMap.remove (sMenuItemID));
    if (ret != null)
      if (s_aLogger.isDebugEnabled ())
        s_aLogger.debug ("Removed content of last forced redirect from '" + sMenuItemID + "'");
    return ret;
  }

  @Nullable
  public static Serializable getAndRemoveLastForcedRedirectContent (@Nullable final String sMenuItemID)
  {
    if (StringHelper.hasNoText (sMenuItemID))
      return null;

    final ForcedRedirectManager aMgr = getInstanceIfInstantiated ();
    if (aMgr == null)
      return null;

    // test in read-lock
    if (aMgr.hasContent (sMenuItemID))
    {
      // get and remove in write lock
      return aMgr.getAndRemoveContent (sMenuItemID);
    }

    // No content
    return null;
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ()).append ("map", m_aMap).getToString ();
  }
}

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
package com.helger.smtp.failed;

import java.io.Serializable;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ELockType;
import com.helger.commons.annotation.MustBeLocked;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.CommonsLinkedHashMap;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.collection.ext.ICommonsOrderedMap;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.statistics.IMutableStatisticsHandlerCounter;
import com.helger.commons.statistics.StatisticsManager;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;

/**
 * This is a singleton object that keeps all the mails that could not be send.
 *
 * @author Philip Helger
 */
@ThreadSafe
public class FailedMailQueue implements Serializable
{
  private static final IMutableStatisticsHandlerCounter s_aStatsCountAdd = StatisticsManager.getCounterHandler (FailedMailQueue.class.getName () +
                                                                                                                "$add");
  private static final IMutableStatisticsHandlerCounter s_aStatsCountRemove = StatisticsManager.getCounterHandler (FailedMailQueue.class.getName () +
                                                                                                                   "$remove");

  protected final SimpleReadWriteLock m_aRWLock = new SimpleReadWriteLock ();
  @GuardedBy ("m_aRWLock")
  private final ICommonsOrderedMap <String, FailedMailData> m_aMap = new CommonsLinkedHashMap<> ();

  public FailedMailQueue ()
  {}

  @MustBeLocked (ELockType.WRITE)
  protected void internalAdd (@Nonnull final FailedMailData aFailedMailData)
  {
    m_aMap.put (aFailedMailData.getID (), aFailedMailData);
    s_aStatsCountAdd.increment ();
  }

  public void add (@Nonnull final FailedMailData aFailedMailData)
  {
    ValueEnforcer.notNull (aFailedMailData, "FailedMailData");

    m_aRWLock.writeLocked ( () -> internalAdd (aFailedMailData));
  }

  @Nullable
  @MustBeLocked (ELockType.WRITE)
  protected FailedMailData internalRemove (@Nullable final String sID)
  {
    final FailedMailData ret = m_aMap.remove (sID);
    if (ret != null)
      s_aStatsCountRemove.increment ();
    return ret;
  }

  /**
   * Remove the failed mail at the given index.
   *
   * @param sID
   *        The ID of the failed mail data to be removed.
   * @return <code>null</code> if no such data exists
   */
  @Nullable
  public FailedMailData remove (@Nullable final String sID)
  {
    if (StringHelper.hasNoText (sID))
      return null;

    return m_aRWLock.writeLocked ( () -> internalRemove (sID));
  }

  @Nonnegative
  @MustBeLocked (ELockType.READ)
  protected int internalSize ()
  {
    return m_aMap.size ();
  }

  @Nonnegative
  public int size ()
  {
    return m_aRWLock.readLocked ( () -> internalSize ());
  }

  @Nullable
  @MustBeLocked (ELockType.READ)
  protected FailedMailData internalGetFailedMailOfID (@Nullable final String sID)
  {
    return m_aMap.get (sID);
  }

  @Nullable
  public FailedMailData getFailedMailOfID (@Nullable final String sID)
  {
    return m_aRWLock.readLocked ( () -> internalGetFailedMailOfID (sID));
  }

  @Nonnegative
  @MustBeLocked (ELockType.READ)
  protected int internalGetFailedMailCount ()
  {
    return m_aMap.size ();
  }

  @Nonnegative
  public int getFailedMailCount ()
  {
    return m_aRWLock.readLocked ( () -> internalGetFailedMailCount ());
  }

  @Nonnull
  @ReturnsMutableCopy
  @MustBeLocked (ELockType.READ)
  protected ICommonsList <FailedMailData> internalGetAllFailedMails ()
  {
    return m_aMap.copyOfValues ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <FailedMailData> getAllFailedMails ()
  {
    return m_aRWLock.readLocked ( () -> internalGetAllFailedMails ());
  }

  @Nonnull
  @ReturnsMutableCopy
  @MustBeLocked (ELockType.WRITE)
  protected ICommonsList <FailedMailData> internalRemoveAll ()
  {
    final ICommonsList <FailedMailData> aTempList = new CommonsArrayList<> (m_aMap.size ());
    if (!m_aMap.isEmpty ())
    {
      aTempList.addAll (m_aMap.values ());
      m_aMap.clear ();
    }
    return aTempList;
  }

  /**
   * Remove and return all failed mails.
   *
   * @return All currently available failed mails. Never <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <FailedMailData> removeAll ()
  {
    return m_aRWLock.writeLocked ( () -> internalRemoveAll ());
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("map", m_aMap).toString ();
  }
}

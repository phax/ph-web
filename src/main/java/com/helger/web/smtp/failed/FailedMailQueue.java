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
package com.helger.web.smtp.failed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.MustBeLocked;
import com.helger.commons.annotations.MustBeLocked.ELockType;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.stats.IStatisticsHandlerCounter;
import com.helger.commons.stats.StatisticsManager;
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
  private static final IStatisticsHandlerCounter s_aStatsCountAdd = StatisticsManager.getCounterHandler (FailedMailQueue.class.getName () +
                                                                                                         "$add");
  private static final IStatisticsHandlerCounter s_aStatsCountRemove = StatisticsManager.getCounterHandler (FailedMailQueue.class.getName () +
                                                                                                            "$remove");

  protected final ReadWriteLock m_aRWLock = new ReentrantReadWriteLock ();
  @GuardedBy ("m_aRWLock")
  private final Map <String, FailedMailData> m_aMap = new LinkedHashMap <String, FailedMailData> ();

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

    m_aRWLock.writeLock ().lock ();
    try
    {
      internalAdd (aFailedMailData);
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
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

    m_aRWLock.writeLock ().lock ();
    try
    {
      return internalRemove (sID);
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
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
    m_aRWLock.readLock ().lock ();
    try
    {
      return internalSize ();
    }
    finally
    {
      m_aRWLock.readLock ().unlock ();
    }
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
    m_aRWLock.readLock ().lock ();
    try
    {
      return internalGetFailedMailOfID (sID);
    }
    finally
    {
      m_aRWLock.readLock ().unlock ();
    }
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
    m_aRWLock.readLock ().lock ();
    try
    {
      return internalGetFailedMailCount ();
    }
    finally
    {
      m_aRWLock.readLock ().unlock ();
    }
  }

  @Nonnull
  @ReturnsMutableCopy
  @MustBeLocked (ELockType.READ)
  protected List <FailedMailData> internalGetAllFailedMails ()
  {
    return ContainerHelper.newList (m_aMap.values ());
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <FailedMailData> getAllFailedMails ()
  {
    m_aRWLock.readLock ().lock ();
    try
    {
      return internalGetAllFailedMails ();
    }
    finally
    {
      m_aRWLock.readLock ().unlock ();
    }
  }

  @Nonnull
  @ReturnsMutableCopy
  @MustBeLocked (ELockType.WRITE)
  protected List <FailedMailData> internalRemoveAll ()
  {
    final List <FailedMailData> aTempList = new ArrayList <FailedMailData> (m_aMap.size ());
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
  public List <FailedMailData> removeAll ()
  {
    m_aRWLock.writeLock ().lock ();
    try
    {
      return internalRemoveAll ();
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("map", m_aMap).toString ();
  }
}

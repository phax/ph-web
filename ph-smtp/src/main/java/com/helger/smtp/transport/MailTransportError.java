/**
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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
package com.helger.smtp.transport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.string.ToStringGenerator;

/**
 * This class encapsulates the error that occurs for a single mail sending. It
 * is a {@link Throwable} and an optional list of {@link MailSendDetails}.
 *
 * @author Philip Helger
 */
@Immutable
public class MailTransportError
{
  private final Throwable m_aThrowable;
  private final ICommonsList <MailSendDetails> m_aDetails = new CommonsArrayList <> ();

  public MailTransportError (@Nonnull final Throwable aThrowable)
  {
    this (aThrowable, null);
  }

  public MailTransportError (@Nonnull final Throwable aThrowable,
                             @Nullable final Iterable <? extends MailSendDetails> aDetails)
  {
    m_aThrowable = ValueEnforcer.notNull (aThrowable, "Throwable");
    m_aDetails.addAll (aDetails);
  }

  @Nonnull
  public Throwable getThrowable ()
  {
    return m_aThrowable;
  }

  @Nonnull
  public ICommonsList <MailSendDetails> getAllDetails ()
  {
    return m_aDetails.getClone ();
  }

  public boolean hasAnyDetails ()
  {
    return m_aDetails.isNotEmpty ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Throwable", m_aThrowable)
                                       .append ("Details", m_aDetails)
                                       .getToString ();
  }
}

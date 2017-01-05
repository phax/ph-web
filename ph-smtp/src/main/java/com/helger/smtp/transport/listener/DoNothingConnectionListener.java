/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.smtp.transport.listener;

import javax.annotation.Nonnull;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;

import com.helger.commons.string.ToStringGenerator;

/**
 * An implementation of {@link ConnectionListener} that does nothing.
 *
 * @author Philip Helger
 */
public class DoNothingConnectionListener implements ConnectionListener
{
  public void opened (@Nonnull final ConnectionEvent aEvent)
  {}

  public void disconnected (@Nonnull final ConnectionEvent aEvent)
  {}

  public void closed (@Nonnull final ConnectionEvent aEvent)
  {}

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).toString ();
  }
}

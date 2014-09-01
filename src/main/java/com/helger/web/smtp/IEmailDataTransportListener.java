/**
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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
package com.helger.web.smtp;

import javax.annotation.Nonnull;
import javax.mail.event.TransportEvent;

/**
 * An interface similar to javax.mail.event.TransportListener but specific
 * relations to out internal object types {@link ISMTPSettings} and
 * {@link IEmailData}.
 * 
 * @author Philip Helger
 */
public interface IEmailDataTransportListener
{
  /**
   * Invoked when a Message is successfully delivered.
   * 
   * @param aSMTPSettings
   *        The SMTP settings used for this message. Never <code>null</code>.
   * @param aEmailData
   *        The data that was sent. Never <code>null</code>.
   * @param aEvent
   *        TransportEvent. Never <code>null</code>.
   */
  void messageDelivered (@Nonnull ISMTPSettings aSMTPSettings,
                         @Nonnull IEmailData aEmailData,
                         @Nonnull TransportEvent aEvent);

  /**
   * Invoked when a Message is not delivered.
   * 
   * @param aSMTPSettings
   *        The SMTP settings used for this message. Never <code>null</code>.
   * @param aEmailData
   *        The data that was not sent. Never <code>null</code>.
   * @param aEvent
   *        TransportEvent. Never <code>null</code>.
   * @see TransportEvent
   */
  void messageNotDelivered (@Nonnull ISMTPSettings aSMTPSettings,
                            @Nonnull IEmailData aEmailData,
                            @Nonnull TransportEvent aEvent);

  /**
   * Invoked when a Message is partially delivered.
   * 
   * @param aSMTPSettings
   *        The SMTP settings used for this message. Never <code>null</code>.
   * @param aEmailData
   *        The data that was partially sent. Never <code>null</code>.
   * @param aEvent
   *        TransportEvent. Never <code>null</code>.
   * @see TransportEvent
   */
  void messagePartiallyDelivered (@Nonnull ISMTPSettings aSMTPSettings,
                                  @Nonnull IEmailData aEmailData,
                                  @Nonnull TransportEvent aEvent);
}

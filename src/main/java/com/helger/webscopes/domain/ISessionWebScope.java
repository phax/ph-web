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
package com.helger.webscopes.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

import com.helger.commons.annotations.Nonempty;
import com.helger.commons.scopes.domain.ISessionScope;
import com.helger.webscopes.IWebScope;

/**
 * Interface for a single session scope object.
 * 
 * @author Philip Helger
 */
public interface ISessionWebScope extends ISessionScope, IWebScope
{
  /**
   * Get the underlying HTTP session. Important: do not use it to access the
   * attributes within the session. Use only the scope API for this, so that the
   * synchronization is consistent!
   * 
   * @return The underlying HTTP session. Never <code>null</code>.
   */
  @Nonnull
  HttpSession getSession ();

  /**
   * Returns the time when the underlying HTTP session was created, measured in
   * milliseconds since midnight January 1, 1970 GMT.
   * 
   * @return a <code>long</code> specifying when this session was created,
   *         expressed in milliseconds since 1/1/1970 GMT
   * @exception IllegalStateException
   *            if this method is called on an invalidated session
   */
  long getCreationTime ();

  /**
   * Returns the last time the client sent a request associated with the
   * underlying HTTP session, as the number of milliseconds since midnight
   * January 1, 1970 GMT, and marked by the time the container received the
   * request.
   * <p>
   * Actions that your application takes, such as getting or setting a value
   * associated with the session, do not affect the access time.
   * 
   * @return a <code>long</code> representing the last time the client sent a
   *         request associated with this session, expressed in milliseconds
   *         since 1/1/1970 GMT
   * @exception IllegalStateException
   *            if this method is called on an invalidated session
   */
  long getLastAccessedTime ();

  /**
   * Returns the maximum time interval, in seconds, that the servlet container
   * will keep the underlying HTTP session open between client accesses. After
   * this interval, the servlet container will invalidate the session. A
   * negative time indicates the session should never timeout.
   * 
   * @return an integer specifying the number of seconds this session remains
   *         open between client requests
   */
  long getMaxInactiveInterval ();

  /**
   * Returns <code>true</code> if the client does not yet know about the
   * underlying HTTP session or if the client chooses not to join the underlying
   * HTTP session. For example, if the server used only cookie-based sessions,
   * and the client had disabled the use of cookies, then a session would be new
   * on each request.
   * 
   * @return <code>true</code> if the server has created a new HTTP session, but
   *         the client has not yet joined
   * @exception IllegalStateException
   *            if this method is called on an already invalidated session
   */
  boolean isNew ();

  /**
   * {@inheritDoc}
   */
  @Nullable
  ISessionApplicationWebScope getSessionApplicationScope (@Nonnull @Nonempty String sApplicationID,
                                                          boolean bCreateIfNotExisting);
}

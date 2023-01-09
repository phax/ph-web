/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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
package com.helger.network.port;

/**
 * Status of a remote port for the port checker.
 *
 * @author Philip Helger
 * @since 9.0.5
 */
public enum ENetworkPortStatus
{
  /** Port is open */
  PORT_IS_OPEN,
  PORT_IS_CLOSED,
  HOST_NOT_EXISTING,
  CONNECTION_TIMEOUT,
  GENERIC_IO_ERROR;

  public boolean isPortOpen ()
  {
    return this == PORT_IS_OPEN;
  }
}

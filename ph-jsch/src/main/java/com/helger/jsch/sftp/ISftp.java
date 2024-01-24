/*
 * Copyright (C) 2016-2024 Philip Helger (www.helger.com)
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
package com.helger.jsch.sftp;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;

/**
 * A simple callback interface for working with <i>managed</i> sftp channels.
 */
@FunctionalInterface
public interface ISftp
{
  void run (@Nonnull ChannelSftp aSftp) throws JSchException, IOException;
}

/*
 * Copyright (C) 2016-2026 Philip Helger (www.helger.com)
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

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.state.ESuccess;
import com.helger.base.string.StringHelper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * Helper class to perform certain activities with {@link ChannelSftp}
 *
 * @author Philip Helger
 * @since 11.2.4
 */
@Immutable
public final class ChannelSftpHelper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ChannelSftpHelper.class);

  private ChannelSftpHelper ()
  {}

  /**
   * Check if a remote directory exists.
   *
   * @param aChannel
   *        Channel to use. May not be <code>null</code>.
   * @param sDirName
   *        Directory to check for existence. May not be <code>null</code>.
   * @return <code>true</code> if it exists, <code>false</code> if not.
   */
  public static boolean dirExists (@NonNull final ChannelSftp aChannel, @NonNull final String sDirName)
  {
    try
    {
      final SftpATTRS aAttrs = aChannel.stat (sDirName);
      if (aAttrs == null)
        return false;

      if ((aAttrs.getFlags () & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS) == 0)
      {
        LOGGER.warn ("Unexpected directory flag: " + aAttrs.getFlags () + " - persmissions not present?");
        return false;
      }

      return aAttrs.isDir ();
    }
    catch (final SftpException ex)
    {
      // E.g. com.jcraft.jsch.SftpException: The requested file does not exist
      if (false)
        LOGGER.warn ("Failed to check if dir '" + sDirName + "' exists", ex);
      return false;
    }
  }

  /**
   * Create a remote directory. If the directory is absolute it will be used as such, whereas if the
   * directory is relative, it is applied relative to the current working directory.
   *
   * @param aChannel
   *        The channel to use. May not be <code>null</code>.
   * @param sDirName
   *        The directory to create. When starting with "/" it is considered to be an absolute
   *        directory, otherwise a relative directory.
   * @return {@link ESuccess}
   */
  @NonNull
  public static ESuccess mkdir (@NonNull final ChannelSftp aChannel, @NonNull final String sDirName)
  {
    try
    {
      final String [] aDirs = StringHelper.getExplodedArray ('/', sDirName);

      // Special handling for first part
      if (!StringHelper.startsWith (sDirName, '/'))
      {
        String sPwd = aChannel.pwd ();

        // Avoid leading slash
        if (StringHelper.startsWith (sPwd, '/'))
          sPwd = sPwd.substring (1);

        if (StringHelper.isNotEmpty (sPwd))
        {
          // Append only if something is present
          if (!StringHelper.endsWith (sPwd, '/'))
            sPwd += '/';
          LOGGER.info ("Prefixing dir with '" + sPwd + "'");
          aDirs[0] = sPwd + aDirs[0];
        }
      }

      // Piece by piece
      for (int i = 1; i < aDirs.length; i++)
        aDirs[i] = aDirs[i - 1] + '/' + aDirs[i];

      for (final String sDir : aDirs)
        if (StringHelper.isNotEmpty (sDir) && !dirExists (aChannel, sDir))
        {
          LOGGER.info ("Trying to create SFTP directory '" + sDir + "'");
          aChannel.mkdir (sDir);
        }

      return ESuccess.SUCCESS;
    }
    catch (final SftpException ex)
    {
      // Folder can not be found!
      LOGGER.warn ("Failed to mkdir '" + sDirName + "'", ex);
      return ESuccess.FAILURE;
    }
  }
}

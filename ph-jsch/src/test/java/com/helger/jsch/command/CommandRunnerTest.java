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
package com.helger.jsch.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.jsch.JSchTestHelper;
import com.helger.jsch.command.CommandRunner.ExecuteResult;
import com.helger.jsch.proxy.SshProxyTest;
import com.helger.jsch.session.ISessionFactory;

public final class CommandRunnerTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SshProxyTest.class);
  private static final String EXPECTED = "there is absolutely no chance this is gonna work!";

  private static ISessionFactory s_aSessionFactory;

  @BeforeClass
  public static void initializeClass ()
  {
    s_aSessionFactory = JSchTestHelper.createSessionFactoryFromConfig ();
  }

  @Test
  public void testCommandRunner ()
  {
    try (CommandRunner commandRunner = new CommandRunner (s_aSessionFactory))
    {
      LOGGER.info ("run a command");
      ExecuteResult result = commandRunner.execute ("echo " + EXPECTED);
      assertEquals (0, result.getExitCode ());
      assertEquals (EXPECTED + "\n", result.getStdout ());
      assertEquals ("", result.getStderr ());

      // test automatic reconnect...
      commandRunner.close ();

      LOGGER.info ("now try a second command");
      result = commandRunner.execute ("echo second " + EXPECTED);
      assertEquals (0, result.getExitCode ());
      assertEquals ("second " + EXPECTED + "\n", result.getStdout ());
      assertEquals ("", result.getStderr ());

      LOGGER.info ("and a third command");
      result = commandRunner.execute ("echo third " + EXPECTED);
      assertEquals (0, result.getExitCode ());
      assertEquals ("third " + EXPECTED + "\n", result.getStdout ());
      assertEquals ("", result.getStderr ());

      LOGGER.info ("wow, they all worked");
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed for command runner", e);
      LOGGER.info ("failed:", e);
      fail (e.getMessage ());
    }
  }

  @Test
  public void testSlowCommand ()
  {
    try (final CommandRunner commandRunner = new CommandRunner (s_aSessionFactory))
    {
      LOGGER.info ("run a command");
      final ExecuteResult result = commandRunner.execute ("sleep 3;echo " + EXPECTED);
      assertEquals (0, result.getExitCode ());
      assertEquals (EXPECTED + "\n", result.getStdout ());
      assertEquals ("", result.getStderr ());

      // test automatic reconnect...
      commandRunner.close ();

      LOGGER.info ("cool, even slow commands work");
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed for command runner", e);
      LOGGER.info ("failed:", e);
      fail (e.getMessage ());
    }
  }

  @Test
  public void testDetectOs ()
  {
    try (final CommandRunner commandRunner = new CommandRunner (s_aSessionFactory))
    {
      LOGGER.info ("run a command");
      ExecuteResult result = commandRunner.execute ("ver");
      if (result.getExitCode () == 0)
      {
        LOGGER.info ("likely windows: " + result.getStdout ());
      }
      else
      {
        result = commandRunner.execute ("uname -a");
        if (result.getExitCode () == 0)
        {
          LOGGER.info ("likely unix: " + result.getStdout ());
        }
        else
        {
          LOGGER.info ("unknown os: " + result.getStdout ());
        }
      }
      LOGGER.info ("wow, they all worked");
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed for command runner", e);
      LOGGER.info ("failed:", e);
      fail (e.getMessage ());
    }
  }
}

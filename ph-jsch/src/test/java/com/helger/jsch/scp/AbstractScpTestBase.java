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
package com.helger.jsch.scp;

import org.junit.BeforeClass;

import com.helger.base.rt.NonBlockingProperties;
import com.helger.base.wrapper.Wrapper;
import com.helger.jsch.JSchTestHelper;
import com.helger.jsch.session.ISessionFactory;

abstract class AbstractScpTestBase
{
  protected static ISessionFactory s_aSessionFactory;
  protected static String s_sScpPath;
  protected static String s_sFileSystemPath;

  @BeforeClass
  public static void initializeClass ()
  {
    final Wrapper <NonBlockingProperties> aProps = Wrapper.empty ();
    s_aSessionFactory = JSchTestHelper.createSessionFactoryFromConfig (aProps::set);
    s_sScpPath = aProps.isNotSet () ? null : aProps.get ().getProperty ("scp.out.test.scpPath");
    s_sFileSystemPath = aProps.isNotSet () ? null : aProps.get ().getProperty ("scp.out.test.filesystemPath");
  }
}

/**
 * Copyright (C) 2016-2017 Philip Helger (www.helger.com)
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
package com.helger.mail.datahandler;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.annotation.Nonnull;

public class DataSourceStreamingDataHandler extends AbstractStreamingDataHandler
{
  public DataSourceStreamingDataHandler (@Nonnull final DataSource aDataSource)
  {
    super (aDataSource);
  }

  @Override
  public InputStream readOnce () throws IOException
  {
    return getInputStream ();
  }

  @Override
  public void close () throws IOException
  {
    // nothing to do here
  }
}

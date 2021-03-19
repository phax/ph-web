/**
 * Copyright (C) 2016-2021 Philip Helger (www.helger.com)
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
package com.helger.mail.datasource;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.mime.CMimeType;
import com.helger.commons.mime.IMimeType;
import com.helger.mail.cte.IContentTransferEncoding;

/**
 * Extension interface for {@link DataSource}.
 *
 * @author Philip Helger
 */
public interface IExtendedDataSource extends DataSource
{
  IMimeType DEFAULT_CONTENT_TYPE = CMimeType.APPLICATION_OCTET_STREAM;

  @Nonnull
  default DataHandler getAsDataHandler ()
  {
    return new DataHandler (this);
  }

  @Nonnull
  default IEncodingAwareDataSource getEncodingAware (@Nullable final IContentTransferEncoding aCTE)
  {
    return IEncodingAwareDataSource.getEncodingAware (this, aCTE);
  }
}

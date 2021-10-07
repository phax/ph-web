/*
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.EncodingAware;

import com.helger.mail.cte.IContentTransferEncoding;

/**
 * Extension interface for {@link DataSource} that includes
 * {@link EncodingAware}.
 *
 * @author Philip Helger
 */
public interface IEncodingAwareDataSource extends IExtendedDataSource, EncodingAware
{
  @Nullable
  IContentTransferEncoding getContentTransferEncoding ();

  /**
   * @return The content transfer encoding to be used. May be <code>null</code>.
   * @see #getContentTransferEncoding()
   */
  @Nullable
  default String getEncoding ()
  {
    final IContentTransferEncoding aCTE = getContentTransferEncoding ();
    return aCTE == null ? null : aCTE.getID ();
  }

  @Nonnull
  static IEncodingAwareDataSource getEncodingAware (@Nonnull final DataSource aDS, @Nullable final IContentTransferEncoding aCTE)
  {
    return new IEncodingAwareDataSource ()
    {
      public String getContentType ()
      {
        return aDS.getContentType ();
      }

      public InputStream getInputStream () throws IOException
      {
        return aDS.getInputStream ();
      }

      public String getName ()
      {
        return aDS.getName ();
      }

      public OutputStream getOutputStream () throws IOException
      {
        return aDS.getOutputStream ();
      }

      @Nullable
      public IContentTransferEncoding getContentTransferEncoding ()
      {
        return aCTE;
      }
    };
  }
}

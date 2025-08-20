/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * {@link DataHandler} extended to offer better buffer management in a streaming
 * environment.<br>
 * {@link DataHandler} is used commonly as a data format across multiple systems
 * (such as JAXB/WS.) Unfortunately, {@link DataHandler} has the semantics of
 * "read as many times as you want", so this makes it difficult for involving
 * parties to handle a BLOB in a streaming fashion.<br>
 * {@link AbstractStreamingDataHandler} solves this problem by offering methods
 * that enable faster bulk "consume once" read operation.
 *
 * @author Jitendra Kotamraju
 * @author Philip Helger
 */
public abstract class AbstractStreamingDataHandler extends DataHandler implements Closeable
{
  private String m_sHrefCid;

  public AbstractStreamingDataHandler (@Nonnull final Object aObj, @Nonnull final String sMimeType)
  {
    super (aObj, sMimeType);
  }

  public AbstractStreamingDataHandler (@Nonnull final URL aUrl)
  {
    super (aUrl);
  }

  public AbstractStreamingDataHandler (@Nonnull final DataSource aDataSource)
  {
    super (aDataSource);
  }

  @Nullable
  public String getHrefCid ()
  {
    return m_sHrefCid;
  }

  public void setHrefCid (@Nullable final String sHrefCid)
  {
    m_sHrefCid = sHrefCid;
  }

  /**
   * Works like {@link #getInputStream()} except that this method can be invoked
   * only once.<br>
   * This is used as a signal from the caller that there will be no further
   * {@link #getInputStream()} invocation nor {@link #readOnce()} invocation on
   * this object (which would result in {@link IOException}.)<br>
   * When {@link DataHandler} is backed by a streaming BLOB (such as an
   * attachment in a web service read from the network), this allows the callee
   * to avoid unnecessary buffering.<br>
   * Note that it is legal to call {@link #getInputStream()} multiple times and
   * then call {@link #readOnce()} afterward. Streams created such a way can be
   * read in any order - there's no requirement that streams created earlier
   * must be read first.
   *
   * @return always non-<code>null</code>. Represents the content of this BLOB.
   *         The returned stream is generally not buffered, so for better
   *         performance read in a big batch or wrap this into
   *         {@link BufferedInputStream}.
   * @throws IOException
   *         if any i/o error
   */
  @Nonnull
  public abstract InputStream readOnce () throws IOException;
}

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
package com.helger.mail.cte;

import com.helger.base.codec.IByteArrayCodec;
import com.helger.base.id.IHasID;

import jakarta.annotation.Nonnull;

/**
 * Base interface for a content transfer encoding. See
 * {@link EContentTransferEncoding} for predefined ones.
 *
 * @author Philip Helger
 */
public interface IContentTransferEncoding extends IHasID <String>
{
  /**
   * @return A new encoder for this Content Transfer Encoding. May not be
   *         <code>null</code>.
   * @since 9.0.5
   */
  @Nonnull
  IByteArrayCodec createCodec ();
}

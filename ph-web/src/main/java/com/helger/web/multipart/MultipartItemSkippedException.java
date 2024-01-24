/*
 * Copyright (C) 2014-2024 Philip Helger (www.helger.com)
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
package com.helger.web.multipart;

import java.io.IOException;
import java.io.InputStream;

import com.helger.web.fileupload.IFileItemStream;

/**
 * This exception is thrown, if an attempt is made to read data from the
 * {@link InputStream}, which has been returned by
 * {@link IFileItemStream#openStream()}, after
 * {@link java.util.Iterator#hasNext()} has been invoked on the iterator, which
 * created the {@link IFileItemStream}.
 */
public class MultipartItemSkippedException extends IOException
{
  public MultipartItemSkippedException ()
  {}
}

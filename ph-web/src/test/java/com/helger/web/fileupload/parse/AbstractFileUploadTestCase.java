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
package com.helger.web.fileupload.parse;

import java.nio.charset.StandardCharsets;

import com.helger.commons.collection.impl.ICommonsList;
import com.helger.servlet.mock.MockHttpServletRequest;
import com.helger.web.fileupload.IFileItem;
import com.helger.web.fileupload.exception.FileUploadException;
import com.helger.web.fileupload.servlet.ServletFileUpload;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Base class for deriving test cases.
 */
public abstract class AbstractFileUploadTestCase
{
  protected static final String CONTENT_TYPE = "multipart/form-data; boundary=---1234";

  protected final ICommonsList <IFileItem> parseUpload (final byte [] bytes) throws FileUploadException
  {
    return parseUpload (bytes, CONTENT_TYPE);
  }

  protected final ICommonsList <IFileItem> parseUpload (final byte [] bytes, final String contentType) throws FileUploadException
  {
    final ServletFileUpload upload = new ServletFileUpload (new DiskFileItemFactory (10240));
    final HttpServletRequest request = new MockHttpServletRequest ().setContent (bytes).setContentType (contentType);

    final ICommonsList <IFileItem> fileItems = upload.parseRequest (request);
    return fileItems;
  }

  protected final ICommonsList <IFileItem> parseUpload (final String content) throws FileUploadException
  {
    final byte [] bytes = content.getBytes (StandardCharsets.US_ASCII);
    return parseUpload (bytes, CONTENT_TYPE);
  }
}

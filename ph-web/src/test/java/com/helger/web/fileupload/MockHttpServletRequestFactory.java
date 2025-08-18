/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.web.fileupload;

import java.nio.charset.StandardCharsets;

import com.helger.servlet.mock.MockHttpServletRequest;
import com.helger.servlet.request.RequestHelper;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;

final class MockHttpServletRequestFactory
{
  @Nonnull
  public static HttpServletRequest createHttpServletRequestWithNullContentType ()
  {
    final byte [] requestData = "foobar".getBytes (StandardCharsets.US_ASCII);
    return new MockHttpServletRequest ().setContent (requestData);
  }

  public static HttpServletRequest createValidHttpServletRequest (final String [] aFilenames)
  {
    // todo - provide a real implementation
    final StringBuilder sbRequestData = new StringBuilder ();

    for (final String sFilename : aFilenames)
      sbRequestData.append (sFilename);

    final byte [] requestData = sbRequestData.toString ().getBytes (StandardCharsets.US_ASCII);

    return new MockHttpServletRequest ().setContent (requestData).setContentType (RequestHelper.MULTIPART_FORM_DATA);
  }

  public static HttpServletRequest createInvalidHttpServletRequest ()
  {
    final byte [] requestData = "foobar".getBytes (StandardCharsets.US_ASCII);
    return new MockHttpServletRequest ().setContent (requestData).setContentType (RequestHelper.MULTIPART_FORM_DATA);
  }
}

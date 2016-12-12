/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import com.helger.commons.charset.CCharset;
import com.helger.servlet.request.RequestHelper;
import com.helger.web.mock.MockHttpServletRequest;

final class MockHttpServletRequestFactory
{
  @Nonnull
  public static HttpServletRequest createHttpServletRequestWithNullContentType ()
  {
    final byte [] requestData = "foobar".getBytes (CCharset.CHARSET_US_ASCII_OBJ);
    return new MockHttpServletRequest ().setContent (requestData);
  }

  public static HttpServletRequest createValidHttpServletRequest (final String [] aFilenames)
  {
    // todo - provide a real implementation
    final StringBuilder sbRequestData = new StringBuilder ();

    for (final String sFilename : aFilenames)
      sbRequestData.append (sFilename);

    final byte [] requestData = sbRequestData.toString ().getBytes (CCharset.CHARSET_US_ASCII_OBJ);

    return new MockHttpServletRequest ().setContent (requestData).setContentType (RequestHelper.MULTIPART_FORM_DATA);
  }

  public static HttpServletRequest createInvalidHttpServletRequest ()
  {
    final byte [] requestData = "foobar".getBytes (CCharset.CHARSET_US_ASCII_OBJ);
    return new MockHttpServletRequest ().setContent (requestData).setContentType (RequestHelper.MULTIPART_FORM_DATA);
  }
}

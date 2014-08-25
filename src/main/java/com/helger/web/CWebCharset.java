/**
 * Copyright (C) 2006-2014 phloc systems (www.phloc.com)
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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
package com.helger.web;

import java.nio.charset.Charset;

import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotations.PresentForCodeCoverage;
import com.helger.commons.charset.CCharset;
import com.helger.commons.url.URLUtils;
import com.helger.commons.xml.serialize.XMLWriterSettings;

/**
 * Contains the preferred charsets to be used for web stuff.
 * 
 * @author Philip Helger
 */
@Immutable
public final class CWebCharset
{
  /** Default charset for URLs: UTF-8 */
  public static final String CHARSET_URL = URLUtils.CHARSET_URL;
  /** Default charset for URLs: UTF-8 */
  public static final Charset CHARSET_URL_OBJ = URLUtils.CHARSET_URL_OBJ;
  /** Default charset for requests: UTF-8 */
  public static final String CHARSET_REQUEST = CCharset.CHARSET_UTF_8;
  /** Default charset for requests: UTF-8 */
  public static final Charset CHARSET_REQUEST_OBJ = CCharset.CHARSET_UTF_8_OBJ;
  /** Default charset for multipart requests: UTF-8 */
  public static final String CHARSET_MULTIPART = CCharset.CHARSET_UTF_8;
  /** Default charset for multipart requests: UTF-8 */
  public static final Charset CHARSET_MULTIPART_OBJ = CCharset.CHARSET_UTF_8_OBJ;
  /** Default charset for XML: UTF-8 */
  public static final String CHARSET_XML = XMLWriterSettings.DEFAULT_XML_CHARSET;
  /** Default charset for XML: UTF-8 */
  public static final Charset CHARSET_XML_OBJ = XMLWriterSettings.DEFAULT_XML_CHARSET_OBJ;
  /** Default charset for SMTP: UTF-8 */
  public static final String CHARSET_SMTP = CCharset.CHARSET_UTF_8;
  /** Default charset for SMTP: UTF-8 */
  public static final Charset CHARSET_SMTP_OBJ = CCharset.CHARSET_UTF_8_OBJ;

  @PresentForCodeCoverage
  @SuppressWarnings ("unused")
  private static final CWebCharset s_aInstance = new CWebCharset ();

  private CWebCharset ()
  {}
}

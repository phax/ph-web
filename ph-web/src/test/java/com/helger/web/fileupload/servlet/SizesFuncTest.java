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
package com.helger.web.fileupload.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.helger.commons.charset.CCharset;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.web.fileupload.IFileItem;
import com.helger.web.fileupload.exception.FileSizeLimitExceededException;
import com.helger.web.fileupload.exception.FileUploadException;
import com.helger.web.fileupload.parse.AbstractFileUploadTestCase;
import com.helger.web.fileupload.parse.DiskFileItemFactory;
import com.helger.web.mock.MockHttpServletRequest;

/**
 * Unit test for items with varying sizes.
 */
public final class SizesFuncTest extends AbstractFileUploadTestCase
{
  /**
   * Runs a test with varying file sizes.
   *
   * @throws IOException
   *         In case of error
   * @throws FileUploadException
   *         In case of error
   */
  @Test
  public void testFileUpload () throws IOException, FileUploadException
  {
    final NonBlockingByteArrayOutputStream baos = new NonBlockingByteArrayOutputStream ();
    int add = 16;
    int num = 0;
    for (int i = 0; i < 16384; i += add)
    {
      if (++add == 32)
      {
        add = 16;
      }
      final String header = "-----1234\r\n" +
                            "Content-Disposition: form-data; name=\"field" +
                            (num++) +
                            "\"\r\n" +
                            "\r\n";
      baos.write (header.getBytes (CCharset.CHARSET_US_ASCII_OBJ));
      for (int j = 0; j < i; j++)
      {
        baos.write ((byte) j);
      }
      baos.write ("\r\n".getBytes (CCharset.CHARSET_US_ASCII_OBJ));
    }
    baos.write ("-----1234--\r\n".getBytes (CCharset.CHARSET_US_ASCII_OBJ));

    final List <IFileItem> fileItems = parseUpload (baos.toByteArray ());
    final Iterator <IFileItem> fileIter = fileItems.iterator ();
    add = 16;
    num = 0;
    for (int i = 0; i < 16384; i += add)
    {
      if (++add == 32)
      {
        add = 16;
      }
      final IFileItem item = fileIter.next ();
      assertEquals ("field" + (num++), item.getFieldName ());
      final byte [] bytes = item.get ();
      assertEquals (i, bytes.length);
      for (int j = 0; j < i; j++)
      {
        assertEquals ((byte) j, bytes[j]);
      }
    }
    assertTrue (!fileIter.hasNext ());
  }

  /**
   * Checks, whether limiting the file size works.
   *
   * @throws FileUploadException
   *         In case of error
   */
  @Test
  public void testFileSizeLimit () throws FileUploadException
  {
    final String request = "-----1234\r\n" +
                           "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
                           "Content-Type: text/whatever\r\n" +
                           "\r\n" +
                           "This is the content of the file\n" +
                           "\r\n" +
                           "-----1234--\r\n";

    ServletFileUpload upload = new ServletFileUpload (new DiskFileItemFactory (10240));
    upload.setFileSizeMax (-1);
    HttpServletRequest req = new MockHttpServletRequest ().setContent (request.getBytes (CCharset.CHARSET_US_ASCII_OBJ))
                                                          .setContentType (CONTENT_TYPE);
    List <IFileItem> fileItems = upload.parseRequest (req);
    assertEquals (1, fileItems.size ());
    IFileItem item = fileItems.get (0);
    assertEquals ("This is the content of the file\n", new String (item.get (), CCharset.CHARSET_US_ASCII_OBJ));

    upload = new ServletFileUpload (new DiskFileItemFactory (10240));
    upload.setFileSizeMax (40);
    req = new MockHttpServletRequest ().setContent (request.getBytes (CCharset.CHARSET_US_ASCII_OBJ))
                                       .setContentType (CONTENT_TYPE);
    fileItems = upload.parseRequest (req);
    assertEquals (1, fileItems.size ());
    item = fileItems.get (0);
    assertEquals ("This is the content of the file\n", new String (item.get (), CCharset.CHARSET_US_ASCII_OBJ));

    upload = new ServletFileUpload (new DiskFileItemFactory (10240));
    upload.setFileSizeMax (30);
    req = new MockHttpServletRequest ().setContent (request.getBytes (CCharset.CHARSET_US_ASCII_OBJ))
                                       .setContentType (CONTENT_TYPE);
    try
    {
      upload.parseRequest (req);
      fail ("Expected exception.");
    }
    catch (final FileSizeLimitExceededException e)
    {
      assertEquals (30, e.getPermittedSize ());
    }
  }
}

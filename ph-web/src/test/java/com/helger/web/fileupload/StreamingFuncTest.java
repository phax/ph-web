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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.charset.CCharset;
import com.helger.commons.exception.mock.MockIOException;
import com.helger.web.fileupload.exception.FileUploadException;
import com.helger.web.fileupload.exception.IOFileUploadException;
import com.helger.web.fileupload.exception.InvalidFileNameException;
import com.helger.web.fileupload.exception.MultipartMalformedStreamException;
import com.helger.web.fileupload.io.DiskFileItemFactory;
import com.helger.web.fileupload.servlet.ServletFileUpload;
import com.helger.web.fileupload.servlet.ServletRequestContext;
import com.helger.web.mock.MockHttpServletRequest;
import com.helger.web.servlet.AbstractServletInputStream;

/**
 * Unit test for items with varying sizes.
 */
public final class StreamingFuncTest
{
  /**
   * Tests a file upload with varying file sizes.
   *
   * @throws Exception
   *         In case of error
   */
  @Test
  public void testFileUpload () throws Exception
  {
    final byte [] request = _newRequest ();
    final List <IFileItem> fileItems = _parseUploadToList (request);
    final Iterator <IFileItem> fileIter = fileItems.iterator ();
    int add = 16;
    int num = 0;
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
   * Tests, whether an invalid request throws a proper exception.
   *
   * @throws Exception
   *         In case of error
   */
  @Test
  public void testFileUploadException () throws Exception
  {
    final byte [] request = _newRequest ();
    final byte [] invalidRequest = new byte [request.length - 11];
    System.arraycopy (request, 0, invalidRequest, 0, request.length - 11);
    try
    {
      _parseUploadToList (invalidRequest);
      fail ("Expected EndOfStreamException");
    }
    catch (final IOFileUploadException e)
    {
      assertTrue (e.getCause () instanceof MultipartMalformedStreamException);
    }
  }

  /**
   * Tests, whether an IOException is properly delegated.
   *
   * @throws IOException
   *         In case of error
   */
  @Test
  public void testIOException () throws IOException
  {
    final byte [] request = _newRequest ();
    final InputStream stream = new FilterInputStream (new ByteArrayInputStream (request))
    {
      private int num;

      @Override
      public int read () throws IOException
      {
        if (++num > 123)
        {
          throw new MockIOException ("123");
        }
        return super.read ();
      }

      @Override
      public int read (final byte [] pB, final int pOff, final int pLen) throws IOException
      {
        for (int i = 0; i < pLen; i++)
        {
          final int res = read ();
          if (res == -1)
          {
            return i == 0 ? -1 : i;
          }
          pB[pOff + i] = (byte) res;
        }
        return pLen;
      }
    };
    try
    {
      _parseUploadToList (stream, request.length);
      fail ("Expected IOException");
    }
    catch (final FileUploadException e)
    {
      assertTrue (e.getCause () instanceof IOException);
      assertEquals ("123", e.getCause ().getMessage ());
    }
  }

  /**
   * Test for FILEUPLOAD-135
   *
   * @throws Exception
   *         In case of error
   */
  @Test
  public void testFILEUPLOAD135 () throws Exception
  {
    final byte [] request = _newShortRequest ();
    final ByteArrayInputStream bais = new ByteArrayInputStream (request);
    final List <IFileItem> fileItems = _parseUploadToList (new InputStream ()
    {
      @Override
      public int read () throws IOException
      {
        return bais.read ();
      }

      @Override
      public int read (final byte b[], final int off, final int len) throws IOException
      {
        return bais.read (b, off, Math.min (len, 3));
      }

    }, request.length);
    final Iterator <IFileItem> fileIter = fileItems.iterator ();
    assertTrue (fileIter.hasNext ());
    final IFileItem item = fileIter.next ();
    assertEquals ("field", item.getFieldName ());
    final byte [] bytes = item.get ();
    assertEquals (3, bytes.length);
    assertEquals ((byte) '1', bytes[0]);
    assertEquals ((byte) '2', bytes[1]);
    assertEquals ((byte) '3', bytes[2]);
    assertFalse (fileIter.hasNext ());
  }

  private IFileItemIterator _parseUploadToIterator (final byte [] aContent) throws FileUploadException, IOException
  {
    final String contentType = "multipart/form-data; boundary=---1234";

    final AbstractFileUploadBase upload = new ServletFileUpload (new DiskFileItemFactory (10240));
    final HttpServletRequest request = new MockHttpServletRequest ().setContent (aContent).setContentType (contentType);

    return upload.getItemIterator (new ServletRequestContext (request));
  }

  private List <IFileItem> _parseUploadToList (final byte [] bytes) throws FileUploadException
  {
    return _parseUploadToList (new ByteArrayInputStream (bytes), bytes.length);
  }

  @Nonnull
  @ReturnsMutableCopy
  private List <IFileItem> _parseUploadToList (final InputStream pStream, final int pLength) throws FileUploadException
  {
    final String contentType = "multipart/form-data; boundary=---1234";

    final AbstractFileUploadBase upload = new ServletFileUpload (new DiskFileItemFactory (10240));
    final MockHttpServletRequest request = new MockHttpServletRequest ()
    {
      @Override
      public int getContentLength ()
      {
        return pLength;
      }

      @Override
      public ServletInputStream getInputStream ()
      {
        return new AbstractServletInputStream ()
        {
          @Override
          public int read () throws IOException
          {
            return pStream.read ();
          }
        };
      }
    };
    request.setContentType (contentType);

    return upload.parseRequest (new ServletRequestContext (request));
  }

  private static String _getHeader (final String pField)
  {
    return "-----1234\r\n" + "Content-Disposition: form-data; name=\"" + pField + "\"\r\n" + "\r\n";
  }

  private static String _getFooter ()
  {
    return "-----1234--\r\n";
  }

  private static byte [] _newShortRequest () throws IOException
  {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    try (final OutputStreamWriter osw = new OutputStreamWriter (baos, "US-ASCII"))
    {
      osw.write (_getHeader ("field"));
      osw.write ("123");
      osw.write ("\r\n");
      osw.write (_getFooter ());
    }
    return baos.toByteArray ();
  }

  private static byte [] _newRequest () throws IOException
  {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    try (final OutputStreamWriter osw = new OutputStreamWriter (baos, "US-ASCII"))
    {
      int add = 16;
      int num = 0;
      for (int i = 0; i < 16384; i += add)
      {
        if (++add == 32)
        {
          add = 16;
        }
        osw.write (_getHeader ("field" + (num++)));
        osw.flush ();
        for (int j = 0; j < i; j++)
        {
          baos.write ((byte) j);
        }
        osw.write ("\r\n");
      }
      osw.write (_getFooter ());
    }
    return baos.toByteArray ();
  }

  /**
   * Tests, whether an {@link InvalidFileNameException} is thrown.
   *
   * @throws Exception
   *         In case of error
   */
  @Test
  public void testInvalidFileNameException () throws Exception
  {
    final String sFilename = "foo.exe\u0000.png";
    assertEquals (12, sFilename.length ());
    assertEquals (12, sFilename.toCharArray ().length);
    assertEquals (0, sFilename.toCharArray ()[7]);
    final String aRequest = "-----1234\r\n" +
                            "Content-Disposition: form-data; name=\"file\"; filename=\"" +
                            sFilename +
                            "\"\r\n" +
                            "Content-Type: text/whatever\r\n" +
                            "\r\n" +
                            "This is the content of the file\n" +
                            "\r\n" +
                            "-----1234\r\n" +
                            "Content-Disposition: form-data; name=\"field\"\r\n" +
                            "\r\n" +
                            "fieldValue\r\n" +
                            "-----1234\r\n" +
                            "Content-Disposition: form-data; name=\"multi\"\r\n" +
                            "\r\n" +
                            "value1\r\n" +
                            "-----1234\r\n" +
                            "Content-Disposition: form-data; name=\"multi\"\r\n" +
                            "\r\n" +
                            "value2\r\n" +
                            "-----1234--\r\n";
    final byte [] aReqBytes = aRequest.getBytes (CCharset.CHARSET_US_ASCII_OBJ);

    final IFileItemIterator fileItemIter = _parseUploadToIterator (aReqBytes);
    final IFileItemStream fileItemStream = fileItemIter.next ();
    try
    {
      fileItemStream.getName ();
      fail ("Expected exception");
    }
    catch (final InvalidFileNameException e)
    {
      assertEquals (sFilename, e.getName ());
      assertEquals (-1, e.getMessage ().indexOf (sFilename));
      assertTrue (e.getMessage ().indexOf ("foo.exe\\0.png") >= 0);
    }
    assertEquals ("foo.exe", fileItemStream.getNameSecure ());

    final List <IFileItem> fileItems = _parseUploadToList (aReqBytes);
    final IFileItem fileItem = fileItems.get (0);
    try
    {
      fileItem.getName ();
      fail ("Expected exception");
    }
    catch (final InvalidFileNameException e)
    {
      assertEquals (sFilename, e.getName ());
      assertTrue (e.getMessage ().indexOf (sFilename) == -1);
      assertTrue (e.getMessage ().indexOf ("foo.exe\\0.png") != -1);
    }
    assertEquals ("foo.exe", fileItem.getNameSecure ());
  }
}

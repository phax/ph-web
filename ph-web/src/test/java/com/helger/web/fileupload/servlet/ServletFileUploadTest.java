/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;

import com.helger.web.fileupload.IFileItem;
import com.helger.web.fileupload.exception.FileUploadException;
import com.helger.web.fileupload.parse.AbstractFileUploadTestCase;

/**
 * Unit tests
 *
 * @author <a href="mailto:jmcnally@apache.org">John McNally</a>
 * @author Sean C. Sullivan
 */
public final class ServletFileUploadTest extends AbstractFileUploadTestCase
{
  @Test
  public void testFileUpload () throws FileUploadException
  {
    final List <IFileItem> fileItems = parseUpload ("-----1234\r\n" +
                                                    "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
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
                                                    "-----1234--\r\n");
    assertEquals (4, fileItems.size ());

    final IFileItem file = fileItems.get (0);
    assertEquals ("file", file.getFieldName ());
    assertFalse (file.isFormField ());
    assertEquals ("This is the content of the file\n", file.getString ());
    assertEquals ("text/whatever", file.getContentType ());
    assertEquals ("foo.tab", file.getName ());

    final IFileItem field = fileItems.get (1);
    assertEquals ("field", field.getFieldName ());
    assertTrue (field.isFormField ());
    assertEquals ("fieldValue", field.getString ());

    final IFileItem multi0 = fileItems.get (2);
    assertEquals ("multi", multi0.getFieldName ());
    assertTrue (multi0.isFormField ());
    assertEquals ("value1", multi0.getString ());

    final IFileItem multi1 = fileItems.get (3);
    assertEquals ("multi", multi1.getFieldName ());
    assertTrue (multi1.isFormField ());
    assertEquals ("value2", multi1.getString ());
  }

  @Test
  public void testFilenameCaseSensitivity () throws FileUploadException
  {
    final List <IFileItem> fileItems = parseUpload ("-----1234\r\n" +
                                                    "Content-Disposition: form-data; name=\"FiLe\"; filename=\"FOO.tab\"\r\n" +
                                                    "Content-Type: text/whatever\r\n" +
                                                    "\r\n" +
                                                    "This is the content of the file\n" +
                                                    "\r\n" +
                                                    "-----1234--\r\n");
    assertEquals (1, fileItems.size ());

    final IFileItem file = fileItems.get (0);
    assertEquals ("FiLe", file.getFieldName ());
    assertEquals ("FOO.tab", file.getName ());
  }

  /**
   * This is what the browser does if you submit the form without choosing a
   * file.
   *
   * @throws FileUploadException
   *         In case of error
   */
  @Test
  public void testEmptyFile () throws FileUploadException
  {
    final List <IFileItem> fileItems = parseUpload ("-----1234\r\n" +
                                                    "Content-Disposition: form-data; name=\"file\"; filename=\"\"\r\n" +
                                                    "\r\n" +
                                                    "\r\n" +
                                                    "-----1234--\r\n");
    assertEquals (1, fileItems.size ());

    final IFileItem file = fileItems.get (0);
    assertFalse (file.isFormField ());
    assertEquals ("", file.getString ());
    assertEquals ("", file.getName ());
  }

  /**
   * Internet Explorer 5 for the Mac has a bug where the carriage return is
   * missing on any boundary line immediately preceding an input with
   * type=image. (type=submit does not have the bug.)
   *
   * @throws FileUploadException
   *         In case of error
   */
  @Test
  public void testIE5MacBug () throws FileUploadException
  {
    final List <IFileItem> fileItems = parseUpload ("-----1234\r\n" +
                                                    "Content-Disposition: form-data; name=\"field1\"\r\n" +
                                                    "\r\n" +
                                                    "fieldValue\r\n" +
                                                    "-----1234\n" + // NOTE \r
                                                                    // missing
                                                    "Content-Disposition: form-data; name=\"submitName.x\"\r\n" +
                                                    "\r\n" +
                                                    "42\r\n" +
                                                    "-----1234\n" + // NOTE \r
                                                                    // missing
                                                    "Content-Disposition: form-data; name=\"submitName.y\"\r\n" +
                                                    "\r\n" +
                                                    "21\r\n" +
                                                    "-----1234\r\n" +
                                                    "Content-Disposition: form-data; name=\"field2\"\r\n" +
                                                    "\r\n" +
                                                    "fieldValue2\r\n" +
                                                    "-----1234--\r\n");

    assertEquals (4, fileItems.size ());

    final IFileItem field1 = fileItems.get (0);
    assertEquals ("field1", field1.getFieldName ());
    assertTrue (field1.isFormField ());
    assertEquals ("fieldValue", field1.getString ());

    final IFileItem submitX = fileItems.get (1);
    assertEquals ("submitName.x", submitX.getFieldName ());
    assertTrue (submitX.isFormField ());
    assertEquals ("42", submitX.getString ());

    final IFileItem submitY = fileItems.get (2);
    assertEquals ("submitName.y", submitY.getFieldName ());
    assertTrue (submitY.isFormField ());
    assertEquals ("21", submitY.getString ());

    final IFileItem field2 = fileItems.get (3);
    assertEquals ("field2", field2.getFieldName ());
    assertTrue (field2.isFormField ());
    assertEquals ("fieldValue2", field2.getString ());
  }

  /**
   * Test for
   * <a href="http://issues.apache.org/jira/browse/FILEUPLOAD-62">FILEUPLOAD-62
   * </a>
   *
   * @throws Exception
   *         In case of error
   */
  @Test
  public void testFILEUPLOAD62 () throws Exception
  {
    final String contentType = "multipart/form-data; boundary=AaB03x";
    final String request = "--AaB03x\r\n" +
                           "content-disposition: form-data; name=\"field1\"\r\n" +
                           "\r\n" +
                           "Joe Blow\r\n" +
                           "--AaB03x\r\n" +
                           "content-disposition: form-data; name=\"pics\"\r\n" +
                           "Content-type: multipart/mixed; boundary=BbC04y\r\n" +
                           "\r\n" +
                           "--BbC04y\r\n" +
                           "Content-disposition: attachment; filename=\"file1.txt\"\r\n" +
                           "Content-Type: text/plain\r\n" +
                           "\r\n" +
                           "... contents of file1.txt ...\r\n" +
                           "--BbC04y\r\n" +
                           "Content-disposition: attachment; filename=\"file2.gif\"\r\n" +
                           "Content-type: image/gif\r\n" +
                           "Content-Transfer-Encoding: binary\r\n" +
                           "\r\n" +
                           "...contents of file2.gif...\r\n" +
                           "--BbC04y--\r\n" +
                           "--AaB03x--";
    final List <IFileItem> fileItems = parseUpload (request.getBytes (StandardCharsets.US_ASCII), contentType);
    assertEquals (3, fileItems.size ());
    final IFileItem item0 = fileItems.get (0);
    assertEquals ("field1", item0.getFieldName ());
    assertNull (item0.getName ());
    assertEquals ("Joe Blow", new String (item0.get (), StandardCharsets.ISO_8859_1));
    final IFileItem item1 = fileItems.get (1);
    assertEquals ("pics", item1.getFieldName ());
    assertEquals ("file1.txt", item1.getName ());
    assertEquals ("... contents of file1.txt ...", new String (item1.get (), StandardCharsets.ISO_8859_1));
    final IFileItem item2 = fileItems.get (2);
    assertEquals ("pics", item2.getFieldName ());
    assertEquals ("file2.gif", item2.getName ());
    assertEquals ("...contents of file2.gif...", new String (item2.get (), StandardCharsets.ISO_8859_1));
  }

  /**
   * Test for
   * <a href="http://issues.apache.org/jira/browse/FILEUPLOAD-111">FILEUPLOAD
   * -111</a>
   *
   * @throws FileUploadException
   *         In case of error
   */
  @Test
  public void testFoldedHeaders () throws FileUploadException
  {
    final List <IFileItem> fileItems = parseUpload ("-----1234\r\n" +
                                                    "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
                                                    "Content-Type: text/whatever\r\n" +
                                                    "\r\n" +
                                                    "This is the content of the file\n" +
                                                    "\r\n" +
                                                    "-----1234\r\n" +
                                                    "Content-Disposition: form-data; \r\n" +
                                                    "\tname=\"field\"\r\n" +
                                                    "\r\n" +
                                                    "fieldValue\r\n" +
                                                    "-----1234\r\n" +
                                                    "Content-Disposition: form-data;\r\n" +
                                                    "     name=\"multi\"\r\n" +
                                                    "\r\n" +
                                                    "value1\r\n" +
                                                    "-----1234\r\n" +
                                                    "Content-Disposition: form-data; name=\"multi\"\r\n" +
                                                    "\r\n" +
                                                    "value2\r\n" +
                                                    "-----1234--\r\n");
    assertEquals (4, fileItems.size ());

    final IFileItem file = fileItems.get (0);
    assertEquals ("file", file.getFieldName ());
    assertFalse (file.isFormField ());
    assertEquals ("This is the content of the file\n", file.getString ());
    assertEquals ("text/whatever", file.getContentType ());
    assertEquals ("foo.tab", file.getName ());

    final IFileItem field = fileItems.get (1);
    assertEquals ("field", field.getFieldName ());
    assertTrue (field.isFormField ());
    assertEquals ("fieldValue", field.getString ());

    final IFileItem multi0 = fileItems.get (2);
    assertEquals ("multi", multi0.getFieldName ());
    assertTrue (multi0.isFormField ());
    assertEquals ("value1", multi0.getString ());

    final IFileItem multi1 = fileItems.get (3);
    assertEquals ("multi", multi1.getFieldName ());
    assertTrue (multi1.isFormField ());
    assertEquals ("value2", multi1.getString ());
  }
}

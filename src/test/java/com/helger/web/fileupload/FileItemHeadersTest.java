/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.helger.web.fileupload.FileItemHeaders;
import com.helger.web.fileupload.IFileItemHeaders;

/**
 * Unit tests {@link IFileItemHeaders} and {@link FileItemHeaders}.
 *
 * @author Michael C. Macaluso
 */
public final class FileItemHeadersTest
{
  /**
   * @throws Exception
   *         Never
   */
  @Test
  public void testFileItemHeaders () throws Exception
  {
    final FileItemHeaders aMutableFileItemHeaders = new FileItemHeaders ();
    aMutableFileItemHeaders.addHeader ("Content-Disposition", "form-data; name=\"FileItem\"; filename=\"file1.txt\"");
    aMutableFileItemHeaders.addHeader ("Content-Type", "text/plain");

    aMutableFileItemHeaders.addHeader ("TestHeader", "headerValue1");
    aMutableFileItemHeaders.addHeader ("TestHeader", "headerValue2");
    aMutableFileItemHeaders.addHeader ("TestHeader", "headerValue3");
    aMutableFileItemHeaders.addHeader ("testheader", "headerValue4");

    {
      final Iterator <String> headerNameEnumeration = aMutableFileItemHeaders.getHeaderNames ();
      assertEquals ("content-disposition", headerNameEnumeration.next ());
      assertEquals ("content-type", headerNameEnumeration.next ());
      assertEquals ("testheader", headerNameEnumeration.next ());
      assertFalse (headerNameEnumeration.hasNext ());
    }

    {
      final List <String> headerNameList = aMutableFileItemHeaders.getAllHeaderNames ();
      assertEquals (3, headerNameList.size ());
      assertEquals ("content-disposition", headerNameList.get (0));
      assertEquals ("content-type", headerNameList.get (1));
      assertEquals ("testheader", headerNameList.get (2));
    }

    {
      assertEquals ("form-data; name=\"FileItem\"; filename=\"file1.txt\"",
                    aMutableFileItemHeaders.getHeader ("Content-Disposition"));
      assertEquals ("text/plain", aMutableFileItemHeaders.getHeader ("Content-Type"));
      assertEquals ("text/plain", aMutableFileItemHeaders.getHeader ("content-type"));
      assertEquals ("headerValue1", aMutableFileItemHeaders.getHeader ("TestHeader"));
      assertNull (aMutableFileItemHeaders.getHeader ("DummyHeader"));
    }

    Iterator <String> headerValueEnumeration;

    headerValueEnumeration = aMutableFileItemHeaders.getHeaders ("Content-Type");
    assertTrue (headerValueEnumeration.hasNext ());
    assertEquals (headerValueEnumeration.next (), "text/plain");
    assertFalse (headerValueEnumeration.hasNext ());

    headerValueEnumeration = aMutableFileItemHeaders.getHeaders ("content-type");
    assertTrue (headerValueEnumeration.hasNext ());
    assertEquals (headerValueEnumeration.next (), "text/plain");
    assertFalse (headerValueEnumeration.hasNext ());

    headerValueEnumeration = aMutableFileItemHeaders.getHeaders ("TestHeader");
    assertTrue (headerValueEnumeration.hasNext ());
    assertEquals ("headerValue1", headerValueEnumeration.next ());
    assertTrue (headerValueEnumeration.hasNext ());
    assertEquals ("headerValue2", headerValueEnumeration.next ());
    assertTrue (headerValueEnumeration.hasNext ());
    assertEquals ("headerValue3", headerValueEnumeration.next ());
    assertTrue (headerValueEnumeration.hasNext ());
    assertEquals ("headerValue4", headerValueEnumeration.next ());
    assertFalse (headerValueEnumeration.hasNext ());

    headerValueEnumeration = aMutableFileItemHeaders.getHeaders ("DummyHeader");
    assertFalse (headerValueEnumeration.hasNext ());
  }
}

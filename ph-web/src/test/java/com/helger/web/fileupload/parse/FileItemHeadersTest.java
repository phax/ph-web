/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import com.helger.commons.collection.impl.ICommonsList;
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
      final ICommonsList <String> headerNameList = aMutableFileItemHeaders.getAllHeaderNames ();
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

    Iterator <String> aHeaderValueIterator;

    aHeaderValueIterator = aMutableFileItemHeaders.getHeaders ("Content-Type");
    assertNotNull (aHeaderValueIterator);
    assertTrue (aHeaderValueIterator.hasNext ());
    assertEquals ("text/plain", aHeaderValueIterator.next ());
    assertFalse (aHeaderValueIterator.hasNext ());

    aHeaderValueIterator = aMutableFileItemHeaders.getHeaders ("content-type");
    assertNotNull (aHeaderValueIterator);
    assertTrue (aHeaderValueIterator.hasNext ());
    assertEquals ("text/plain", aHeaderValueIterator.next ());
    assertFalse (aHeaderValueIterator.hasNext ());

    aHeaderValueIterator = aMutableFileItemHeaders.getHeaders ("TestHeader");
    assertNotNull (aHeaderValueIterator);
    assertTrue (aHeaderValueIterator.hasNext ());
    assertEquals ("headerValue1", aHeaderValueIterator.next ());
    assertTrue (aHeaderValueIterator.hasNext ());
    assertEquals ("headerValue2", aHeaderValueIterator.next ());
    assertTrue (aHeaderValueIterator.hasNext ());
    assertEquals ("headerValue3", aHeaderValueIterator.next ());
    assertTrue (aHeaderValueIterator.hasNext ());
    assertEquals ("headerValue4", aHeaderValueIterator.next ());
    assertFalse (aHeaderValueIterator.hasNext ());

    aHeaderValueIterator = aMutableFileItemHeaders.getHeaders ("DummyHeader");
    assertNotNull (aHeaderValueIterator);
    assertFalse (aHeaderValueIterator.hasNext ());
  }
}

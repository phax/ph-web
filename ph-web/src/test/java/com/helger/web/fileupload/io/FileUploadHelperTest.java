/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
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
package com.helger.web.fileupload.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.helger.web.fileupload.exception.InvalidFileNameException;

/**
 * Security verification tests for file upload filename handling. Verifies that NUL-byte injection
 * is blocked by {@link FileUploadHelper#checkFileName(String)}. Documents that path traversal
 * protection requires callers to use {@code DiskFileItem.getNameSecure()} rather than
 * {@code getName()}.
 *
 * @author Philip Helger
 */
public final class FileUploadHelperTest
{
  @Test
  public void testNullFilenameIsAccepted ()
  {
    assertNull (FileUploadHelper.checkFileName (null));
  }

  @Test
  public void testNormalFilenameIsAccepted ()
  {
    assertEquals ("report.pdf", FileUploadHelper.checkFileName ("report.pdf"));
  }

  @Test (expected = InvalidFileNameException.class)
  public void testNulByteInFilenameIsRejected ()
  {
    // CWE-158: NUL byte injection in filenames can truncate the name at the OS level
    FileUploadHelper.checkFileName ("malicious.php\u0000.jpg");
    fail ("Should have thrown InvalidFileNameException");
  }

  @Test
  public void testPathTraversalNotBlockedByCheckFileName ()
  {
    // This test documents that checkFileName does NOT block path traversal.
    // Callers must use DiskFileItem.getNameSecure() for path traversal protection.
    final String sTraversalName = "../../etc/passwd";
    assertEquals (sTraversalName, FileUploadHelper.checkFileName (sTraversalName));
  }

  @Test
  public void testBackslashPathTraversalNotBlockedByCheckFileName ()
  {
    // Windows-style path traversal is also not blocked
    final String sTraversalName = "..\\..\\windows\\system32\\config\\sam";
    assertEquals (sTraversalName, FileUploadHelper.checkFileName (sTraversalName));
  }
}

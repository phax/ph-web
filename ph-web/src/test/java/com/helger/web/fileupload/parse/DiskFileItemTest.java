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
package com.helger.web.fileupload.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.base.mock.CommonsAssert;
import com.helger.web.fileupload.IFileItem;
import com.helger.web.fileupload.IFileItemFactory;

/**
 * Serialization Unit tests for {@link DiskFileItem}.
 */
public final class DiskFileItemTest
{
  /**
   * Content type for regular form items.
   */
  private static final String CT_TEXT = "text/plain";

  /**
   * Very low threshold for testing memory versus disk options.
   */
  private static final int THRESHOLD = 16;

  /**
   * Test creation of a field for which the amount of data falls below the configured threshold.
   */
  @Test
  public void testBelowThreshold ()
  {
    // Create the FileItem
    final byte [] aTestFieldValueBytes = _createContentBytes (THRESHOLD - 1);
    final IFileItem aItem = _createFileItem (aTestFieldValueBytes);

    // Check state is as expected
    assertTrue ("Initial: in memory", aItem.isInMemory ());
    assertEquals ("Initial: size", aItem.getSize (), aTestFieldValueBytes.length);
    _compareBytes ("Initial", aItem.directGet (), aTestFieldValueBytes);

    // Serialize & Deserialize
    if (false)
      try
      {
        final IFileItem newItem = (IFileItem) _serializeDeserialize (aItem);

        // Test deserialized content is as expected
        assertTrue ("Check in memory", newItem.isInMemory ());
        _compareBytes ("Check", aTestFieldValueBytes, newItem.directGet ());

        // Compare FileItem's (except byte[])
        _compareFileItems (aItem, newItem);

      }
      catch (final Exception e)
      {
        fail ("Error Serializing/Deserializing: " + e);
      }
  }

  /**
   * Test creation of a field for which the amount of data equals the configured threshold.
   */
  @Test
  public void testThreshold ()
  {
    // Create the FileItem
    final byte [] aTestFieldValueBytes = _createContentBytes (THRESHOLD);
    final IFileItem aItem = _createFileItem (aTestFieldValueBytes);

    // Check state is as expected
    assertTrue ("Initial: in memory", aItem.isInMemory ());
    assertEquals ("Initial: size", aItem.getSize (), aTestFieldValueBytes.length);
    _compareBytes ("Initial", aItem.directGet (), aTestFieldValueBytes);

    // Serialize & Deserialize
    if (false)
      try
      {
        final IFileItem aNewItem = (IFileItem) _serializeDeserialize (aItem);

        // Test deserialized content is as expected
        assertTrue ("Check in memory", aNewItem.isInMemory ());
        _compareBytes ("Check", aTestFieldValueBytes, aNewItem.directGet ());

        // Compare FileItem's (except byte[])
        _compareFileItems (aItem, aNewItem);

      }
      catch (final Exception e)
      {
        fail ("Error Serializing/Deserializing: " + e);
      }
  }

  /**
   * Test creation of a field for which the amount of data falls above the configured threshold.
   */
  @Test
  public void testAboveThreshold ()
  {
    // Create the FileItem
    final byte [] aTestFieldValueBytes = _createContentBytes (THRESHOLD + 1);
    final IFileItem aItem = _createFileItem (aTestFieldValueBytes);

    // Check state is as expected
    assertFalse ("Initial: in memory", aItem.isInMemory ());
    assertEquals ("Initial: size", aItem.getSize (), aTestFieldValueBytes.length);
    _compareBytes ("Initial", aItem.directGet (), aTestFieldValueBytes);

    // Serialize & Deserialize
    if (false)
      try
      {
        final IFileItem newItem = (IFileItem) _serializeDeserialize (aItem);

        // Test deserialized content is as expected
        assertFalse ("Check in memory", newItem.isInMemory ());
        _compareBytes ("Check", aTestFieldValueBytes, newItem.directGet ());

        // Compare FileItem's (except byte[])
        _compareFileItems (aItem, newItem);

      }
      catch (final Exception e)
      {
        fail ("Error Serializing/Deserializing: " + e);
      }
  }

  @Test
  public void testGetNameSecureStripsPathTraversal ()
  {
    final IFileItemFactory aFactory = new DiskFileItemFactory (THRESHOLD);
    final IFileItem aItem = aFactory.createItem ("field", CT_TEXT, false, "../../etc/passwd");
    // getNameSecure must strip path components
    final String sSecure = aItem.getNameSecure ();
    assertNotNull (sSecure);
    assertFalse ("getNameSecure must not contain '..'", sSecure.contains (".."));
    assertFalse ("getNameSecure must not contain '/'", sSecure.contains ("/"));
    assertEquals ("passwd", sSecure);
  }

  @Test
  public void testGetNameSecureStripsWindowsPath ()
  {
    final IFileItemFactory aFactory = new DiskFileItemFactory (THRESHOLD);
    final IFileItem aItem = aFactory.createItem ("field",
                                                 CT_TEXT,
                                                 false,
                                                 "C:\\Users\\evil\\..\\..\\Windows\\system.ini");
    final String sSecure = aItem.getNameSecure ();
    assertNotNull (sSecure);
    assertFalse ("getNameSecure must not contain '\\'", sSecure.contains ("\\"));
    assertEquals ("system.ini", sSecure);
  }

  @Test
  public void testGetNameSecureNormalFilename ()
  {
    final IFileItemFactory aFactory = new DiskFileItemFactory (THRESHOLD);
    final IFileItem aItem = aFactory.createItem ("field", CT_TEXT, false, "report.pdf");
    assertEquals ("report.pdf", aItem.getNameSecure ());
  }

  /**
   * Compare FileItem's (except the byte[] content)
   */
  private void _compareFileItems (@NonNull final IFileItem aOrigItem, @NonNull final IFileItem aNewItem)
  {
    CommonsAssert.assertEquals ("Compare: is in Memory", aOrigItem.isInMemory (), aNewItem.isInMemory ());
    CommonsAssert.assertEquals ("Compare: is Form Field", aOrigItem.isFormField (), aNewItem.isFormField ());
    assertEquals ("Compare: Field Name", aOrigItem.getFieldName (), aNewItem.getFieldName ());
    assertEquals ("Compare: Content Type", aOrigItem.getContentType (), aNewItem.getContentType ());
    assertEquals ("Compare: File Name", aOrigItem.getNameSecure (), aNewItem.getNameSecure ());
  }

  /**
   * Compare content bytes.
   */
  private static void _compareBytes (final String sText, final byte [] aOrigBytes, final byte [] aNewBytes)
  {
    assertNotNull (aOrigBytes);
    assertNotNull (aNewBytes);
    assertEquals (sText + " byte[] length", aOrigBytes.length, aNewBytes.length);
    for (int i = 0; i < aOrigBytes.length; i++)
      assertEquals (sText + " byte[" + i + "]", aOrigBytes[i], aNewBytes[i]);
  }

  /**
   * Create content bytes of a specified size.
   */
  private static byte [] _createContentBytes (final int nSize)
  {
    final StringBuilder aSB = new StringBuilder (nSize);
    byte nCount = 0;
    for (int i = 0; i < nSize; i++)
    {
      aSB.append (nCount);
      nCount++;
      if (nCount > 9)
        nCount = 0;
    }
    return aSB.toString ().getBytes (StandardCharsets.ISO_8859_1);
  }

  /**
   * Create a FileItem with the specified content bytes.
   */
  private static IFileItem _createFileItem (final byte [] aContentBytes)
  {
    final IFileItemFactory aFactory = new DiskFileItemFactory (THRESHOLD);
    final String sTextFieldName = "textField";

    final IFileItem aItem = aFactory.createItem (sTextFieldName, CT_TEXT, true, "My File Name");
    try (final OutputStream aOS = aItem.getOutputStream ())
    {
      aOS.write (aContentBytes);
    }
    catch (final IOException e)
    {
      fail ("Unexpected IOException" + e);
    }
    return aItem;
  }

  /**
   * Do serialization and deserialization.
   */
  private Object _serializeDeserialize (final Object aTarget)
  {
    // Serialize the test object
    try (final NonBlockingByteArrayOutputStream aBaos = new NonBlockingByteArrayOutputStream ())
    {
      try (final ObjectOutputStream aOos = new ObjectOutputStream (aBaos))
      {
        aOos.writeObject (aTarget);
        aOos.flush ();
      }
      catch (final Exception e)
      {
        fail ("Exception during serialization: " + e);
      }

      // Deserialize the test object
      Object ret = null;
      try (final NonBlockingByteArrayInputStream aBais = new NonBlockingByteArrayInputStream (aBaos.toByteArray ());
           final ObjectInputStream aOis = new ObjectInputStream (aBais))
      {
        ret = aOis.readObject ();
      }
      catch (final Exception e)
      {
        fail ("Exception during deserialization: " + e);
      }
      return ret;
    }
  }
}

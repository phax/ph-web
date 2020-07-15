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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

import org.junit.Test;

import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.mock.CommonsAssert;
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
  private static final String textContentType = "text/plain";

  /**
   * Very low threshold for testing memory versus disk options.
   */
  private static final int threshold = 16;

  /**
   * Test creation of a field for which the amount of data falls below the
   * configured threshold.
   */
  @Test
  public void testBelowThreshold ()
  {

    // Create the FileItem
    final byte [] testFieldValueBytes = _createContentBytes (threshold - 1);
    final IFileItem item = _createFileItem (testFieldValueBytes);

    // Check state is as expected
    assertTrue ("Initial: in memory", item.isInMemory ());
    assertEquals ("Initial: size", item.getSize (), testFieldValueBytes.length);
    _compareBytes ("Initial", item.directGet (), testFieldValueBytes);

    // Serialize & Deserialize
    try
    {
      final IFileItem newItem = (IFileItem) _serializeDeserialize (item);

      // Test deserialized content is as expected
      assertTrue ("Check in memory", newItem.isInMemory ());
      _compareBytes ("Check", testFieldValueBytes, newItem.directGet ());

      // Compare FileItem's (except byte[])
      _compareFileItems (item, newItem);

    }
    catch (final Exception e)
    {
      fail ("Error Serializing/Deserializing: " + e);
    }
  }

  /**
   * Test creation of a field for which the amount of data equals the configured
   * threshold.
   */
  @Test
  public void testThreshold ()
  {
    // Create the FileItem
    final byte [] testFieldValueBytes = _createContentBytes (threshold);
    final IFileItem item = _createFileItem (testFieldValueBytes);

    // Check state is as expected
    assertTrue ("Initial: in memory", item.isInMemory ());
    assertEquals ("Initial: size", item.getSize (), testFieldValueBytes.length);
    _compareBytes ("Initial", item.directGet (), testFieldValueBytes);

    // Serialize & Deserialize
    try
    {
      final IFileItem newItem = (IFileItem) _serializeDeserialize (item);

      // Test deserialized content is as expected
      assertTrue ("Check in memory", newItem.isInMemory ());
      _compareBytes ("Check", testFieldValueBytes, newItem.directGet ());

      // Compare FileItem's (except byte[])
      _compareFileItems (item, newItem);

    }
    catch (final Exception e)
    {
      fail ("Error Serializing/Deserializing: " + e);
    }
  }

  /**
   * Test creation of a field for which the amount of data falls above the
   * configured threshold.
   */
  @Test
  public void testAboveThreshold ()
  {

    // Create the FileItem
    final byte [] testFieldValueBytes = _createContentBytes (threshold + 1);
    final IFileItem item = _createFileItem (testFieldValueBytes);

    // Check state is as expected
    assertFalse ("Initial: in memory", item.isInMemory ());
    assertEquals ("Initial: size", item.getSize (), testFieldValueBytes.length);
    _compareBytes ("Initial", item.directGet (), testFieldValueBytes);

    // Serialize & Deserialize
    try
    {
      final IFileItem newItem = (IFileItem) _serializeDeserialize (item);

      // Test deserialized content is as expected
      assertFalse ("Check in memory", newItem.isInMemory ());
      _compareBytes ("Check", testFieldValueBytes, newItem.directGet ());

      // Compare FileItem's (except byte[])
      _compareFileItems (item, newItem);

    }
    catch (final Exception e)
    {
      fail ("Error Serializing/Deserializing: " + e);
    }
  }

  /**
   * Compare FileItem's (except the byte[] content)
   */
  private void _compareFileItems (@Nonnull final IFileItem aOrigItem, @Nonnull final IFileItem aNewItem)
  {
    CommonsAssert.assertEquals ("Compare: is in Memory", aOrigItem.isInMemory (), aNewItem.isInMemory ());
    CommonsAssert.assertEquals ("Compare: is Form Field", aOrigItem.isFormField (), aNewItem.isFormField ());
    assertEquals ("Compare: Field Name", aOrigItem.getFieldName (), aNewItem.getFieldName ());
    assertEquals ("Compare: Content Type", aOrigItem.getContentType (), aNewItem.getContentType ());
    assertEquals ("Compare: File Name", aOrigItem.getName (), aNewItem.getName ());
  }

  /**
   * Compare content bytes.
   */
  private void _compareBytes (final String text, final byte [] origBytes, final byte [] newBytes)
  {
    assertNotNull (origBytes);
    assertNotNull (newBytes);
    assertEquals (text + " byte[] length", origBytes.length, newBytes.length);
    for (int i = 0; i < origBytes.length; i++)
    {
      assertEquals (text + " byte[" + i + "]", origBytes[i], newBytes[i]);
    }
  }

  /**
   * Create content bytes of a specified size.
   */
  private static byte [] _createContentBytes (final int size)
  {
    final StringBuilder buffer = new StringBuilder (size);
    byte count = 0;
    for (int i = 0; i < size; i++)
    {
      buffer.append (count);
      count++;
      if (count > 9)
        count = 0;
    }
    return buffer.toString ().getBytes (StandardCharsets.ISO_8859_1);
  }

  /**
   * Create a FileItem with the specified content bytes.
   */
  private IFileItem _createFileItem (final byte [] contentBytes)
  {
    final IFileItemFactory factory = new DiskFileItemFactory (threshold);
    final String textFieldName = "textField";

    final IFileItem item = factory.createItem (textFieldName, textContentType, true, "My File Name");
    try (final OutputStream os = item.getOutputStream ())
    {
      os.write (contentBytes);
    }
    catch (final IOException e)
    {
      fail ("Unexpected IOException" + e);
    }
    return item;
  }

  /**
   * Do serialization and deserialization.
   */
  private Object _serializeDeserialize (final Object target)
  {
    // Serialize the test object
    final NonBlockingByteArrayOutputStream baos = new NonBlockingByteArrayOutputStream ();
    try (final ObjectOutputStream oos = new ObjectOutputStream (baos))
    {
      oos.writeObject (target);
      oos.flush ();
    }
    catch (final Exception e)
    {
      fail ("Exception during serialization: " + e);
    }

    // Deserialize the test object
    Object result = null;
    try (final NonBlockingByteArrayInputStream bais = new NonBlockingByteArrayInputStream (baos.toByteArray ());
         final ObjectInputStream ois = new ObjectInputStream (bais))
    {
      result = ois.readObject ();
    }
    catch (final Exception e)
    {
      fail ("Exception during deserialization: " + e);
    }
    return result;
  }
}

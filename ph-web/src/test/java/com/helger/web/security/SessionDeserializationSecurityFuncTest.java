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
package com.helger.web.security;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.Test;

import com.helger.web.fileupload.parse.DiskFileItem;
import com.helger.web.scope.session.SessionWebScopeActivator;

/**
 * Security verification tests for deserialization protection (audit finding F-02). Verifies that
 * ObjectInputFilter fields are present in classes with readObject methods.
 *
 * @author Philip Helger
 */
public final class SessionDeserializationSecurityFuncTest
{
  private static boolean _hasReadObject (@NonNull final Class <?> aClass)
  {
    for (final Method aMethod : aClass.getDeclaredMethods ())
      if ("readObject".equals (aMethod.getName ()))
      {
        final Class <?> [] aParamTypes = aMethod.getParameterTypes ();
        if (aParamTypes.length == 1 && aParamTypes[0] == ObjectInputStream.class)
          return true;
      }
    return false;
  }

  @Nullable
  private static Field _findStaticField (@NonNull final Class <?> aClass, @NonNull final Class <?> aFieldType)
  {
    for (final Field aField : aClass.getDeclaredFields ())
      if (Modifier.isStatic (aField.getModifiers ()) && aFieldType.isAssignableFrom (aField.getType ()))
        return aField;
    return null;
  }

  @Test
  public void testSessionWebScopeActivatorHasDeserFilter ()
  {
    // Verify that SessionWebScopeActivator has a DESER_FILTER field
    final Field aFilterField = _findStaticField (SessionWebScopeActivator.class, ObjectInputFilter.class);
    assertNotNull ("SessionWebScopeActivator should have a static ObjectInputFilter field " +
                   "to restrict deserialization of session attributes",
                   aFilterField);
  }

  @Test
  public void testDiskFileItemHasDeserFilter ()
  {
    // Verify that DiskFileItem has a DESER_FILTER field
    final Field aFilterField = _findStaticField (DiskFileItem.class, ObjectInputFilter.class);
    assertNotNull ("DiskFileItem should have a static ObjectInputFilter field " +
                   "to restrict deserialization of file item data",
                   aFilterField);
  }

  @Test
  public void testSessionWebScopeActivatorHasReadObject ()
  {
    assertTrue ("SessionWebScopeActivator should have a readObject(ObjectInputStream) method",
                _hasReadObject (SessionWebScopeActivator.class));
  }

  @Test
  public void testDiskFileItemHasReadObject ()
  {
    assertTrue ("DiskFileItem should have a readObject(ObjectInputStream) method", _hasReadObject (DiskFileItem.class));
  }
}

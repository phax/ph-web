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
package com.helger.web.servlet.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import com.helger.commons.collection.ext.CommonsHashMap;
import com.helger.commons.collection.ext.CommonsLinkedHashMap;
import com.helger.commons.collection.ext.ICommonsMap;
import com.helger.commons.collection.ext.ICommonsOrderedMap;
import com.helger.commons.mock.CommonsAssert;
import com.helger.commons.mock.CommonsTestHelper;
import com.helger.servlet.request.IRequestParamMap;
import com.helger.servlet.request.RequestParamMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unit test class for class {@link RequestParamMap}.
 *
 * @author Philip Helger
 */
@SuppressFBWarnings ("NP_NONNULL_PARAM_VIOLATION")
public final class RequestParamMapTest
{
  @Test
  public void test ()
  {
    final ICommonsMap <String, Object> aTestMap = new CommonsHashMap <> ();
    aTestMap.put ("a", "...");
    aTestMap.put ("page_name[de]", "deutscher name");
    aTestMap.put ("a", "...");
    aTestMap.put ("page_name[en]", "english name");
    aTestMap.put ("a", "...");
    aTestMap.put ("b", "...");
    aTestMap.put ("page_name[de]", "deutscher name");
    aTestMap.put ("c", "...");
    assertEquals (5, aTestMap.size ());

    final IRequestParamMap aMap = RequestParamMap.create (aTestMap);
    assertEquals (4, aMap.getSize ());
    assertTrue (aMap.containsKey ("a"));
    assertTrue (aMap.containsKey ("b"));
    assertTrue (aMap.containsKey ("c"));
    assertTrue (aMap.containsKey ("page_name"));

    // get page_name[de] and page_name[en]
    final IRequestParamMap aNames = aMap.getMap ("page_name");
    assertEquals (2, aNames.getSize ());
    final ICommonsOrderedMap <String, String> aValueMap = aNames.getAsValueMap ();
    assertEquals (2, aValueMap.size ());
    assertEquals ("deutscher name", aValueMap.get ("de"));
    assertEquals ("english name", aValueMap.get ("en"));

    // non-existing key
    assertFalse (aMap.contains ("xxx"));
    assertNull (aMap.getMap ("xxx"));
    assertNull (aMap.getString ("xxx"));

    // nested non-existing key
    assertFalse (aMap.contains ("xxx", "yyy"));
    assertNull (aMap.getMap ("xxx", "yyy"));
    assertNull (aMap.getString ("xxx", "yyy"));

    // non-existing nested key
    assertFalse (aMap.contains ("pagename", "yyy"));
    assertNull (aMap.getMap ("pagename", "yyy"));
    assertNull (aMap.getString ("pagename", "yyy"));

    // getting nested key of a non-map
    assertFalse (aMap.contains ("a", "yyy"));
    assertNull (aMap.getMap ("a", "yyy"));
    assertNull (aMap.getString ("a", "yyy"));
  }

  @SuppressWarnings ("deprecation")
  @Test
  public void testGetFieldName ()
  {
    assertEquals ("abc", RequestParamMap.getFieldName ("abc"));
    assertEquals ("abc[idx1][idx2]", RequestParamMap.getFieldName ("abc", "idx1", "idx2"));
  }

  @Test
  public void testSetSeparators ()
  {
    assertEquals (RequestParamMap.DEFAULT_OPEN, RequestParamMap.getOpenSeparator ());
    assertEquals (RequestParamMap.DEFAULT_CLOSE, RequestParamMap.getCloseSeparator ());
    RequestParamMap.setSeparators ('(', ')');
    assertEquals ("(", RequestParamMap.getOpenSeparator ());
    assertEquals (")", RequestParamMap.getCloseSeparator ());
    try
    {
      // Same chars not allowed
      RequestParamMap.setSeparators ('(', '(');
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {}
    assertEquals ("(", RequestParamMap.getOpenSeparator ());
    assertEquals (")", RequestParamMap.getCloseSeparator ());
    RequestParamMap.setSeparators ("!", "?");
    assertEquals ("!", RequestParamMap.getOpenSeparator ());
    assertEquals ("?", RequestParamMap.getCloseSeparator ());
    try
    {
      // null not allowed
      RequestParamMap.setSeparators (null, "!");
      fail ();
    }
    catch (final NullPointerException ex)
    {}
    assertEquals ("!", RequestParamMap.getOpenSeparator ());
    assertEquals ("?", RequestParamMap.getCloseSeparator ());
    try
    {
      // Empty not allowed
      RequestParamMap.setSeparators ("", "!");
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {}
    assertEquals ("!", RequestParamMap.getOpenSeparator ());
    assertEquals ("?", RequestParamMap.getCloseSeparator ());
    try
    {
      // null not allowed
      RequestParamMap.setSeparators ("!", null);
      fail ();
    }
    catch (final NullPointerException ex)
    {}
    assertEquals ("!", RequestParamMap.getOpenSeparator ());
    assertEquals ("?", RequestParamMap.getCloseSeparator ());
    try
    {
      // empty not allowed
      RequestParamMap.setSeparators ("!", "");
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {}
    assertEquals ("!", RequestParamMap.getOpenSeparator ());
    assertEquals ("?", RequestParamMap.getCloseSeparator ());
    try
    {
      // May not be identical
      RequestParamMap.setSeparators ("!", "!");
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {}
    assertEquals ("!", RequestParamMap.getOpenSeparator ());
    assertEquals ("?", RequestParamMap.getCloseSeparator ());
    try
    {
      // Close contains open
      RequestParamMap.setSeparators ("!", "!!");
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {}
    assertEquals ("!", RequestParamMap.getOpenSeparator ());
    assertEquals ("?", RequestParamMap.getCloseSeparator ());
    try
    {
      // Open contains close
      RequestParamMap.setSeparators ("!!", "!");
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {}
    assertEquals ("!", RequestParamMap.getOpenSeparator ());
    assertEquals ("?", RequestParamMap.getCloseSeparator ());
    RequestParamMap.setSeparators ("ab", "cd");
    assertEquals ("ab", RequestParamMap.getOpenSeparator ());
    assertEquals ("cd", RequestParamMap.getCloseSeparator ());

    // Restore default state
    RequestParamMap.setSeparatorsToDefault ();
    assertEquals (RequestParamMap.DEFAULT_OPEN, RequestParamMap.getOpenSeparator ());
    assertEquals (RequestParamMap.DEFAULT_CLOSE, RequestParamMap.getCloseSeparator ());
  }

  @SuppressWarnings ("deprecation")
  @Test
  public void testWithSeparators ()
  {
    RequestParamMap.setSeparators ("!!", "??");
    assertEquals ("abc", RequestParamMap.getFieldName ("abc"));
    assertEquals ("abc!!idx1??!!idx2??", RequestParamMap.getFieldName ("abc", "idx1", "idx2"));

    final ICommonsMap <String, Object> aTestMap = new CommonsHashMap <> ();
    aTestMap.put ("a", "...");
    aTestMap.put ("page_name!!de??", "deutscher name");
    aTestMap.put ("a", "...");
    aTestMap.put ("page_name!!en??", "english name");
    aTestMap.put ("a", "...");
    aTestMap.put ("b", "...");
    aTestMap.put ("page_name!!de??", "deutscher name");
    aTestMap.put ("c!!level1??!!level2??", "...");
    assertEquals (5, aTestMap.size ());

    final IRequestParamMap aMap = RequestParamMap.create (aTestMap);
    assertEquals (4, aMap.getSize ());
    assertTrue (aMap.containsKey ("a"));
    assertTrue (aMap.containsKey ("b"));
    assertTrue (aMap.containsKey ("c"));
    assertTrue (aMap.containsKey ("page_name"));

    // get page_name[de] and page_name[en]
    final IRequestParamMap aNames = aMap.getMap ("page_name");
    assertEquals (2, aNames.getSize ());
    final ICommonsOrderedMap <String, String> aValueMap = aNames.getAsValueMap ();
    assertEquals (2, aValueMap.size ());
    assertEquals ("deutscher name", aValueMap.get ("de"));
    assertEquals ("english name", aValueMap.get ("en"));

    // non-existing key
    assertFalse (aMap.contains ("xxx"));
    assertNull (aMap.getMap ("xxx"));
    assertNull (aMap.getString ("xxx"));

    // nested non-existing key
    assertFalse (aMap.contains ("xxx", "yyy"));
    assertNull (aMap.getMap ("xxx", "yyy"));
    assertNull (aMap.getString ("xxx", "yyy"));

    // non-existing nested key
    assertFalse (aMap.contains ("pagename", "yyy"));
    assertNull (aMap.getMap ("pagename", "yyy"));
    assertNull (aMap.getString ("pagename", "yyy"));

    // getting nested key of a non-map
    assertFalse (aMap.contains ("a", "yyy"));
    assertNull (aMap.getMap ("a", "yyy"));
    assertNull (aMap.getString ("a", "yyy"));

    assertNotNull (aMap.getMap ("c"));
    assertNotNull (aMap.getMap ("c", "level1"));
    assertNotNull (aMap.getString ("c", "level1", "level2"));

    RequestParamMap.setSeparatorsToDefault ();
  }

  @Test
  public void testInvalidNames1 ()
  {
    final ICommonsMap <String, Object> aTestMap = new CommonsHashMap <> ();
    aTestMap.put ("columns[0][][][][]", "bla");

    final IRequestParamMap aMap = RequestParamMap.create (aTestMap);
    assertTrue (aMap.contains ("columns"));
    assertNotNull (aMap.getMap ("columns"));
    assertTrue (aMap.getMap ("columns").contains ("0"));
    assertEquals ("bla", aMap.getMap ("columns").getString ("0"));
  }

  @Test
  public void testInvalidNames2 ()
  {
    final ICommonsMap <String, Object> aTestMap = new CommonsHashMap <> ();
    aTestMap.put ("columns[][][][][0]", "bla");

    final IRequestParamMap aMap = RequestParamMap.create (aTestMap);
    assertTrue (aMap.contains ("columns"));
    assertNotNull (aMap.getMap ("columns"));
    assertTrue (aMap.getMap ("columns").contains ("0"));
    assertEquals ("bla", aMap.getMap ("columns").getString ("0"));
  }

  @Test
  public void testExtend ()
  {
    // Order is important for this map!
    final ICommonsOrderedMap <String, Object> aTestMap = new CommonsLinkedHashMap <> ();
    aTestMap.put ("columns[name]", "bla");
    aTestMap.put ("columns[name][test]", "value");
    aTestMap.put ("columns[name][test2]", "value2");
    aTestMap.put ("columns[name2]", "2");
    aTestMap.put ("columns[name2][test]", "3");
    aTestMap.put ("columns[name2][test][again]", "true");
    aTestMap.put ("columns[name-equals]", "bla");
    aTestMap.put ("columns[name-equals][test]", "value");
    aTestMap.put ("columns[name-equals][test2]", "value2");

    final IRequestParamMap aMap = RequestParamMap.create (aTestMap);
    assertTrue (aMap.contains ("columns"));
    assertNotNull (aMap.getMap ("columns"));
    assertTrue (aMap.getMap ("columns").contains ("name"));
    assertEquals ("bla", aMap.getString ("columns", "name"));
    assertTrue (aMap.getMap ("columns").contains ("test"));
    assertTrue (aMap.getMap ("columns").contains ("test2"));
    assertEquals ("value", aMap.getString ("columns", "name", "test"));
    assertEquals ("value2", aMap.getString ("columns", "name", "test2"));
    assertEquals (2, aMap.getMap ("columns").getInt ("name2", -1));
    assertEquals (3L, aMap.getMap ("columns", "name2").getLong ("test", -1));
    CommonsAssert.assertEquals (3d, aMap.getMap ("columns", "name2").getDouble ("test", -1));
    assertEquals (BigInteger.valueOf (2), aMap.getBigInteger ("columns", "name2"));
    assertEquals (new BigDecimal (3).setScale (0), aMap.getBigDecimal ("columns", "name2", "test"));
    assertTrue (aMap.getMap ("columns", "name2", "test").getBoolean ("again", false));

    assertNull (aMap.getValueMap ("columns", "order"));
    assertEquals (2, aMap.getValueMap ("columns", "name").size ());
    assertTrue (aMap.getValueMap ("columns", "name").containsKey ("test"));
    assertTrue (aMap.getValueMap ("columns", "name").containsKey ("test2"));

    assertFalse (aMap.getMap ("columns").isEmpty ());
    assertFalse (aMap.getMap ("columns", "name").isEmpty ());
    assertTrue (aMap.getMap ("columns", "name", "test").isEmpty ());

    assertEquals (3, aMap.getMap ("columns").keySet ().size ());
    assertEquals (2, aMap.getMap ("columns", "name").keySet ().size ());
    assertEquals (0, aMap.getMap ("columns", "name", "test").keySet ().size ());

    assertEquals (3, aMap.getMap ("columns").values ().size ());
    assertEquals (2, aMap.getMap ("columns", "name").values ().size ());
    assertEquals (0, aMap.getMap ("columns", "name", "test").values ().size ());

    assertEquals (3, aMap.getMap ("columns").getAsObjectMap ().size ());
    assertEquals (2, aMap.getMap ("columns", "name").getAsObjectMap ().size ());
    assertEquals (0, aMap.getMap ("columns", "name", "test").getAsObjectMap ().size ());

    CommonsTestHelper.testDefaultSerialization (aMap);
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (aMap.getMap ("columns", "name"),
                                                                       aMap.getMap ("columns", "name"));
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (aMap.getMap ("columns", "name"),
                                                                       aMap.getMap ("columns", "name-equals"));
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (aMap.getMap ("columns", "name"),
                                                                           aMap.getMap ("columns", "name2"));
  }
}

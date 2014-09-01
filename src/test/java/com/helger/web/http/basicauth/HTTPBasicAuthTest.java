/**
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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
package com.helger.web.http.basicauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Test class for class {@link HTTPBasicAuth}.
 * 
 * @author Philip Helger
 */
public final class HTTPBasicAuthTest
{
  @Test
  public void testBasic ()
  {
    final BasicAuthClientCredentials aCredentials = new BasicAuthClientCredentials ("Alladin", "open sesame");
    final String sValue = aCredentials.getRequestValue ();
    assertNotNull (sValue);
    final BasicAuthClientCredentials aDecoded = HTTPBasicAuth.getBasicAuthClientCredentials (sValue);
    assertNotNull (aDecoded);
    assertEquals (aCredentials, aDecoded);
  }

  @Test
  public void testUserNameOnly ()
  {
    BasicAuthClientCredentials aCredentials = new BasicAuthClientCredentials ("Alladin");
    String sValue = aCredentials.getRequestValue ();
    assertNotNull (sValue);
    BasicAuthClientCredentials aDecoded = HTTPBasicAuth.getBasicAuthClientCredentials (sValue);
    assertNotNull (aDecoded);
    assertEquals (aCredentials, aDecoded);

    aCredentials = new BasicAuthClientCredentials ("Alladin", "");
    sValue = aCredentials.getRequestValue ();
    assertNotNull (sValue);
    aDecoded = HTTPBasicAuth.getBasicAuthClientCredentials (sValue);
    assertNotNull (aDecoded);
    assertEquals (aCredentials, aDecoded);
  }

  @Test
  public void testGetBasicAuthValues ()
  {
    assertNull (HTTPBasicAuth.getBasicAuthClientCredentials ((String) null));
    assertNull (HTTPBasicAuth.getBasicAuthClientCredentials (""));
    assertNull (HTTPBasicAuth.getBasicAuthClientCredentials ("bla"));
    assertNull (HTTPBasicAuth.getBasicAuthClientCredentials ("bla foor"));
    assertNull (HTTPBasicAuth.getBasicAuthClientCredentials ("Basic"));
    assertNull (HTTPBasicAuth.getBasicAuthClientCredentials ("  Basic  "));
    // Base64 with blanks is OK!
    BasicAuthClientCredentials aUP = HTTPBasicAuth.getBasicAuthClientCredentials ("  Basic  QWxsYW  Rp   bjpvcG  VuIH Nlc2F tZQ   =  =   ");
    assertNotNull (aUP);
    assertEquals ("Alladin", aUP.getUserName ());
    assertEquals ("open sesame", aUP.getPassword ());

    aUP = HTTPBasicAuth.getBasicAuthClientCredentials ("  Basic  QWxsYWRpbjpvcGVuIHNlc2FtZQ==   ");
    assertNotNull (aUP);
    assertEquals ("Alladin", aUP.getUserName ());
    assertEquals ("open sesame", aUP.getPassword ());
  }
}

package com.helger.mail.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.mail.internet.InternetAddress;

import org.junit.Test;

import com.helger.commons.email.EmailAddress;
import com.helger.commons.typeconvert.TypeConverter;

public final class TypeConverterFuncTest
{
  @Test
  public void testEmailAddress ()
  {
    final EmailAddress aEA = new EmailAddress ("spam@helger.com", "Philip");
    final InternetAddress aIA = TypeConverter.convertIfNecessary (aEA, InternetAddress.class);
    assertNotNull (aIA);
    final EmailAddress aEA2 = TypeConverter.convertIfNecessary (aIA, EmailAddress.class);
    assertNotNull (aEA2);
    assertEquals (aEA, aEA2);
  }
}

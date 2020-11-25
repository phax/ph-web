package com.helger.dns.naptr;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test class for class {@link NaptrResolver}.
 *
 * @author Philip Helger
 */
public class NaptrResolverTest
{
  @Test
  public void testGetAppliedNAPTRRegEx ()
  {
    assertEquals ("http://test-infra.peppol.at",
                  NaptrResolver.getAppliedNAPTRRegEx ("!^.*$!http://test-infra.peppol.at!", "bla.foo.example.org"));
    assertEquals ("http://test-infra.peppol.at",
                  NaptrResolver.getAppliedNAPTRRegEx ("!.*!http://test-infra.peppol.at!", "bla.foo.example.org"));
  }
}

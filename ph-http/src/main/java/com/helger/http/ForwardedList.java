package com.helger.http;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsOrderedMap;

/**
 * This class contains the different value pairs for a single "Forwarded" header as defined in RFC
 * 7239.
 *
 * @author Philip Helger
 * @since 10.5.1
 */
@NotThreadSafe
public class ForwardedList
{
  private final ICommonsOrderedMap <String, String> m_aPairs = new CommonsLinkedHashMap <> ();

  public ForwardedList ()
  {}

  @Nonnull
  public ForwardedList addPair (@Nonnull @Nonempty final String sToken, @Nonnull final String sValue)
  {
    ValueEnforcer.notEmpty (sToken, "Token");
    ValueEnforcer.isTrue ( () -> RFC7230Helper.isValidToken (sToken), "Token is not valid according to RFC 7230");
    ValueEnforcer.notNull (sValue, "Value");
    m_aPairs.put (sToken, sValue);
    return this;
  }
}

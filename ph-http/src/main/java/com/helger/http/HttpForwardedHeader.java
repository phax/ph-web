package com.helger.http;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;

/**
 * This class contains the different value pairs for a single "Forwarded" header as defined in RFC
 * 7239.
 *
 * @author Philip Helger
 * @since 10.5.1
 */
@NotThreadSafe
public class HttpForwardedHeader
{
  /** Standard "for" parameter name as defined in RFC 7239 */
  public static final String PARAM_FOR = "for";

  /** Standard "host" parameter name as defined in RFC 7239 */
  public static final String PARAM_HOST = "host";

  /** Standard "by" parameter name as defined in RFC 7239 */
  public static final String PARAM_BY = "by";

  /** Standard "proto" parameter name as defined in RFC 7239 */
  public static final String PARAM_PROTO = "proto";

  private final ICommonsOrderedMap <String, String> m_aPairs = new CommonsLinkedHashMap <> ();

  public HttpForwardedHeader ()
  {}

  @Nonnull
  public HttpForwardedHeader addPair (@Nonnull @Nonempty final String sToken, @Nonnull final String sValue)
  {
    ValueEnforcer.notEmpty (sToken, "Token");
    ValueEnforcer.isTrue ( () -> RFC7230Helper.isValidToken (sToken), "Token is not valid according to RFC 7230");
    ValueEnforcer.notNull (sValue, "Value");
    m_aPairs.put (sToken, sValue);
    return this;
  }

  /**
   * Get the value for the specified token.
   *
   * @param sToken
   *        The token to get the value for. May not be <code>null</code> or empty.
   * @return <code>null</code> if no such token is present.
   */
  @Nullable
  public String getValue (@Nonnull @Nonempty final String sToken)
  {
    ValueEnforcer.notEmpty (sToken, "Token");
    return m_aPairs.get (sToken);
  }

  /**
   * Check if the specified token is present.
   *
   * @param sToken
   *        The token to check. May not be <code>null</code> or empty.
   * @return <code>true</code> if the token is present, <code>false</code> otherwise.
   */
  public boolean containsToken (@Nonnull @Nonempty final String sToken)
  {
    ValueEnforcer.notEmpty (sToken, "Token");
    return m_aPairs.containsKey (sToken);
  }

  /**
   * Remove the specified token and its value.
   *
   * @param sToken
   *        The token to remove. May not be <code>null</code> or empty.
   * @return The previous value associated with the token, or <code>null</code> if no such token
   *         existed.
   */
  @Nullable
  public String removePair (@Nonnull @Nonempty final String sToken)
  {
    ValueEnforcer.notEmpty (sToken, "Token");
    return m_aPairs.remove (sToken);
  }

  /**
   * Get all token names in the order they were added.
   *
   * @return A mutable copy of all token names. Never <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableCopy
  public ICommonsOrderedSet <String> getAllTokens ()
  {
    return m_aPairs.copyOfKeySet ();
  }

  /**
   * Get a copy of all pairs.
   *
   * @return A mutable copy of all pairs. Never <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableCopy
  public ICommonsOrderedMap <String, String> getAllPairs ()
  {
    return m_aPairs.getClone ();
  }

  /**
   * Remove all pairs.
   *
   * @return this for chaining.
   */
  @Nonnull
  public HttpForwardedHeader removeAll ()
  {
    m_aPairs.clear ();
    return this;
  }

  /**
   * Check if this forwarded list is empty (contains no pairs).
   *
   * @return <code>true</code> if empty, <code>false</code> otherwise.
   */
  public boolean isEmpty ()
  {
    return m_aPairs.isEmpty ();
  }

  /**
   * Check if this forwarded list is not empty (contains at least one pair).
   *
   * @return <code>true</code> if not empty, <code>false</code> otherwise.
   */
  public boolean isNotEmpty ()
  {
    return m_aPairs.isNotEmpty ();
  }

  /**
   * Get the number of pairs contained in this list.
   *
   * @return The number of pairs. Always &ge; 0.
   */
  public int size ()
  {
    return m_aPairs.size ();
  }

  /**
   * Add the "for" parameter with the specified client identifier. This parameter identifies the
   * client that the request came from.
   *
   * @param sClientIdentifier
   *        The client identifier. May not be <code>null</code>.
   * @return this for chaining.
   */
  @Nonnull
  public HttpForwardedHeader setFor (@Nonnull final String sClientIdentifier)
  {
    return addPair (PARAM_FOR, sClientIdentifier);
  }

  /**
   * Get the "for" parameter value.
   *
   * @return The client identifier or <code>null</code> if not present.
   */
  @Nullable
  public String getFor ()
  {
    return getValue (PARAM_FOR);
  }

  /**
   * Add the "host" parameter with the specified host header value. This parameter contains the host
   * header field value that the client requested.
   *
   * @param sHost
   *        The host header value. May not be <code>null</code>.
   * @return this for chaining.
   */
  @Nonnull
  public HttpForwardedHeader setHost (@Nonnull final String sHost)
  {
    return addPair (PARAM_HOST, sHost);
  }

  /**
   * Get the "host" parameter value.
   *
   * @return The host header value or <code>null</code> if not present.
   */
  @Nullable
  public String getHost ()
  {
    return getValue (PARAM_HOST);
  }

  /**
   * Add the "by" parameter with the specified proxy identifier. This parameter identifies the proxy
   * that received the request.
   *
   * @param sProxyIdentifier
   *        The proxy identifier. May not be <code>null</code>.
   * @return this for chaining.
   */
  @Nonnull
  public HttpForwardedHeader setBy (@Nonnull final String sProxyIdentifier)
  {
    return addPair (PARAM_BY, sProxyIdentifier);
  }

  /**
   * Get the "by" parameter value.
   *
   * @return The proxy identifier or <code>null</code> if not present.
   */
  @Nullable
  public String getBy ()
  {
    return getValue (PARAM_BY);
  }

  /**
   * Add the "proto" parameter with the specified protocol scheme. This parameter indicates the
   * protocol scheme of the original request.
   *
   * @param sProtocolScheme
   *        The protocol scheme. May not be <code>null</code>.
   * @return this for chaining.
   */
  @Nonnull
  public HttpForwardedHeader setProto (@Nonnull final String sProtocolScheme)
  {
    return addPair (PARAM_PROTO, sProtocolScheme);
  }

  /**
   * Get the "proto" parameter value.
   *
   * @return The protocol scheme or <code>null</code> if not present.
   */
  @Nullable
  public String getProto ()
  {
    return getValue (PARAM_PROTO);
  }

  /**
   * Check if a value needs to be quoted according to RFC 7239. Values that are not valid tokens
   * must be quoted.
   *
   * @param sValue
   *        The value to check. May not be <code>null</code>.
   * @return <code>true</code> if the value needs quoting, <code>false</code> otherwise.
   */
  private static boolean _needsQuoting (@Nonnull final String sValue)
  {
    if (StringHelper.hasNoText (sValue))
      return true;

    // Check if it's a valid IPv6 address in brackets (special case)
    if (sValue.startsWith ("[") && sValue.endsWith ("]"))
      return false;

    // Check if it's a valid token
    return !RFC7230Helper.isValidToken (sValue);
  }

  /**
   * Convert this forwarded list to its string representation according to RFC 7239. The format is:
   * token=value;token=value;...
   *
   * @return The string representation. Never <code>null</code> but may be empty.
   */
  @Nonnull
  public String getAsString ()
  {
    if (m_aPairs.isEmpty ())
      return "";

    final StringBuilder aSB = new StringBuilder ();
    boolean bFirst = true;
    for (final var aEntry : m_aPairs.entrySet ())
    {
      if (bFirst)
        bFirst = false;
      else
        aSB.append (';');

      aSB.append (aEntry.getKey ()).append ('=');

      final String sValue = aEntry.getValue ();
      // Quote the value if it contains special characters or is not a valid token
      if (_needsQuoting (sValue))
      {
        aSB.append ('"');
        // Escape quotes and backslashes in the value
        for (int i = 0; i < sValue.length (); i++)
        {
          final char c = sValue.charAt (i);
          if (c == '"' || c == '\\')
            aSB.append ('\\');
          aSB.append (c);
        }
        aSB.append ('"');
      }
      else
      {
        // Append plain value
        aSB.append (sValue);
      }
    }
    return aSB.toString ();
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;

    final HttpForwardedHeader rhs = (HttpForwardedHeader) o;
    return m_aPairs.equals (rhs.m_aPairs);
  }

  @Override
  public int hashCode ()
  {
    return getClass ().hashCode () * 31 + m_aPairs.hashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Pairs", m_aPairs).getToString ();
  }
}

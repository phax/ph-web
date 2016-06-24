package com.helger.mail.cte;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.helger.commons.codec.IDecoder;
import com.helger.commons.id.IHasID;

/**
 * Base interface for a content transfer encoding. See
 * {@link EContentTransferEncoding} for predefined ones.
 * 
 * @author Philip Helger
 */
public interface IContentTransferEncoding extends IHasID <String>, Serializable
{
  /**
   * @return A new decoder for this Content Transfer Encoding. May not be
   *         <code>null</code>.
   */
  @Nonnull
  IDecoder <byte [], byte []> createDecoder ();
}

package com.helger.mail.datasource;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.Nonnull;

import com.helger.commons.mime.CMimeType;
import com.helger.commons.mime.IMimeType;

/**
 * Extension interface for {@link DataSource}.
 *
 * @author Philip Helger
 */
public interface IExtendedDataSource extends DataSource
{
  IMimeType DEFAULT_CONTENT_TYPE = CMimeType.APPLICATION_OCTET_STREAM;

  @Nonnull
  default DataHandler getAsDataHandler ()
  {
    return new DataHandler (this);
  }
}

package com.helger.mail.datahandler;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.annotation.Nonnull;

public class DataSourceStreamingDataHandler extends AbstractStreamingDataHandler
{
  public DataSourceStreamingDataHandler (@Nonnull final DataSource aDataSource)
  {
    super (aDataSource);
  }

  @Override
  public InputStream readOnce () throws IOException
  {
    return getInputStream ();
  }

  @Override
  public void close () throws IOException
  {
    // nothing to do here
  }
}

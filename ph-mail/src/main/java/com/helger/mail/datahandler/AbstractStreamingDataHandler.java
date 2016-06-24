package com.helger.mail.datahandler;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;

import com.helger.commons.io.stream.StreamHelper;

/**
 * {@link DataHandler} extended to offer better buffer management in a streaming
 * environment.<br>
 * {@link DataHandler} is used commonly as a data format across multiple systems
 * (such as JAXB/WS.) Unfortunately, {@link DataHandler} has the semantics of
 * "read as many times as you want", so this makes it difficult for involving
 * parties to handle a BLOB in a streaming fashion.<br>
 * {@link AbstractStreamingDataHandler} solves this problem by offering methods
 * that enable faster bulk "consume once" read operation.
 *
 * @author Jitendra Kotamraju
 * @author Philip Helger
 */
public abstract class AbstractStreamingDataHandler extends DataHandler implements Closeable
{
  private String m_sHrefCid;

  public AbstractStreamingDataHandler (@Nonnull final Object aObj, @Nonnull final String sMimeType)
  {
    super (aObj, sMimeType);
  }

  public AbstractStreamingDataHandler (@Nonnull final URL aUrl)
  {
    super (aUrl);
  }

  public AbstractStreamingDataHandler (@Nonnull final DataSource aDataSource)
  {
    super (aDataSource);
  }

  @Nullable
  public String getHrefCid ()
  {
    return m_sHrefCid;
  }

  public void setHrefCid (@Nullable final String sHrefCid)
  {
    m_sHrefCid = sHrefCid;
  }

  /**
   * Works like {@link #getInputStream()} except that this method can be invoked
   * only once.<br>
   * This is used as a signal from the caller that there will be no further
   * {@link #getInputStream()} invocation nor {@link #readOnce()} invocation on
   * this object (which would result in {@link IOException}.)<br>
   * When {@link DataHandler} is backed by a streaming BLOB (such as an
   * attachment in a web service read from the network), this allows the callee
   * to avoid unnecessary buffering.<br>
   * Note that it is legal to call {@link #getInputStream()} multiple times and
   * then call {@link #readOnce()} afterward. Streams created such a way can be
   * read in any order - there's no requirement that streams created earlier
   * must be read first.
   *
   * @return always non-<code>null</code>. Represents the content of this BLOB.
   *         The returned stream is generally not buffered, so for better
   *         performance read in a big batch or wrap this into
   *         {@link BufferedInputStream}.
   * @throws IOException
   *         if any i/o error
   */
  @Nonnull
  public abstract InputStream readOnce () throws IOException;

  /**
   * Obtains the BLOB into a specified file. <br>
   * Semantically, this method is roughly equivalent to the following code,
   * except that the actual implementation is likely to be a lot faster.
   *
   * <pre>
   * InputStream i = getInputStream ();
   * OutputStream o = new FileOutputStream (dst);
   * int ch;
   * while ((ch = i.read ()) != -1)
   *   o.write (ch);
   * i.close ();
   * o.close ();
   * </pre>
   *
   * The main motivation behind this method is that often {@link DataHandler}
   * that reads data from a streaming source will use a temporary file as a data
   * store to hold data (think of commons-fileupload.) In such case this method
   * can be as fast as calling {@link File#renameTo(File)}.<br>
   * This method shouldn't be called when there are any open streams.<br>
   * After this method is invoked, {@link #readOnce()} and
   * {@link #getInputStream()} will simply open the destination file you've
   * specified as an argument. So if you further move the file or delete this
   * file, those methods will behave in undefined fashion. For a similar reason,
   * calling this method multiple times will cause undefined behavior.
   *
   * @param aOS
   *        The OutputStream to write to. May not be <code>null</code>. Is
   *        closed internally.
   * @throws IOException
   *         If obtaining the input stream failed.
   */
  public void moveTo (@Nonnull @WillClose final OutputStream aOS) throws IOException
  {
    StreamHelper.copyInputStreamToOutputStreamAndCloseOS (getInputStream (), aOS);
  }

  /**
   * Releases any resources associated with this DataHandler (such as an
   * attachment in a web service read from a temporary file will be deleted).
   * After calling this method, it is illegal to call any other methods.
   */
  public abstract void close () throws IOException;
}

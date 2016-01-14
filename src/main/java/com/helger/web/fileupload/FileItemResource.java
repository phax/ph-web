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
package com.helger.web.fileupload;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.UnsupportedOperation;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.string.ToStringGenerator;

/**
 * Implementation of the {@link IReadableResource} interface for uploaded
 * {@link IFileItem} objects.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public final class FileItemResource implements IReadableResource
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (FileItemResource.class);

  private final IFileItem m_aFileItem;
  private Integer m_aHashCode;

  public FileItemResource (@Nonnull final IFileItem aFileItem)
  {
    m_aFileItem = ValueEnforcer.notNull (aFileItem, "FileItem");
  }

  @Nonnull
  public String getResourceID ()
  {
    return getPath ();
  }

  @Nonnull
  public String getPath ()
  {
    return m_aFileItem.getNameSecure ();
  }

  @Nullable
  public InputStream getInputStream ()
  {
    return m_aFileItem.getInputStream ();
  }

  @Nullable
  public Reader getReader (@Nonnull final Charset aCharset)
  {
    final InputStream aIS = getInputStream ();
    return StreamHelper.createReader (aIS, aCharset);
  }

  public boolean exists ()
  {
    return true;
  }

  @Nullable
  public URL getAsURL ()
  {
    s_aLogger.warn ("Cannot convert an IFileItem to a URL: " + toString ());
    return null;
  }

  @Nullable
  public File getAsFile ()
  {
    s_aLogger.warn ("Cannot convert an IFileItem to a File: " + toString ());
    return null;
  }

  @Nonnull
  @UnsupportedOperation
  public IReadableResource getReadableCloneForPath (@Nonnull final String sPath)
  {
    throw new UnsupportedOperationException ();
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final FileItemResource rhs = (FileItemResource) o;
    return m_aFileItem.equals (rhs.m_aFileItem);
  }

  @Override
  public int hashCode ()
  {
    // We need a cached one!
    if (m_aHashCode == null)
      m_aHashCode = new HashCodeGenerator (this).append (m_aFileItem).getHashCodeObj ();
    return m_aHashCode.intValue ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (null).append ("fileItem", m_aFileItem).toString ();
  }
}

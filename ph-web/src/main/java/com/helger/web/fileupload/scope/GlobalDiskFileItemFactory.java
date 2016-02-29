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
package com.helger.web.fileupload.scope;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.CGlobal;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.scope.IScope;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.fileupload.IFileItemFactory;
import com.helger.web.fileupload.io.DiskFileItem;
import com.helger.web.fileupload.io.DiskFileItemFactory;
import com.helger.web.scope.singleton.AbstractGlobalWebSingleton;

/**
 * Wrapper around a {@link DiskFileItemFactory}, that is correctly cleaning up,
 * when the servlet context is destroyed.
 *
 * @author Philip Helger
 */
public final class GlobalDiskFileItemFactory extends AbstractGlobalWebSingleton implements IFileItemFactory
{
  private final DiskFileItemFactory m_aFactory = new DiskFileItemFactory (CGlobal.BYTES_PER_MEGABYTE, null);

  @UsedViaReflection
  @Deprecated
  public GlobalDiskFileItemFactory ()
  {}

  @Nonnull
  public static GlobalDiskFileItemFactory getInstance ()
  {
    return getGlobalSingleton (GlobalDiskFileItemFactory.class);
  }

  @Override
  protected void onDestroy (@Nonnull final IScope aScopeInDestruction)
  {
    m_aFactory.deleteAllTemporaryFiles ();
  }

  public void setRepository (@Nullable final File aRepository)
  {
    m_aFactory.setRepository (aRepository);
  }

  @Nonnull
  public DiskFileItem createItem (final String sFieldName,
                                  @Nullable final String sContentType,
                                  final boolean bIsFormField,
                                  @Nullable final String sFileName)
  {
    return m_aFactory.createItem (sFieldName, sContentType, bIsFormField, sFileName);
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <File> getAllTemporaryFiles ()
  {
    return m_aFactory.getAllTemporaryFiles ();
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ()).append ("factory", m_aFactory).toString ();
  }
}

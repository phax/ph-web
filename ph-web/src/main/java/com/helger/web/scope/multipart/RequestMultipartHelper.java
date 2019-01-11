/**
 * Copyright (C) 2014-2019 Philip Helger (www.helger.com)
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
package com.helger.web.scope.multipart;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.collection.multimap.IMultiMapListBased;
import com.helger.collection.multimap.MultiHashMapArrayListBased;
import com.helger.commons.CGlobal;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.lang.ServiceLoaderHelper;
import com.helger.commons.state.EChange;
import com.helger.servlet.mock.MockHttpServletRequest;
import com.helger.servlet.request.RequestHelper;
import com.helger.web.CWeb;
import com.helger.web.fileupload.IFileItem;
import com.helger.web.fileupload.IFileItemFactoryProviderSPI;
import com.helger.web.fileupload.exception.FileUploadException;
import com.helger.web.fileupload.servlet.ServletFileUpload;
import com.helger.web.progress.IProgressListener;
import com.helger.web.progress.ProgressListenerProvider;

public final class RequestMultipartHelper
{
  /**
   * The maximum size of a single file (in bytes) that will be handled. May not
   * be larger than 2 GB as browsers cannot handle more than 2GB. See e.g.
   * http://www.motobit.com/help/ScptUtl/pa33.htm or
   * https://bugzilla.mozilla.org/show_bug.cgi?id=215450 Extensive analysis:
   * <a href=
   * "http://tomcat.10.n6.nabble.com/Problems-uploading-huge-files-gt-2GB-to-Tomcat-app-td4730850.html"
   * >here</a>
   */
  public static final long MAX_REQUEST_SIZE = 5 * CGlobal.BYTES_PER_GIGABYTE;

  private static final Logger LOGGER = LoggerFactory.getLogger (RequestMultipartHelper.class);
  private static final IFileItemFactoryProviderSPI s_aFIFP;

  static
  {
    IFileItemFactoryProviderSPI aFIFP = ServiceLoaderHelper.getFirstSPIImplementation (IFileItemFactoryProviderSPI.class);
    if (aFIFP != null)
    {
      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Using custom IFileItemFactoryProviderSPI " + aFIFP);
    }
    else
      aFIFP = GlobalDiskFileItemFactory::getInstance;
    s_aFIFP = aFIFP;
  }

  private RequestMultipartHelper ()
  {}

  /**
   * @param aHttpRequest
   *        Source HTTP request from which multipart/form-data (aka file
   *        uploads) should be extracted.
   * @param aConsumer
   *        A consumer that takes either {@link IFileItem} or
   *        {@link IFileItem}[] or {@link String} or {@link String}[].
   * @return {@link EChange#CHANGED} if something was added
   */
  @Nonnull
  public static EChange handleMultipartFormData (@Nonnull final HttpServletRequest aHttpRequest,
                                                 @Nonnull final BiConsumer <String, Object> aConsumer)
  {
    if (aHttpRequest instanceof MockHttpServletRequest)
    {
      // First check, because some of the contained methods throw
      // UnsupportedOperationExceptions
      return EChange.UNCHANGED;
    }

    if (!RequestHelper.isMultipartFormDataContent (aHttpRequest))
    {
      // It's not a multipart request
      return EChange.UNCHANGED;
    }

    // It is a multipart request!
    // Note: this handles only POST parameters!
    boolean bAddedFileUploadItems = false;

    try
    {
      // Setup the ServletFileUpload....
      final ServletFileUpload aUpload = new ServletFileUpload (s_aFIFP.getFileItemFactory ());
      aUpload.setSizeMax (MAX_REQUEST_SIZE);
      aUpload.setHeaderEncoding (CWeb.CHARSET_REQUEST_OBJ.name ());
      final IProgressListener aProgressListener = ProgressListenerProvider.getProgressListener ();
      if (aProgressListener != null)
        aUpload.setProgressListener (aProgressListener);

      try
      {
        aHttpRequest.setCharacterEncoding (CWeb.CHARSET_REQUEST_OBJ.name ());
      }
      catch (final UnsupportedEncodingException ex)
      {
        if (LOGGER.isErrorEnabled ())
          LOGGER.error ("Failed to set request character encoding to '" + CWeb.CHARSET_REQUEST_OBJ.name () + "'",
                           ex);
      }

      // Group all items with the same name together
      final IMultiMapListBased <String, String> aFormFields = new MultiHashMapArrayListBased <> ();
      final IMultiMapListBased <String, IFileItem> aFormFiles = new MultiHashMapArrayListBased <> ();
      final ICommonsList <IFileItem> aFileItems = aUpload.parseRequest (aHttpRequest);
      for (final IFileItem aFileItem : aFileItems)
      {
        if (aFileItem.isFormField ())
        {
          // We need to explicitly use the charset, as by default only the
          // charset from the content type is used!
          aFormFields.putSingle (aFileItem.getFieldName (), aFileItem.getString (CWeb.CHARSET_REQUEST_OBJ));
        }
        else
          aFormFiles.putSingle (aFileItem.getFieldName (), aFileItem);
      }

      // set all form fields
      for (final Map.Entry <String, ICommonsList <String>> aEntry : aFormFields.entrySet ())
      {
        // Convert list of String to value (String or String[])
        final ICommonsList <String> aValues = aEntry.getValue ();
        final Object aValue = aValues.size () == 1 ? aValues.getFirst () : ArrayHelper.newArray (aValues, String.class);
        aConsumer.accept (aEntry.getKey (), aValue);
      }

      // set all form files (potentially overwriting form fields with the same
      // name)
      for (final Map.Entry <String, ICommonsList <IFileItem>> aEntry : aFormFiles.entrySet ())
      {
        // Convert list of String to value (IFileItem or IFileItem[])
        final ICommonsList <IFileItem> aValues = aEntry.getValue ();
        final Object aValue = aValues.size () == 1 ? aValues.getFirst ()
                                                   : ArrayHelper.newArray (aValues, IFileItem.class);
        aConsumer.accept (aEntry.getKey (), aValue);
      }

      // Parsing complex file upload succeeded -> do not use standard scan for
      // parameters
      bAddedFileUploadItems = true;
    }
    catch (final FileUploadException ex)
    {
      if (!StreamHelper.isKnownEOFException (ex.getCause ()))
        LOGGER.error ("Error parsing multipart request content", ex);
    }
    catch (final RuntimeException ex)
    {
      LOGGER.error ("Error parsing multipart request content", ex);
    }
    return EChange.valueOf (bAddedFileUploadItems);
  }
}

/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
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

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.CGlobal;
import com.helger.base.array.ArrayHelper;
import com.helger.base.io.stream.StreamHelper;
import com.helger.base.spi.ServiceLoaderHelper;
import com.helger.base.state.EChange;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsMap;
import com.helger.servlet.SafeHttpServletRequest;
import com.helger.servlet.mock.MockHttpServletRequest;
import com.helger.servlet.request.RequestHelper;
import com.helger.web.CWeb;
import com.helger.web.fileupload.IFileItem;
import com.helger.web.fileupload.IFileItemFactoryProviderSPI;
import com.helger.web.fileupload.exception.FileUploadException;
import com.helger.web.fileupload.servlet.ServletFileUpload;
import com.helger.web.progress.IProgressListener;
import com.helger.web.progress.ProgressListenerProvider;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestMultipartHelper
{
  /**
   * The default maximum size of a single upload request (in bytes). It can be changed via
   * {@link #setMaxRequestSize(long)}.
   */
  public static final long MAX_REQUEST_SIZE = 100 * CGlobal.BYTES_PER_MEGABYTE;

  /**
   * The default maximum number of parts (files and form fields) of a single upload request. It can
   * be changed via {@link #setMaxPartCount(long)}.
   *
   * @since 11.5.0
   */
  public static final long DEFAULT_MAX_PART_COUNT = 1_000;

  private static final Logger LOGGER = LoggerFactory.getLogger (RequestMultipartHelper.class);
  private static final AtomicLong MAX_REQUEST_SIZE_CONFIGURED = new AtomicLong (MAX_REQUEST_SIZE);
  private static final AtomicLong MAX_PART_COUNT = new AtomicLong (DEFAULT_MAX_PART_COUNT);
  private static final IFileItemFactoryProviderSPI PROVIDER;

  static
  {
    IFileItemFactoryProviderSPI aFIFP = ServiceLoaderHelper.getFirstSPIImplementation (IFileItemFactoryProviderSPI.class);
    if (aFIFP != null)
    {
      LOGGER.info ("Using custom IFileItemFactoryProviderSPI " + aFIFP);
    }
    else
      aFIFP = GlobalDiskFileItemFactory::getInstance;
    PROVIDER = aFIFP;
  }

  private RequestMultipartHelper ()
  {}

  /**
   * @return The maximum size of a single upload request in bytes. A value &lt; 0 means unlimited.
   *         Default is {@link #MAX_REQUEST_SIZE}.
   * @since 11.5.0
   */
  public static long getMaxRequestSize ()
  {
    return MAX_REQUEST_SIZE_CONFIGURED.get ();
  }

  /**
   * Set the maximum size of a single upload request.
   *
   * @param nMaxRequestSize
   *        The maximum size in bytes. A value &lt; 0 means unlimited.
   * @since 11.5.0
   */
  public static void setMaxRequestSize (final long nMaxRequestSize)
  {
    MAX_REQUEST_SIZE_CONFIGURED.set (nMaxRequestSize);
  }

  /**
   * @return The maximum number of parts (files and form fields) of a single upload request. A value
   *         &le; 0 means unlimited. Default is {@link #DEFAULT_MAX_PART_COUNT}.
   * @since 11.5.0
   */
  public static long getMaxPartCount ()
  {
    return MAX_PART_COUNT.get ();
  }

  /**
   * Set the maximum number of parts (files and form fields) of a single upload request. Requests
   * with more parts are not processed.
   *
   * @param nMaxPartCount
   *        The maximum number of parts. A value &le; 0 means unlimited.
   * @since 11.5.0
   */
  public static void setMaxPartCount (final long nMaxPartCount)
  {
    MAX_PART_COUNT.set (nMaxPartCount);
  }

  /**
   * Parse the provided servlet request as multipart, if the Content-Type starts with
   * <code>multipart/form-data</code>.
   *
   * @param aHttpRequest
   *        Source HTTP request from which multipart/form-data (aka file uploads) should be
   *        extracted.
   * @param aConsumer
   *        A consumer that takes either {@link IFileItem} or {@link IFileItem}[] or {@link String}
   *        or {@link String}[].
   * @return {@link EChange#CHANGED} if something was added
   */
  @NonNull
  public static EChange handleMultipartFormData (@NonNull final HttpServletRequest aHttpRequest,
                                                 @NonNull final BiConsumer <String, Object> aConsumer)
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

    final SafeHttpServletRequest aSafeHttpRequest = SafeHttpServletRequest.wrap (aHttpRequest);

    // It is a multipart request!
    // Note: this handles only POST parameters!
    boolean bAddedFileUploadItems = false;
    try
    {
      // Setup the ServletFileUpload....
      final ServletFileUpload aUpload = new ServletFileUpload (PROVIDER.getFileItemFactory ());
      aUpload.setSizeMax (getMaxRequestSize ());
      aUpload.setFileCountMax (getMaxPartCount ());
      aUpload.setHeaderEncoding (CWeb.CHARSET_REQUEST_OBJ.name ());
      final IProgressListener aProgressListener = ProgressListenerProvider.getProgressListener ();
      if (aProgressListener != null)
        aUpload.setProgressListener (aProgressListener);

      aSafeHttpRequest.setCharacterEncoding (CWeb.CHARSET_REQUEST_OBJ);

      // Group all items with the same name together
      final ICommonsMap <String, ICommonsList <String>> aFormFields = new CommonsHashMap <> ();
      final ICommonsMap <String, ICommonsList <IFileItem>> aFormFiles = new CommonsHashMap <> ();
      final ICommonsList <IFileItem> aFileItems = aUpload.parseRequest (aSafeHttpRequest);
      for (final IFileItem aFileItem : aFileItems)
      {
        if (aFileItem.isFormField ())
        {
          // We need to explicitly use the charset, as by default only the
          // charset from the content type is used!
          aFormFields.computeIfAbsent (aFileItem.getFieldName (), k -> new CommonsArrayList <> ())
                     .add (aFileItem.getString (CWeb.CHARSET_REQUEST_OBJ));
          // The value was extracted - the item itself is no longer needed
          aFileItem.delete ();
        }
        else
          aFormFiles.computeIfAbsent (aFileItem.getFieldName (), k -> new CommonsArrayList <> ()).add (aFileItem);
      }
      // set all form fields
      for (final Map.Entry <String, ICommonsList <String>> aEntry : aFormFields.entrySet ())
      {
        // Convert list of String to value (String or String[])
        final ICommonsList <String> aValues = aEntry.getValue ();
        final Object aValue = aValues.size () == 1 ? aValues.getFirstOrNull ()
                                                   : ArrayHelper.createArray (aValues, String.class);
        aConsumer.accept (aEntry.getKey (), aValue);
      }
      // set all form files (potentially overwriting form fields with the same
      // name)
      for (final Map.Entry <String, ICommonsList <IFileItem>> aEntry : aFormFiles.entrySet ())
      {
        // Convert list of String to value (IFileItem or IFileItem[])
        final ICommonsList <IFileItem> aValues = aEntry.getValue ();
        final Object aValue = aValues.size () == 1 ? aValues.getFirstOrNull ()
                                                   : ArrayHelper.createArray (aValues, IFileItem.class);
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

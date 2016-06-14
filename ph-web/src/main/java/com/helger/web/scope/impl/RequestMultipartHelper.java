package com.helger.web.scope.impl;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.CGlobal;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.attr.IMutableAttributeContainerAny;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.collection.multimap.IMultiMapListBased;
import com.helger.commons.collection.multimap.MultiHashMapArrayListBased;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.lang.ServiceLoaderHelper;
import com.helger.web.CWeb;
import com.helger.web.fileupload.IFileItem;
import com.helger.web.fileupload.IFileItemFactoryProviderSPI;
import com.helger.web.fileupload.exception.FileUploadException;
import com.helger.web.fileupload.servlet.ServletFileUpload;
import com.helger.web.mock.MockHttpServletRequest;
import com.helger.web.progress.IProgressListener;
import com.helger.web.progress.ProgressListenerProvider;
import com.helger.web.scope.singleton.GlobalDiskFileItemFactory;
import com.helger.web.servlet.request.RequestHelper;

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

  private static final Logger s_aLogger = LoggerFactory.getLogger (RequestMultipartHelper.class);
  private static final IFileItemFactoryProviderSPI s_aFIFP;

  static
  {
    IFileItemFactoryProviderSPI aFIFP = ServiceLoaderHelper.getFirstSPIImplementation (IFileItemFactoryProviderSPI.class);
    if (aFIFP != null)
      s_aLogger.info ("Using custom IFileItemFactoryProviderSPI " + aFIFP);
    else
      aFIFP = () -> GlobalDiskFileItemFactory.getInstance ();
    s_aFIFP = aFIFP;
  }

  private RequestMultipartHelper ()
  {}

  public static boolean handleMultipart (@Nonnull final HttpServletRequest aHttpRequest,
                                         @Nonnull final IMutableAttributeContainerAny <String> aTargetContainer)
  {
    // check file uploads
    // Note: this handles only POST parameters!
    boolean bAddedFileUploadItems = false;

    // Is it a multipart request at all?
    if (RequestHelper.isMultipartContent (aHttpRequest) && !(aHttpRequest instanceof MockHttpServletRequest))
    {
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
          s_aLogger.error ("Failed to set request character encoding to '" +
                           CWeb.CHARSET_REQUEST_OBJ.name () +
                           "'",
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
          final Object aValue = aValues.size () == 1 ? aValues.getFirst ()
                                                     : ArrayHelper.newArray (aValues, String.class);
          aTargetContainer.setAttribute (aEntry.getKey (), aValue);
        }

        // set all form files (potentially overwriting form fields with the same
        // name)
        for (final Map.Entry <String, ICommonsList <IFileItem>> aEntry : aFormFiles.entrySet ())
        {
          // Convert list of String to value (IFileItem or IFileItem[])
          final ICommonsList <IFileItem> aValues = aEntry.getValue ();
          final Object aValue = aValues.size () == 1 ? aValues.getFirst ()
                                                     : ArrayHelper.newArray (aValues, IFileItem.class);
          aTargetContainer.setAttribute (aEntry.getKey (), aValue);
        }

        // Parsing complex file upload succeeded -> do not use standard scan for
        // parameters
        bAddedFileUploadItems = true;
      }
      catch (final FileUploadException ex)
      {
        if (!StreamHelper.isKnownEOFException (ex.getCause ()))
          s_aLogger.error ("Error parsing multipart request content", ex);
      }
      catch (final RuntimeException ex)
      {
        s_aLogger.error ("Error parsing multipart request content", ex);
      }
    }
    return bAddedFileUploadItems;
  }
}

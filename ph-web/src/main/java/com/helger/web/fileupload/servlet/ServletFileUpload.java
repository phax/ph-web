/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.web.fileupload.servlet;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.web.fileupload.IFileItem;
import com.helger.web.fileupload.IFileItemFactory;
import com.helger.web.fileupload.IFileItemIterator;
import com.helger.web.fileupload.exception.FileUploadException;
import com.helger.web.fileupload.parse.FileUpload;

/**
 * <p>
 * High level API for processing file uploads.
 * </p>
 * <p>
 * This class handles multiple files per single HTML widget, sent using
 * <code>multipart/mixed</code> encoding type, as specified by
 * <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>. Use
 * {@link #parseRequest(HttpServletRequest)} to acquire a list of
 * {@link com.helger.web.fileupload.IFileItem}s associated with a given HTML
 * widget.
 * </p>
 * <p>
 * How the data for individual parts is stored is determined by the factory used
 * to create them; a given part may be in memory, on disk, or somewhere else.
 * </p>
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @author Sean C. Sullivan
 * @version $Id: ServletFileUpload.java 479484 2006-11-27 01:06:53Z jochen $
 */
public class ServletFileUpload extends FileUpload
{
  /**
   * Constructs an instance of this class which uses the supplied factory to
   * create <code>FileItem</code> instances.
   *
   * @param aFileItemFactory
   *        The factory to use for creating file items.
   */
  public ServletFileUpload (@Nonnull final IFileItemFactory aFileItemFactory)
  {
    super (aFileItemFactory);
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
   * compliant <code>multipart/form-data</code> stream.
   *
   * @param aHttpRequest
   *        The servlet request to be parsed.
   * @return A list of <code>FileItem</code> instances parsed from the request,
   *         in the order that they were transmitted.
   * @throws FileUploadException
   *         if there are problems reading/parsing the request or storing files.
   */
  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <IFileItem> parseRequest (@Nonnull final HttpServletRequest aHttpRequest) throws FileUploadException
  {
    return super.parseRequest (new ServletRequestContext (aHttpRequest));
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
   * compliant <code>multipart/form-data</code> stream.
   *
   * @param aHttpRequest
   *        The servlet request to be parsed.
   * @return An iterator to instances of <code>FileItemStream</code> parsed from
   *         the request, in the order that they were transmitted.
   * @throws FileUploadException
   *         if there are problems reading/parsing the request or storing files.
   * @throws IOException
   *         An I/O error occurred. This may be a network error while
   *         communicating with the client or a problem while storing the
   *         uploaded content.
   */
  @Nonnull
  public IFileItemIterator getItemIterator (@Nonnull final HttpServletRequest aHttpRequest) throws FileUploadException,
                                                                                            IOException
  {
    return super.getItemIterator (new ServletRequestContext (aHttpRequest));
  }
}

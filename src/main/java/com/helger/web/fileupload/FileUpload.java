/**
 * Copyright (C) 2006-2014 phloc systems (www.phloc.com)
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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

/**
 * <p>
 * High level API for processing file uploads.
 * </p>
 * <p>
 * This class handles multiple files per single HTML widget, sent using
 * <code>multipart/mixed</code> encoding type, as specified by <a
 * href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.
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
 * @version $Id: FileUpload.java 479484 2006-11-27 01:06:53Z jochen $
 */
public class FileUpload extends AbstractFileUploadBase
{
  /**
   * The factory to use to create new form items.
   */
  private final IFileItemFactory m_aFileItemFactory;

  /**
   * Constructs an instance of this class which uses the supplied factory to
   * create <code>FileItem</code> instances.
   * 
   * @param fileItemFactory
   *        The factory to use for creating file items.
   */
  public FileUpload (final IFileItemFactory fileItemFactory)
  {
    super ();
    m_aFileItemFactory = fileItemFactory;
  }

  // ----------------------------------------------------- Property accessors

  /**
   * Returns the factory class used when creating file items.
   * 
   * @return The factory class for new file items.
   */
  @Override
  public IFileItemFactory getFileItemFactory ()
  {
    return m_aFileItemFactory;
  }
}

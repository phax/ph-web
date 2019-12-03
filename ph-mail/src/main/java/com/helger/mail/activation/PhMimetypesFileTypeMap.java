/**
 * Copyright (C) 2016-2019 Philip Helger (www.helger.com)
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
package com.helger.mail.activation;

import javax.activation.MimetypesFileTypeMap;

/**
 * A special file type map, that contains all entries from the
 * "META-INF/mime.types" file in this project. This is basically to work around
 * a class loader issue. By using this class, the classloader of this class is
 * used and therefore the correct "META-INF/mime.types" will be loaded.
 *
 * @author Philip Helger
 * @since 9.1.5
 */
public class PhMimetypesFileTypeMap extends MimetypesFileTypeMap
{
  public PhMimetypesFileTypeMap ()
  {
    // empty
  }
}

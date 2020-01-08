/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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
package com.helger.servlet.spec;

import javax.servlet.ServletContext;

/**
 * Dummy interface with all ServletContext default methods for new methods in
 * Servlet Spec 3.1.0 compared to 3.0.0
 *
 * @author Philip Helger
 */
public interface IServletContext300To310Migration extends ServletContext
{
  default String getVirtualServerName ()
  {
    throw new UnsupportedOperationException ();
  }
}

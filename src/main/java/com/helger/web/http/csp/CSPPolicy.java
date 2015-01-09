/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.http.csp;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.helger.commons.IHasStringRepresentation;
import com.helger.commons.ValueEnforcer;

/**
 * CSP policy. See http://www.w3.org/TR/CSP/
 * 
 * @author Philip Helger
 */
public class CSPPolicy implements IHasStringRepresentation
{
  private final List <CSPDirective> m_aList = new ArrayList <CSPDirective> ();

  public CSPPolicy ()
  {}

  @Nonnegative
  public int getDirectiveCount ()
  {
    return m_aList.size ();
  }

  @Nonnull
  public CSPPolicy addDirective (@Nonnull final CSPDirective aDirective)
  {
    ValueEnforcer.notNull (aDirective, "Directive");
    m_aList.add (aDirective);
    return this;
  }

  @Nonnull
  public String getAsString ()
  {
    final StringBuilder aSB = new StringBuilder ();
    for (final CSPDirective aDirective : m_aList)
    {
      if (aSB.length () > 0)
        aSB.append ("; ");
      aSB.append (aDirective.getAsString ());
    }
    return aSB.toString ();
  }
}

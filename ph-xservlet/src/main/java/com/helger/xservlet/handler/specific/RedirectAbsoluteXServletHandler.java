/**
 * Copyright (C) 2016-2018 Philip Helger (www.helger.com)
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
package com.helger.xservlet.handler.specific;

import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.url.ISimpleURL;
import com.helger.commons.url.SimpleURL;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xservlet.handler.simple.IXServletSimpleHandler;

/**
 * An {@link IXServletSimpleHandler} that does a redirect to a fixed URL.
 *
 * @author Philip Helger
 */
public class RedirectAbsoluteXServletHandler implements IXServletSimpleHandler
{
  private final ISimpleURL m_aTargetURL;

  /**
   * Constructor.
   *
   * @param aTargetURL
   *        The URL to redirect to. Is interpreted as an absolute URL.
   */
  public RedirectAbsoluteXServletHandler (@Nonnull final ISimpleURL aTargetURL)
  {
    ValueEnforcer.notNull (aTargetURL, "aTargetURL");

    m_aTargetURL = aTargetURL;
  }

  public void handleRequest (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                             @Nonnull final UnifiedResponse aUnifiedResponse) throws Exception
  {
    final SimpleURL aTargetURL = new SimpleURL (m_aTargetURL);

    // Add all parameters
    for (final Map.Entry <String, Object> aEntry : aRequestScope.params ().entrySet ())
    {
      final String sKey = aEntry.getKey ();
      final Object aValue = aEntry.getValue ();
      if (aValue instanceof String)
        aTargetURL.add (sKey, (String) aValue);
      else
        if (aValue instanceof String [])
          for (final String sValue : (String []) aValue)
            aTargetURL.add (sKey, sValue);
    }
    aUnifiedResponse.setRedirect (aTargetURL.getAsStringWithEncodedParameters ());
  }
}

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
package com.helger.web.proxy.autoconf.functest;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;

import com.helger.commons.io.streams.NonBlockingByteArrayOutputStream;
import com.helger.commons.io.streams.StreamUtils;

public class MainProxyServerTestCall
{
  public static void main (final String [] args) throws Exception
  {
    final SocketAddress addr = new InetSocketAddress ("localhost", 8080);
    final Proxy proxy = new Proxy (Proxy.Type.HTTP, addr);

    final URL url = new URL ("http://java.sun.com/");
    final URLConnection conn = url.openConnection (proxy);
    System.out.println (conn);
    final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ();
    StreamUtils.copyInputStreamToOutputStream (conn.getInputStream (), aBAOS);
    System.out.println (aBAOS.toString ());
  }
}

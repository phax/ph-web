/*
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
// Source: http://www.returnproxy.com/proxypac/index.php?option=com_content&view=article&id=59&Itemid=88

function FindProxyForURL(url, host) {
 
var pacver = "company.net PAC file version 7.2, January 4th, 2008";
 
// Convert everything to lower case.
var lhost = host.toLowerCase();
host = lhost;
 
// Set the default proxy variable that users get if they don�t match
// any more specific rule. 
 
var proxy = "PROXY coreproxy.company.net:8000";
// Los Angeles WAN subnets go to LA proxy 
if (isInNet(myIpAddress(), "10.100.0.0", "255.252.0.0")) { 
     proxy = "PROXY laproxy.company.net:8000";
}
 
// New York WAN subnets go to NY proxy 
if (isInNet(myIpAddress(), "10.200.0.0", "255.252.0.0")) { 
     proxy = "PROXY nyproxy.company.net:8000";
}
 
// Lab subnet goes to it�s own proxy 
if (isInNet(myIpAddress(), "10.100.265.0", "255.255.255.0")) { 
     proxy = "PROXY labproxy.company.net:8000";
}
 
// Spyware blocks 
 
if (dnsDomainIs(host, ".badspyware1.com") ||
    dnsDomainIs(host, ".worsespyware2.com")) {
      return "PROXY 127.0.0.1:49234";
}
 
// Loopback and localhost goes browser direct always. 
 
if ((host == "localhost") ||
   (shExpMatch(host, "localhost.*")) ||
   (host == "127.0.0.1")) {
      return "DIRECT";
}
 
// Trading Floor Override for Bloomberg
if (isInNet(myIpAddress(), "10.101.32.0", "255.255.252.0")) {
    if (dnsDomainIs(host, "Bloomberg.com")) {
         return "DIRECT";
    }
}
 
// ibm.com doesn�t work through LA, set the variable to core proxy. 
// Note you could just return the core proxy here, too but setting the variable is
// more consistent. 
 
if ((dnsDomainIs(host, ".ibm.com")) && 
    (proxy == "PROXY laproxy.company.net:8000")) {
        proxy = "PROXY coreproxy.company.net:8000";
}
 
// Utility 
if ((host =="proxyinfo.company.net")) {
alert("Local IP address is: " + myIpAddress());
alert("PAC File Version:  " + pacver);
}
 
// hosts in company.net on the Internet need to be proxied. 
 
if ((host == "www.company.net") ||
   (host == "webmail.company.net") ||
   (host == "portal.company.net") ||
   (dnsDomainIs(host, ".public.company.net"))) {
       return proxy;
}
 
// Test to see if host is an IP address
var reip = /^\d+\.\d+\.\d+\.\d+$/g;
if (reip.test(host)) {
 
     // Check for an Internet DMZ address
     if (isInNet(host, "10.250.0.0", "255.255.0.0")) {
          return proxy;
     }
 
    // Check for an internal 10.x IP address
     if (isInNet(host, "10.0.0.0", "255.0.0.0")) {
          return "DIRECT";
     }
}
 
// Next, we want to send all traffic to company.net browser direct
if (dnsDomainIs(host, ".company.net")) {
     return "DIRECT";
}
 
// Default return condition is the proxy, since it�s assumed that everything
// else is on the Internet.
 
return proxy;
 
} // End of function
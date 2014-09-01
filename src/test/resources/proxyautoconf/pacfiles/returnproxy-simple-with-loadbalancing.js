/*
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
// Source: http://www.returnproxy.com/proxypac/index.php?option=com_content&view=article&id=63&Itemid=104

function FindProxyForURL(url, host) {

//  Load Balancing Code

// Find the 4th octet
var myip=myIpAddress()
var ipbits=myip.split(".")
var myseg=parseInt(ipbits[3])

// Check to see if the 4th octect is even or odd
if (myseg==Math.floor(myseg/2)*2) {
     // Even
     proxy = "PROXY p1.company.com:8080; PROXY p2.company.com:8080";
}
else {
     // Odd
     proxy = "PROXY p2.company.com:8080; PROXY p1.company.com:8080";
}
 
// First start with the exceptions that need to be proxied
 
if ((host == "www.company.net") ||
    (host == "webmail.company.net") ||
    (host == "portal.company.net") ||
    (dnsDomainIs(host, ".public.company.net"))) {
         return proxy;
}
 
// Next, we want to send all traffic to company.com browser direct
 
if (dnsDomainIs(host, ".company.net")) {
       return "DIRECT";
}
 
// Default return condition is the proxy, since it�s assumed that everything
// else is on the Internet.
 
return proxy;
 
} // End of function
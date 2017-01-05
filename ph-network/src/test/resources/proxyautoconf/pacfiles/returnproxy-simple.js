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
// Source: http://www.returnproxy.com/proxypac/index.php?option=com_content&view=article&id=58&Itemid=87

function FindProxyForURL(url, host) {
// First start with the exceptions that need to be proxied
if ((host == "www.company.net") ||
    (host == "webmail.company.net") ||
    (host == "portal.company.net") ||
    (dnsDomainIs(host, ".public.company.net"))) {
         return "PROXY proxy1.company.net:8000";
}

// Next, we want to send all traffic to company.net browser direct
if (dnsDomainIs(host, ".company.net")) {
       return "DIRECT";
}

// Default return condition is the proxy, since it�s assumed that everything
// else is on the Internet.
return "PROXY proxy1.company.net:8000";
} // End of function

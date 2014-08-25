/*
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
function FindProxyForURL(url, host) {
   // Adressen, die auf example.com liegen, brauchen keinen Proxy:
   if (shExpMatch(url,"*.example.com/*"))                  {return "DIRECT";}
   if (shExpMatch(url, "*.example.com:*/*"))               {return "DIRECT";}
 
   // URLs innerhalb dieses Netzwerkes werden abgefragt �ber
   // Port 8080 auf fastproxy.example.com: (macht Nameserver Anfrage)
   if (isInNet(host, "10.0.0.0", "255.255.248.0")) {
      return "PROXY fastproxy.example.com:8080";
   }
 
   // Alle anderen Anfragen gehen �ber Port 8000 von proxy.example.com.
   // sollte das fehlschlagen, verbinde direkt ins Netz:
   return "PROXY proxy.example.com:8000; DIRECT";
}

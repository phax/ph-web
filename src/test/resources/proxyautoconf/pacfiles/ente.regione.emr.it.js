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
WPAD:
function FindProxyForURL(url, host)
{
  if(isPlainHostName(host) ||
     dnsDomainIs(host,"regione.emilia-romagna.it") &&
     !localHostOrDomainIs(host, "bbcc.ibc.rer.it") && 
     !localHostOrDomainIs(host, "archivi.ibc.rer.it") &&
     !localHostOrDomainIs(host, "ufficistampa.rer.it") &&
     !localHostOrDomainIs(host, "salastampa.rer.it") ||  
     dnsDomainIs(host,"dyned.com") ||
     dnsDomainIs(host,"sitar-er.it") ||
           dnsDomainIs(host,"galiano.it") || 
           dnsDomainIs(host,"e-familybnl.it") ||
     dnsDomainIs(host,"sian.it") ||
     dnsDomainIs(host,"ervet.it") ||
     dnsDomainIs(host,"porretta.redirectme.net") ||
     dnsDomainIs(host,"nxtlab.mediamind.it") ||
     dnsDomainIs(host,"prefettura.it") ||
     dnsDomainIs(host,"sp.fondazionezancan.it") ||
     dnsDomainIs(host,"servizi.inps.it") ||
     localHostOrDomainIs(host,"sinitweb.tesoro.it") ||
     localHostOrDomainIs(host,"rgs.tesoro.it") ||
     localHostOrDomainIs(host,"www.forumpa.it") ||
     localHostOrDomainIs(host,"www.agronica.it") ||
           localHostOrDomainIs(host,"sharepoint.unibo.edu.ar") ||
           localHostOrDomainIs(host,"ftp.webscience.it") ||
     shExpMatch(url,"https://195.62.160.214:8181/*") ||
     isInNet(host,"89.97.58.0","255.255.255.0") ||
     isInNet(host,"10.0.0.0","255.0.0.0") ||
     isInNet(host,"193.43.192.0","255.255.240.0") ||
      isInNet(host,"127.0.0.0","255.0.0.0") ||
     dnsDomainIs(host,"ente.regione.emr.it") )
    return "DIRECT";
         else return "PROXY 193.43.193.100:3128; PROXY 193.43.193.101:3128";
}


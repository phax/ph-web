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
function atoi(charstring)
{

        if ( charstring == "a" ) return 0x61; if ( charstring == "b" ) return 0x62;
        if ( charstring == "c" ) return 0x63; if ( charstring == "d" ) return 0x64;
        if ( charstring == "e" ) return 0x65; if ( charstring == "f" ) return 0x66;
        if ( charstring == "g" ) return 0x67; if ( charstring == "h" ) return 0x68;
        if ( charstring == "i" ) return 0x69; if ( charstring == "j" ) return 0x6a;
        if ( charstring == "k" ) return 0x6b; if ( charstring == "l" ) return 0x6c;
        if ( charstring == "m" ) return 0x6d; if ( charstring == "n" ) return 0x6e;
        if ( charstring == "o" ) return 0x6f; if ( charstring == "p" ) return 0x70;
        if ( charstring == "q" ) return 0x71; if ( charstring == "r" ) return 0x72;
        if ( charstring == "s" ) return 0x73; if ( charstring == "t" ) return 0x74;
        if ( charstring == "u" ) return 0x75; if ( charstring == "v" ) return 0x76;
        if ( charstring == "w" ) return 0x77; if ( charstring == "x" ) return 0x78;
        if ( charstring == "y" ) return 0x79; if ( charstring == "z" ) return 0x7a;
        if ( charstring == "0" ) return 0x30; if ( charstring == "1" ) return 0x31;
        if ( charstring == "2" ) return 0x32; if ( charstring == "3" ) return 0x33;
        if ( charstring == "4" ) return 0x34; if ( charstring == "5" ) return 0x35;
        if ( charstring == "6" ) return 0x36; if ( charstring == "7" ) return 0x37;
        if ( charstring == "8" ) return 0x38; if ( charstring == "9" ) return 0x39;
        if ( charstring == "." ) return 0x2e;
        return 0x20;
}

function URLhash(name)
{
var  cnt=0;
        var str=name.toLowerCase(name);
        if ( str.length ==0) {
                return cnt;
        }
        for(var i=0;i < str.length ; i++) {
           var ch= atoi(str.substring(i,i + 1));
                cnt = cnt + ch;
        }

        return cnt ;
}

function FindProxyForURL(url, host)
{
        if (shExpMatch(url, "http*://elearning.bmf.gv.at*")
        ) {
          ret = URLhash(host);
          if ( (ret % 2) < 1 ) {
                  return "PROXY 172.30.9.12:8080; PROXY 172.30.9.13:8080";
          } else  {
            return "PROXY 172.30.9.13:8080; PROXY 172.30.9.12:8080";
          }    
        }

        if (dnsDomainIs(host,".bgbl.at") ||
            dnsDomainIs(host,".cna.at") ||
            dnsDomainIs(host,".schulen-online.at") 
        ) {
          return "DIRECT";
        }
        
        hostip=dnsResolve(host); 
        if (isInNet(hostip, "127.0.0.0","255.0.0.0") ||
            isInNet(hostip, "10.0.0.0", "255.0.0.0") ||
            isInNet(hostip, "172.16.0.0","255.240.0.0") ||
               isInNet(hostip, "192.168.116.0","255.255.254.0") ||
            isInNet(hostip, "192.168.80.0","255.255.240.0")
        ) {
           return "DIRECT";
        }

        ret = URLhash(host);
        if ( (ret % 2) < 1 ) {
                return "PROXY 172.30.9.12:8080; PROXY 172.30.9.13:8080";
        } else  {
                return "PROXY 172.30.9.13:8080; PROXY 172.30.9.12:8080";
        }
}

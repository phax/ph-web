// Source: http://www.bcu.ac.uk/proxy.pac
//Copyright (c) 1997-2006 Microsoft Corporation
BackupRoute="DIRECT";
UseDirectForLocal=false;
ConvertUrlToLowerCase=false;
function MakeIPs(){
}
DirectIPs=new MakeIPs();
cDirectIPs=0;
function MakeCARPExceptions(){
this[0]="*.windowsupdate.com";
this[1]="windowsupdate.microsoft.com";
this[2]="*.windowsupdate.microsoft.com";
this[3]="*.update.microsoft.com";
this[4]="download.windowsupdate.com";
this[5]="download.microsoft.com";
this[6]="*.download.windowsupdate.com";
this[7]="wustat.windows.com";
this[8]="ntservicepack.microsoft.com";
this[9]="forefrontdl.microsoft.com";
}
CARPExceptions=new MakeCARPExceptions();
cCARPExceptions=10;
function MakeNames(){
}
DirectNames=new MakeNames();
cDirectNames=0;
HttpPort="8080";
cNodes=1;
function MakeProxies(){
this[0]=new Node("10.255.52.251",241542179,1.000000);
}
Proxies = new MakeProxies();
function Node(name, hash, load){
 this.name = name;
 this.hash = hash;
 this.load = load;
 this.score = 0;
 return this;
}
function IpSubnet(ip, mask, prefix){
 this.ip = ip;
 this.mask = mask;
 this.prefix = prefix;
 var isIpV4Addr = /^(\d+.){3}\d+$/;
 this.isIpv6 = !isIpV4Addr.test(ip);
 return this;
}
var pfDnsResolve, pfMyIpAddress, pfIsInNet;
function DnsResolve(name){
 return dnsResolve(name);
}
function DnsResolveEx(name){
 return dnsResolveEx(name);
}
function IsInNet(ip, subnet){
 var isIpV4Addr = /^(\d+.){3}\d+$/;
 if(subnet.isIpv6 || !isIpV4Addr.test(ip)){
  return false;
 }
 return isInNet(ip, subnet.ip, subnet.mask);
}
function IsInNetEx(ip, subnet) {
 return isInNetEx(ip, subnet.prefix);
}
function MyIpAddress(){
 return myIpAddress();
}
function MyIpAddressEx(){
 return myIpAddressEx();
}
function ExpMatch(str, exp){
 if (ConvertUrlToLowerCase)
 {
  str = str.toLowerCase();
 }
 return shExpMatch(str, exp);
}
function FindProxyForURL(url, host){
 pfDnsResolve = DnsResolve;
 pfMyIpAddress = MyIpAddress;
 pfIsInNet = IsInNet;
 return ImplementFindProxyForURL(url, host);
}
function FindProxyForURLEx(url, host){
 pfDnsResolve = DnsResolveEx;
 pfMyIpAddress = MyIpAddressEx;
 pfIsInNet = IsInNetEx;
 return ImplementFindProxyForURL(url, host);
}
function ImplementFindProxyForURL(url, host){
 var hash=0, urllower, i, fIp=false, ip, iparray, nocarp=false, skiphost=false;
 var list="", pl, j, score, ibest, bestscore;
 urllower = url.toLowerCase();
 if((urllower.substring(0,5)=="rtsp:") ||
   (urllower.substring(0,6)=="rtspt:") ||
   (urllower.substring(0,6)=="rtspu:") ||
   (urllower.substring(0,4)=="mms:") ||
   (urllower.substring(0,5)=="mmst:") ||
   (urllower.substring(0,5)=="mmsu:"))
  return "DIRECT";
 if(UseDirectForLocal){
  if(isPlainHostName(host))
   fIp = true;}
 for(i=0; i<cDirectNames; i++){
  if(ExpMatch(host, DirectNames[i])){
   fIp = true;
   break;}
  if(ExpMatch(url, DirectNames[i]))
   return "DIRECT";
 }
 if(cDirectIPs == 0){
  if(fIp)
   return "DIRECT";}
 else{
  ip = host;
  if(fIp)
   ip = pfDnsResolve(host);
  iparray = ip.split(";");
  for(j=0; j<iparray.length; j++){
   for(i=0; i<cDirectIPs; i++){
    if(pfIsInNet(iparray[j], DirectIPs[i]))
     return "DIRECT";}}
  if(isPlainHostName(host))
   return "DIRECT";
 }
 if(cCARPExceptions > 0){
  for(i = 0; i < cCARPExceptions; i++){
   if(ExpMatch(host, CARPExceptions[i])){
    nocarp = true;}
   if(ExpMatch(url, CARPExceptions[i])){
    nocarp = true;
    skiphost = true;
    break;
 }}}
 if(!skiphost)
  hash = HashString(host,hash);
 if(nocarp)
  hash = HashString(pfMyIpAddress(), hash);
 pl = new Array();
 for(i = 0; i<cNodes; i++){
  Proxies[i].score = Proxies[i].load * Scramble(hash ^ Proxies[i].hash);
  pl[i] = i;
 }
 for(j = 0; j < cNodes; j++){
  bestscore = -1;
  for(i = 0; i < cNodes-j; i++){
   score = Proxies[pl[i]].score;
   if(score > bestscore){
    bestscore = score;
    ibest = i;
  }}
  list = list + "PROXY " + Proxies[pl[ibest]].name + ":" + HttpPort + "; ";
  pl[ibest] = pl[cNodes-j-1];
 }
 list = list + BackupRoute;
 return list;
}
var h_tbl = new Array(0,0x10D01913,0x21A03226,0x31702B35,0x4340644C,0x53907D5F,0x62E0566A,0x72304F79,0x8680C898,0x9650D18B,0xA720FABE,0xB7F0E3AD,0xC5C0ACD4,0xD510B5C7,0xE4609EF2,0xF4B087E1);
function HashString(str, h){
  for(var i=0; i<str.length; i++){
   var c = str.charAt(i);
   if(c ==':' || c == '/') break;
   c = CharToAscii(c.toLowerCase());
   h = (h >>> 4) ^ h_tbl[(h ^ c) & 15];
   h = (h >>> 4) ^ h_tbl[(h ^ (c>>>4)) & 15];
   h = MakeInt(h);
 }
 return h;
}
function Scramble(h){
 h += ((h & 0xffff) * 0x1965) + ((((h >> 16) & 0xffff) * 0x1965) << 16) + (((h & 0xffff) * 0x6253) << 16);
 h = MakeInt(h);
 h += (((h & 0x7ff) << 21) | ((h >> 11) & 0x1fffff));
 return MakeInt(h);
}
function CharToAscii(c){
 return c.charCodeAt(0);
}
function MakeInt(x){
 x %= 4294967296;
 if(x < 0)
  x += 4294967296;
 return x;
}

// Source: http://www.returnproxy.com/proxypac/index.php?option=com_content&view=article&id=63&Itemid=104

function FindProxyForURL(url, host) {

//  Load Balancing Code

// Find the 4th octet
var myip=myIpAddress()
var ipbits=myip.split(".")
var myseg=parseInt(ipbits[3])

// Check to see if the 4th octect is even or odd
var proxy;
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
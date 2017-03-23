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

// Proxy auto-configuration (WPAD) profile for net.example.com
// HTTP-only proxy with IPv4 and IPv6 profiles excluding local traffic

// Traditional
function FindProxyForURL(url, host) {

    // Unencrypted HTTP traffic
    if (url.substring(0, 5) == "http:") {

        // Local network traffic is diverted around the proxy
        if (isPlainHostName(host) ||
            !isResolvable(host) ||
            dnsDomainIs(host, ".net.example.com") ||
            isInNet(dnsResolve(host), "127.0.0.0", "255.255.255.0") ||
            isInNet(dnsResolve(host), "172.16.0.0",  "255.240.0.0") ||
            isInNet(dnsResolve(host), "192.168.0.0",  "255.255.0.0") ||
            shExpMatch(host, "*.local") ||
            localHostOrDomainIs(host, "net.example.com") ) {
            return "DIRECT";
        }

        // External traffic is routed to the proxy
        return "PROXY proxy-cache.net.example.com:3128; DIRECT";
    }

    // DEFAULT rule and encrypted HTTPS
    return "DIRECT";
}

// IPv6 extension
function FindProxyForURLEx(url, host) {

    // Unencrypted HTTP traffic
    if (url.substring(0, 5) == "http:") {

        // Local network traffic is diverted around the proxy
        if (isPlainHostName(host) ||
            !isResolvableEx(host) ||
            dnsDomainIs(host, ".net.example.com") ||
            shExpMatch(host, "*.local") ||
            localHostOrDomainIs(host, "net.example.com") ) {
            return "DIRECT";
        }

        // Loop through extended IP address list (containing both
        // IPv4 and IPv6) for more local network traffic
        var addrList=dnsResolveEx(host).split(";");
        for (var i = 0; i < addrList.length; i++) {
            if (isInNetEx(addrList[i], "::1/128") ||
                isInNetEx(addrList[i], "fe80::/10") ||
                isInNetEx(addrList[i], "fc00::/7") ||
                isInNetEx(addrList[i], "127.0.0.0/8") ||
                isInNetEx(addrList[i], "172.16.0.0/12") ||
                isInNetEx(addrList[i], "192.168.0.0/16") ) {
                return "DIRECT";
            }
        }

        // External traffic is routed to the proxy
        return "PROXY proxy-cache.net.example.com:3128; DIRECT";
    }

    // DEFAULT rule and encrypted HTTPS
    return "DIRECT";
}

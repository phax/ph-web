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

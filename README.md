#ph-web
Java library with common web stuff. It contains the following sub-projects:
  * ph-network
   * Contains general network related stuff like Authenticators, DNS helpers, port and proxy classes
  * ph-http
    * Contains special HTTP related helpers 
  * ph-httpclient
    * Contains Apache HTTP client related helpers
  * ph-mail
    * Contains javax.mail related helpers
  * ph-smtp
    * Contains helper classes for asynchronous mail (re-)sending
  * ph-web
    * Other high-level abstraction for servlets, file-upload, browser information, sitemap, web scopes etc.
  
##News

  * v8.6.2 - 2016-09-25
    * Added possibility to disable URL encoding (putting SESSION ID in URL) with WebSettings
  * v8.6.1 - 2016-09-09
    * Binds to ph-commons 8.5.x
  * v8.6.0 - 2016-08-21
    * Binds to ph-commons 8.4.x
  * v8.5.0 - 2016-07-26
    * Binds to ph-commons 8.3.x

##Maven usage
Add the following to your pom.xml to use this artifact:

```
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>ph-web</artifactId>
  <version>8.6.1</version>
</dependency>
```

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodeingStyleguide.md) |
On Twitter: <a href="https://twitter.com/philiphelger">@philiphelger</a>

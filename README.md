# ph-web

Java library with common web stuff. It contains the following sub-projects:
* ph-dns (since v9.2.1)
    * DNS client side helper methods based on dnsjava 
* ph-network
    * Contains general network related stuff like Authenticators, DNS helpers, port and proxy classes
* ph-http
    * Contains special HTTP related helpers 
* ph-useragent
    * Contains User-Agent/Browser related helpers 
* ph-servlet
    * Contains special javax.servlet related helpers 
* ph-mail
    * Contains javax.mail related helpers
* ph-smtp
    * Contains helper classes for asynchronous mail (re-)sending
* ph-httpclient
    * Contains Apache HTTP client related helpers
* ph-web
    * Other high-level abstraction for servlets, file-upload and web scopes
* ph-sitemap (since v9.0.0)
    * Containing Sitemap generation helper
* ph-xservlet (since v9.0.0)
    * Contains an extended Servlet framework (XServlet is just a name I gave it internally and not an official name)
* ph-jsch (since v9.2.0)
    * Helper around JSch - for SSH, SCP and SFTP connections  
  
# Maven usage

Add the following to your pom.xml to use this artifact, replacing `x.y.z` with the effective version number:

```xml
<dependency>
  <groupId>com.helger.web</groupId>
  <artifactId>ph-web</artifactId>
  <version>x.y.z</version>
</dependency>
```

or to use it as a BOM put that in your `<dependencyManagement>` section:

```xml
<dependency>
  <groupId>com.helger.web</groupId>
  <artifactId>ph-web-parent-pom</artifactId>
  <version>x.y.z</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

Note: prior to v9.3.0 the Maven groupId was `com.helger`.

# News and noteworthy

* v9.3.3 - work in progress
    * Updated to ph-commons 9.4.7
* v9.3.2 - 2020-08-18
    * Updated API in ph-xservlet
* v9.3.1 - 2020-07-15
    * Updated to ph-commons 9.4.6
    * Deprecated `AcceptMimeTypeHandler.safeParseMimeType`
    * Updated predefine MIME types list
    * Updated to dnsjava 3.2.2
* v9.3.0 - 2020-05-26
    * Extracted ph-dns as a separate subproject
    * Changed Maven groupId to `com.helger.web`
    * Removed deprecated methods from previous versions
* v9.2.0 - 2020-05-25
    * Updated to dnsjava 3.1.0
    * Updated to ph-commons 9.4.4 
    * Extracted ph-jsch as a separate subproject
* v9.1.12 - 2020-03-30
    * Fixed a potential dead lock when shutting down RequestTracker
* v9.1.11 - 2020-03-29
    * Updated to Apache httpclient 4.5.12
    * Updated to Jakarta Mail 1.6.5
    * Updated the default TLS configuration modes to reflect the current situation 
    * Changed `ETLSConfigurationMode` to `ETLSConfigurationMode_2020_02` to indicate the date of effectiveness
    * `LoggingLongRunningRequestCallback` now logs the remote address of the request by default
    * Updated to ph-commons 9.4.0
* v9.1.10 - 2020-02-18
    * Updated to dnsjava 3.0.0
    * All `getRequestURI` and `getRequestURL` methods were split in `...Encoded` and `...Decoded` methods for improved handling of URL encoding
* v9.1.9 - 2020-02-16
    * Extended `HttpClientSettings` with a customizable user agent string
    * Added Apache HttpClient `PrivateKeyStrategy` and `TrustStrategy` implementations
    * Extended `HttpClientSettings` with "follow redirect" setting
    * Made `HttpClientSettings` clonable
    * Simplified public `IEmailData` API for the receivers to use mutable lists
    * `XServlet` filter and handler are no longer `Serializable`
* v9.1.8 - 2020-02-14
    * Extracted `HttpClientSettings` from `HttpClientFactory`
    * Extended customization options of `HttpClientSettings`
    * Improved error resilience
* v9.1.7 - 2020-02-13
    * Added explicit `HttpClientFactory.setProxyCredentials` method
    * `HttpClientHelper.createRequest` now also supports `PATCH`
    * `XServletFilterConsistency` has now a silent mode
    * Disabled the default debug logging in the HTTP `ResponseHandler`    
* v9.1.6 - 2020-02-07
    * Updated to Apache httpcore 4.4.13
    * Updated to Apache httpclient 4.5.11
    * Enforcing commons-codec 1.14
    * Changed HttpClient to not use commons-codec but SLF4J instead
    * Improved error resilience
* v9.1.5 - 2019-12-11
    * Optimized logging
    * Updated the `META-INF/mime.types` file with the latest MIME types
    * `ServletContextPathHolder` uses the silent mode by default
    * Added new class `PhMimetypesFileTypeMap` to enforce the loading of "META-INF/mime.types" from this project
    * Removed "META-INF/mime.types" from "ph-smtp" and "ph-httpclient" projects
    * Fixed NPE in `ProxySettingsManager`
* v9.1.4 - 2019-10-08
    * Updated to Apache httpcore 4.4.12
    * Updated to Apache httpclient 4.5.10
    * Extended `UnifiedRepsonse` and `UnifiedResponseDefaultSettings` API to make HTTP header value unification and quoting customizable.
    * Updated to Jakarta dependencies
    * `AbstractXServlet.destroy` is no longer final
* v9.1.3 - 2019-08-28
    * Updated to Apache httpclient 4.5.9
    * Updated to ph-commons 9.3.6
    * Added new method `IRequestWebScopeWithoutResponse.getURI()`
    * `HttpDebugger` is disabled by default
    * `AcceptMimeTypeHandler.safeParseMimeType` now automatically tries to decode RFC 2616 encoded values
* v9.1.2 - 2019-06-05
    * Updated to Apache httpclient 4.5.8
    * Updated to ph-commons 9.3.3
    * Added new method `LocalDateTime IWebScope.getScopeCreationDateTime ()`
    * Added new class `NetworkOnlineStatusDeterminator` in `ph-network`
* v9.1.1 - 2019-02-17
    * Updated to JSch 0.1.55
    * Updated to Apache httpcore 4.4.11
    * Updated to Apache httpclient 4.5.7
    * `XServletSettings` got a setting to set the `X-Frame-Options` HTTP header with a default value of `SAMEORIGIN`
    * `XServletFilterSecurityHttpReferrerPolicy` adds the header before the request instead of afterwards
    * Removed deprecated methods in classes `RequestField` and `SessionBackedRequestField`
    * `HttpClientFactory` can now handle "non proxy hosts" as well
* v9.1.0 - 2018-11-22
    * Fixed potential NPE in `EmailAttachmentMicroTypeConverter` if the attachment cannot be read
    * By default all request param values are now also Unicode normalized
    * Added possibility to specify a custom "param value cleanser" using `RequestWebScope.setParamValueCleanser`
    * Requires ph-commons 9.2.0
* v9.0.5 - 2018-10-11
    * Changed `IContentTransferEncoding` to deliver a full codec and not just a decoder
    * Extended `NetworkPortHelper` with a remote port status checker
    * Added new package `com.helger.http.tls` with TLS version and configuration enums
* v9.0.4 - 2018-09-17
    * Added forbidden character check in request parameter value handling
* v9.0.3 - 2018-09-14
    * Added new class `AbstractXFilterUnifiedResponse`
    * `IRequestParamMap` was extended to easily retrieve trimmed values as well
    * Updated to javax.mail 1.6.2
    * Extended `CSP2SourceList` to support kyword `'none'`
    * Extended `CSP2Policy` with new default directives
    * Fixed potential stack overflow in RequestParamMap
* v9.0.2 - 2018-07-24
    * Fixed OSGI ServiceProvider configuration
    * Requires ph-commons 9.1.3
    * Fixed `XServlet` status reset in case Servlet init failed
    * Added classes around `RequestFieldDataMultiValue`
    * Updated to Apache HttpCore 4.4.10
    * Reworked the ph-network/proxy handling (in an incompatible way)
    * Added `IRequestParameter.getAsStringTrimmed`
* v9.0.1 - 2018-04-18
    * Updated to javax.mail 1.6.1
    * Removed exclusion of javax.activation
    * `AbstractXServlet` is now derived from `HttpServlet` and no longer from `GenericServlet`
    * Extended `HttpClientFactory` methods slightly
* v9.0.0 - 2017-12-20
    * Updated to javax.mail 1.6.0
    * Updated to ph-commons 9.0.0
    * Added HTTP Referrer-Policy header support
    * Extracted `ph-sitemap` from `ph-web`
    * Added new sub-project `ph-xservlet` to contain an extended Servlet framework
    * `HttpClientFactory` supports customizable retry count 
* v8.8.2 - 2017-07-04
    * Made debug flag in certain `ResponseHandler*` implementations customizable
    * `ServletHelper` caught Exception logging can now be enabled and disabled
    * Custom `HostnameVerifier` in `HttpClientFactory`
    * `HttpDebugger` has an `afterRequest` method now 
* v8.8.1 - 2017-05-29
    * Improved exception handling in HTTP client helper
* v8.8.0 - 2017-05-10
    * New HTTP servlet class hierarchy for better separation of concerns
    * Started new Servlet base infrastructure
    * Added possibility to disable DNS client caching in `HttpClient`
* v8.7.4 - 2017-03-29
    * Improved PAC support to also handle IP6 addresses and `dnsResolveEx`
    * Updated to ph-commons 8.6.3
    * Updated to ph-schedule 3.6.1
* v8.7.3 - 2017-03-02
    * API extensions
    * Added package `com.helger.servlet.logging` based on https://github.com/librucha/servlet-logging-filter
* v8.7.2 - 2017-01-18
    * Request multipart parsing for uploaded files is now limited to MIME type "multipart/form-data"
* v8.7.1 - 2016-12-21
    * API extensions
    * Logging and JavaDoc improvements
* v8.7.0 - 2016-12-12
    * Binds to ph-commons 8.5.6
    * Extracted new subprojects `ph-servlet` and `ph-useragent`
* v8.6.3 - 2016-11-11
    * Binds to ph-commons 8.5.3
    * Work around some Tomcat/Jetty exceptions
* v8.6.2 - 2016-09-25
    * Added possibility to disable URL encoding (putting SESSION ID in URL) with WebSettings
* v8.6.1 - 2016-09-09
    * Binds to ph-commons 8.5.x
* v8.6.0 - 2016-08-21
    * Binds to ph-commons 8.4.x
* v8.5.0 - 2016-07-26
    * Binds to ph-commons 8.3.x

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodingStyleguide.md) |
On Twitter: <a href="https://twitter.com/philiphelger">@philiphelger</a> |
Kindly supported by [YourKit Java Profiler](https://www.yourkit.com)
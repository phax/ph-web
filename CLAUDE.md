# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ph-web is a Java library of web utilities published under `com.helger.web` on Maven Central. It requires Java 17+ and targets Jakarta EE 10 (Servlet 6.0.0). The parent dependency is `ph-commons` (12.1.3).

## Build Commands

```bash
# Full build with tests
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Build a single module
mvn clean install -pl ph-httpclient -DskipTests

# Run tests for a single module
mvn test -pl ph-web

# Run a single test class
mvn test -pl ph-network -Dtest=com.helger.network.port.NetworkPortHelperTest
```

## Module Structure

11 modules with key dependency relationships:

- **ph-network** — base networking (auth, DNS, ports, proxies)
- **ph-dns** — DNS helpers on top of dnsjava; used by ph-httpclient
- **ph-servlet** — Jakarta Servlet helpers; depends on ph-network
- **ph-useragent** — User-Agent/browser identification
- **ph-mail** — Jakarta Mail extensions
- **ph-smtp** — async mail sending; depends on ph-mail + ph-schedule
- **ph-httpclient** — Apache HttpClient 5.x wrappers; depends on ph-dns + ph-network
- **ph-web** — core module: file upload, web scopes, multipart; depends on ph-servlet
- **ph-xservlet** — extended servlet framework (XServlet); depends on ph-web
- **ph-sitemap** — XML sitemap generation
- **ph-jsch** — SSH/SCP/SFTP via JSch fork (mwiede/jsch)

## Code Conventions

- **Package root:** `com.helger.<module-name>` (e.g., `com.helger.network`, `com.helger.httpclient`)
- **Naming:** `I` prefix = interface, `Helper` = static utilities, `Manager` = stateful lifecycle, `Factory` = object creation
- **Nullability:** JSpecify `@NonNull`/`@Nullable` annotations throughout
- **Immutability:** `@Immutable` annotation on thread-safe classes; `@ReturnsMutableCopy` on methods returning mutable copies
- **All modules are OSGI bundles** with `Automatic-Module-Name` for JPMS compatibility
- **Test framework:** JUnit 4 (`org.junit.Assert.*`, `@Test`)
- **Logging:** SLF4J (via ph-commons)

## CI

GitHub Actions runs `mvn` against Java 17, 21, and 25. SNAPSHOT deploys happen from the Java 17 build only.

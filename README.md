<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-ratpack.png" width="300" />
</p>

This `ratpack-pac4j-demo` project is a Ratpack web application to test the [ratpack-pac4j](https://github.com/ratpack/ratpack/tree/master/ratpack-pac4j) security module with various authentication mechanisms: Facebook, Twitter, form, basic auth, CAS, SAML, OpenID Connect, JWT...

## Start & test

Build the project and launch the web app on [http://localhost:8080](http://localhost:8080):

    cd ratpack-pac4j-demo
    mvn clean compile exec:java

To test, you can call a protected url by clicking on the "Protected url by **xxx**" link, which will start the authentication process with the **xxx** provider.

## Automatic build [![Build Status](https://travis-ci.org/pac4j/ratpack-pac4j-demo.png?branch=master)](https://travis-ci.org/pac4j/ratpack-pac4j-demo)

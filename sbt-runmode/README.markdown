sbt-runmode: SBT Runmode Plugin
===============================

[Simple Build Tool] plugin for setting run modes and compiling Javascript and Less CSS assets in a [Lift] web application. Gets you off to a flying start!

Copyright (c) 2011 [Dave Gurnell] of [Untyped].

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Lift]: http://liftweb.net
[Dave Gurnell]: http://boxandarrow.com
[Untyped]: http://untyped.com

Installation
============

For SBT 0.11:

Create a `project/plugins.sbt` file and paste the following content into it:

    resolvers += "Untyped Public Repo" at "http://repo.untyped.com"

    addSbtPlugin("untyped" %% "sbt-runmode" % "0.1-SNAPSHOT")

Then, in your build.sbt file, put:

    seq(runmodeSettings : _*)

Organising your project
=======================

Organise your source files as follows:

 - Place Javascript sources in `src/main/js`. Files will be compiled and placed in
   the same directory structure under `src/main/webapp`. For example:
   
       src/main/js/foo/bar/baz.js    =>    src/main/webapp/foo/bar/baz.js
 
 - Place Less CSS sources in `src/main/css`. Files will be compiled and placed in
   the same directory structure under `src/main/webapp`. For example:
   
       src/main/css/a/b/c.less       =>    src/main/webapp/a/b/c.css

 - Place any remaining web assets - images, Lift templates, plain CSS files, and 
   so on - in `src/main/webapp` as usual. This includes images you are referencing 
   from your Less CSS sources (the URLs will resolve correctly once the Less sources 
   are compiled and moved into place).

 - Place Scala sources, unit tests, Lift properties files, and other resources
   in their usual locations.

Javascript and Less CSS templating and require statements work as normal, the same as they do in the `sbt-js` and `sbt-less` plugins. Check the README files for those plugins for more details.

Using `sbt-runmode`
===================

Now you've set everything up, you will have access to the following SBT commands:

 - Commands for use during development:

    - `development:compile` - recompiles the project's Scala sources;
    - `development:js` - recompiles the project's Javascript sources in development mode (unminified, no variable renaming);
    - `development:less` - recompiles the project's Less CSS sources in development mode (unminified);
    - `development:run-mode` - sets Lift's run mode to "development" (by deleting `jetty-web.xml`);
    - `development:start` - compiles all sources and starts an embedded Jetty with Lift in `development` run mode;
    - `development:stop` - stops the embedded Jetty;
    - `pilot:package` - compiles all sources and packages the app as a WAR with Lift in `development` run mode.

   Jetty is configured to take its web assets straight out of your `src/main/webapp` 
   directory, so you won't need to recompile anything if you change a template.
   
   You can run `development:js` and `development:less` without stopping and restarting Jetty.

 - Commands to use for unit testing:
 
    - `test:run-mode` - sets Lift's run mode to "test" (by installing an appropriate `jetty-web.xml`);
    - `test:test` - run your unit tests with Lift in `test` run mode.

 - Commands to use to test a "pilot" version of your app with minified Javascript and CSS:
 
    - `pilot:compile` - recompiles the project's Scala sources;
    - `pilot:js` - recompiles the project's Javascript sources in production mode (minified with local variable renaming);
    - `pilot:less` - recompiles the project's Less CSS sources in production mode (minified);
    - `pilot:run-mode` - sets Lift's run mode to "pilot" (by installing an appropriate `jetty-web.xml`);
    - `pilot:start` - compiles all sources and starts an embedded Jetty with Lift in `pilot` run mode;
    - `pilot:stop` - stops the embedded Jetty;
    - `pilot:package` - compiles all sources and packages the app as a WAR with Lift in `pilot` run mode.

 - Commands to use to test/package a "production" version of your app:
 
    - `production:compile` - recompiles the project's Scala sources;
    - `production:js` - recompiles the project's Javascript sources in production mode (minified with local variable renaming);
    - `production:less` - recompiles the project's Less CSS sources in production mode (minified);
    - `production:run-mode` - sets Lift's run mode to "production" (by installing an appropriate `jetty-web.xml`);
    - `production:start` - compiles all sources and starts an embedded Jetty with Lift in `production` run mode;
    - `production:stop` - stops the embedded Jetty;
    - `production:package` - compiles all sources and packages the app as a WAR with Lift in `production` run mode.

Licence
=======

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
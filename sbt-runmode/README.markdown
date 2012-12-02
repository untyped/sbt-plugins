sbt-runmode: SBT Runmode Plugin
===============================

[Simple Build Tool] plugin for setting run modes and compiling Javascript and Less CSS assets in a [Lift] web application. Gets you off to a flying start!

Copyright 2011-12 [Dave Gurnell] of [Untyped]

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Lift]: http://liftweb.net
[Dave Gurnell]: http://boxandarrow.com
[Untyped]: http://untyped.com

Installation
============

For SBT 0.11:

Create a `project/plugins.sbt` file and paste the following content into it:

    resolvers += Resolver.url(
      "sbt-plugin-releases",
      url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/")
    )(Resolver.ivyStylePatterns)

    addSbtPlugin("com.untyped" %% "sbt-runmode" % "0.3")

Note that, as of version 0.3, certain things have changed:

 - the plugin has moved from `repo.untyped.com` to `scalasbt.artifactoryonline.com`;
 - the group ID has changed from `untyped` to `com.untyped`;
 - the package name has changed from `untyped.runmode` to `com.untyped.sbtrunmode`.

In your build.sbt file, put:

    seq(runModeSettings : _*)

Configure Jetty Version:
    
    In Production or Pilot configuration, set jettyVersion to match jetty-web.xml generation.

    RunModeKeys.jettyVersion.in(Production) := JettyVersion.Jetty6 // (default)
    RunModeKeys.jettyVersion.in(Production) := JettyVersion.Jetty7Plus


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

Copyright 2011-12 [Dave Gurnell] of [Untyped]

[Dave Gurnell]: http://boxandarrow.com
[Untyped]: http://untyped.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

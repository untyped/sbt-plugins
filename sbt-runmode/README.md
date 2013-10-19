sbt-runmode: SBT Runmode Plugin
===============================

See [README](../README.md) for copyright, licence, and acknowledgements.

[Simple Build Tool] plugin for setting run modes and compiling Javascript and Less CSS assets in a [Lift] web application. Gets you off to a flying start!

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Lift]: http://liftweb.net

## Configuration

Follow the installation instructions in [README](../README.md) and then...

### Selecting a Jetty version

In Production or Pilot configuration, set jettyVersion to match jetty-web.xml generation.

```scala
RunModeKeys.jettyVersion.in(Production) := JettyVersion.Jetty6 // (default)

RunModeKeys.jettyVersion.in(Production) := JettyVersion.Jetty7Plus

RunModeKeys.jettyVersion.in(Production) := {
    val customJetty = new JettyVersion {
                      val template =  """|<?xml version="1.0"  encoding="UTF-8"?>
                                        |<!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "http://www.eclipse.org/jetty/configure.dtd">
                                        |
                                        |<Configure class="org.eclipse.jetty.webapp.WebAppContext">
                                        |    <Call class="java.lang.System" name="setProperty">
                                        |        <Arg>run.mode</Arg>
                                        |        <Arg>%s</Arg>
                                        |    </Call>
                                        |</Configure>"""
                    }
   customJetty
}
```

Sample configuration:

```scala
(compile in Production) <<= compile in Production dependsOn (JsKeys.js in Production)

(resourceManaged in (Production, JsKeys.js)) <<= (resourceManaged in Compile)(_ / "js" )

excludeFilter in (Production, JsKeys.js)) := ("*.d.*" : FileFilter)

(includeFilter in (Production,  JsKeys.js)) := ("*.a.js*" || "*.a.p.js*" || "*.a.coffee*" || "*.a.p.coffee*")

(JsKeys.strictMode in (Production, JsKeys.js)) := false

LessKeys.less.in(Production) <<=  LessKeys.less.in(Production) dependsOn (someCustomTask in  Production)

(compile in Development) <<= compile in Development dependsOn (LessKeys.less in Development)

(excludeFilter in (Development, LessKeys.less)) := ("*.p.less" )
```

### Organising your project

Organise your source files as follows:

 - Place Javascript sources in `src/main/js`. Files will be compiled and placed in
   the same directory structure under `src/main/webapp`. For example:

   ```
   src/main/js/foo/bar/baz.js    =>    src/main/webapp/foo/bar/baz.js
   ```

 - Place Less CSS sources in `src/main/css`. Files will be compiled and placed in
   the same directory structure under `src/main/webapp`. For example:

   ```
   src/main/css/a/b/c.less       =>    src/main/webapp/a/b/c.css
   ```

 - Place any remaining web assets - images, Lift templates, plain CSS files, and
   so on - in `src/main/webapp` as usual. This includes images you are referencing
   from your Less CSS sources (the URLs will resolve correctly once the Less sources
   are compiled and moved into place).

 - Place Scala sources, unit tests, Lift properties files, and other resources
   in their usual locations.

Javascript and Less CSS templating and require statements work as normal, the same as they do
in the `sbt-js` and `sbt-less` plugins. Check the README files for those plugins for more details.

## Usage

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

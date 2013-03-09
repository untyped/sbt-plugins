sbt-js: SBT Javascript Plugin
=============================

[Simple Build Tool] plugin for compiling Javascript and [Coffeescript] files from multiple sources
using Google's [Closure compiler]. Coffeescript is currently experimental.

Copyright 2011-12 [Dave Gurnell] of [Untyped]

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Coffeescript]: http://coffeescript.org
[Closure compiler]: http://code.google.com/p/closure-compiler
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

    addSbtPlugin("com.untyped" %% "sbt-js" % "0.3")

Note that, as of version 0.3, certain things have changed:

 - the plugin has moved from `repo.untyped.com` to `scalasbt.artifactoryonline.com`;
 - the group ID has changed from `untyped` to `com.untyped`;
 - the package name has changed from `untyped.js` to `com.untyped.sbtjs`.

In your build.sbt file, put:

    seq(jsSettings : _*)

If you're using [xsbt-web-plugin](https://github.com/siasia/xsbt-web-plugin "xsbt-web-plugin"),
add the output files to the webapp with:

    (webappResources in Compile) <+= (resourceManaged in Compile)

To change the directory that is scanned, use:

    (sourceDirectory in (Compile, JsKeys.js)) <<= (sourceDirectory in Compile)(_ / "path" / "to" / "js-and-coffee-files")

To specify multiple source directories, use:

    (sourceDirectories in (Compile, JsKeys.js)) <<= (sourceDirectory in Compile) {
      srcDir =>
        Seq(
          srcDir / "first" / "path",
          srcDir / "second" / "path"
        )
    }

When using multiple source directories, files in earlier directories will "shadow" similarly named files in later directories, allowing you you to override individual files in a library without destructively editing the whole thing.

To change the destination directory to `src/main/webapp` in an `xsbt-web-plugin` project, use:

    (resourceManaged in (Compile, JsKeys.js)) <<= (sourceDirectory in Compile)(_ / "webapp")

To automatically add generated Javascript files to the application JAR:

    (resourceGenerators in Compile) <+= (JsKeys.js in Compile)

To cause the `js` task to run automatically when you run `compile`:

    (compile in Compile) <<= compile in Compile dependsOn (JsKeys.js in Compile)

To use pretty-printing instead of regular Javascript minification:

    (JsKeys.prettyPrint in (Compile, JsKeys.js)) := true

To use more aggressive variable renaming (producing smaller output files that are less
likely to work without care):

    (JsKeys.variableRenamingPolicy in (Compile, JsKeys.js)) := VariableRenamingPolicy.ALL

Or to turn variable renaming off altogether:

    (JsKeys.variableRenamingPolicy in (Compile, JsKeys.js)) := VariableRenamingPolicy.OFF

To use ECMASCRIPT5_STRICT instead of regular ECMASCRIPT5 Language mode:

    (JsKeys.strictMode in (Compile, JsKeys.js)) := true

To set google closure warning level (default QUIET => QUIET=0, DEFAULT=1, VERBOSE=2"):

    (JsKeys.warningLevel in (Compile, JsKeys.js)) := 0

To set google closure optimisation level (default SIMPLE_OPTIMIZATIONS => WHITESPACE_ONLY=0, SIMPLE_OPTIMIZATIONS=1, ADVANCED_OPTIMIZATIONS=2"):

    (JsKeys.optimisationLevel in (Compile, JsKeys.js)) := 1

Usage
=====

To compile Javascript and Coffeescript sources, use the `js` command in sbt. Read the
installation instructions above to see how to include Javascript compilation as part
of the regular `compile` command.

The default behaviour of the plugin is to scan your `src/main` directory and look for
two types of files:

 - Javascript files, with extension `.js`
 - Coffeescript files, with extension `.coffee`
 - Javascript manifest files, with extension `.jsm` or `.jsmanifest`

These files are compiled and minified using Google's Closure compiler and placed in
equivalent locations under `target/scala-2.9.x/resource_managed`.

Read on for a description of the handling for each type of file.

### *Require* statements in Javascript and Coffeescript files

You can add *require* statements to your Javascript files to specify dependencies
on other files or URLs. Require statements are comments of the following forms:

In Javascript files:

    // require "path/to/my/file.js"
    // require "path/to/my/file.coffee"
    // require "http://mywebsite.com/path/to/my/file.js"
    // require "http://mywebsite.com/path/to/my/file.coffee"

In Coffeescript files:

    # require "path/to/my/file.js"
    # require "path/to/my/file.coffee"
    # require "http://mywebsite.com/path/to/my/file.js"
    # require "http://mywebsite.com/path/to/my/file.coffee"

Required files are *prepended* to the file they appear in, Coffeescript files are
individually compiled, and the whole lot is passed through the Google Closure
compiler for minification. Note the following:

 - paths are resolved relative to the file they appear in;

 - the position of a require statement in the source does not matter - dependencies
   are always inserted just before the beginning of the file;

 - if multiple require statements are present in a file, the dependencies are inlined
   in the order they are required;

 - dependencies can be recursive - files can require files that require files;

 - woe betide you if you create recursive dependencies between your files :)

### Javascript manifest files

Javascript manifest files are a useful shorthand for building Javascript from a list
of sources. A manifest contains an ordered list of JavaScript source locations.
For example:

    # You can specify remote files using URLs...
    http://code.jquery.com/jquery-1.5.1.js

    # ...and local files using regular paths
    #    (relative to the location of the manifest):
    lib/foo.js
    bar.js

    # Blank lines and bash-style comments are also supported.
    # These may be swapped for JS-style comments in the future.

The sources are cached and inlined into one large Javascript file, which is then
passed to the Closure compiler. The compiler outputs a file of the same name and
relative path of the manifest, but with a `.js` extension. For example, if your
manifest file is at `src/main/javascript/static/js/kitchen-sink.jsm` in the source
tree, the final path would be `resource_managed/main/static/js/kitchen-sink.js`
in the target tree.

Templating
==========

It is sometime useful to template Javascript files. For example, you might want
scripts to refer to `localhost` during development and your live server once deployed.

Javascript files with the extension `.template.js` are passed through a [Mustache]
template processor before being passed to the Closure compiler.

Property names and values are drawn from a properties file that is located and parsed
in an identical manner to the Lift web framework (though the implementation has no
dependency on Lift). The default location for property files is `src/main/resources/props`.
See the [Lift documentation] for file formats and naming conventions.

[Mustache]: http://mustache.github.com/
[Lift documentation]: http://www.assembla.com/spaces/liftweb/wiki/Properties

Acknowledgements
================

v0.6+ for SBT 0.11 based on [less-sbt](https://github.com/softprops/less-sbt), Copyright (c) 2011 Doug Tangren.
v0.1-v0.5 for SBT 0.7 based on [Coffee Script SBT plugin], Copyright (c) 2010 Luke Amdor.

Includes an embedded copy of [jCoffeeScript 1.1] (bundled with sbt-js to solve deployment issues).

[jCoffeeScript 1.1]: https://github.com/yeungda/jcoffeescript

Heavily influenced by the [YUI Compressor SBT plugin] by Jon Hoffman.

Thanks to:

 - [Tim Nelson](https://github.com/eltimn) for his work on the SBT 0.11
   migration and dramatic improvements to this README.

 - [Glade Diviney](https://github.com/gladed) for help producing test cases
   and debugging various issues.

 - [Alexandre Richonnier](http://www.hera.cc) for some compiler options.

[Coffee Script SBT plugin]: https://github.com/rubbish/coffee-script-sbt-plugin
[YUI Compressor SBT plugin]: https://github.com/hoffrocket/sbt-yui

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

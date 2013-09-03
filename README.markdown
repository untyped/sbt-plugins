Untyped SBT Plugins
===================

Copyright 2011-12 [Dave Gurnell] of [Untyped]

This repo contains source for three SBT plugins:

 - [sbt-less] - Less CSS compilation, minification, and templating;
 - [sbt-js] - Javascript and Coffeescript compilation, minification, and templating;
 - [sbt-tipi] - wrapper for the [Tipi] templating language;
 - [sbt-runmode] - specification of Lift run modes using custom jetty-web.xml files.

See the `README` files in the relevant subdirectories for more information and acknowledgements.

Version 0.6 (current development release)
=========================================

This release is compatible with Scala 2.9.2 and SBT 0.12.3.
Sample `plugins.sbt` file:

    resolvers ++= Resolver.url("untyped", url("http://ivy.untyped.com"))(Resolver.ivyStylePatterns)

    addSbtPlugin("com.untyped" % "sbt-js"      % <<VERSION>>)

    addSbtPlugin("com.untyped" % "sbt-less"    % <<VERSION>>)

    addSbtPlugin("com.untyped" % "sbt-runmode" % <<VERSION>>)

    addSbtPlugin("com.untyped" % "sbt-tipi"    % <<VERSION>>)

Development snapshots are published with milestone suffixes (`"0.6-M1"` and so on). See [Build.scala] for the latest version number.

New features:

Updated to Google Closure Compiler v20130227 and added the *strict mode*, *optimisation level* and *warning level* options. Thanks to [Alexandre Richonnier] for these features.

Added the option to change the Coffee Script compiler version, changed the default version to v1.6.1, and added the *bare* option.

Added the [sbt-tipi] plugin for the [Tipi] templating language.

Version 0.5 (current stable release)
====================================

This is a dual-release for Scala 2.9.2 / SBT 0.11.3 and Scala 2.9.1 / SBT 0.12.3.
Sample `plugins.sbt` file:

    resolvers ++= Resolver.url(
      "sbt-plugin-releases",
      url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"
    ))(Resolver.ivyStylePatterns)

    addSbtPlugin("com.untyped" % "sbt-js"      % "0.5")

    addSbtPlugin("com.untyped" % "sbt-less"    % "0.5")

    addSbtPlugin("com.untyped" % "sbt-runmode" % "0.5")

New features:

Supports SBT 0.12.3 and Scala 2.9.2.

Added the experimental `useCommandLine` key for `sbt-less`, allowing you to
use command line `lessc` instead of Rhino (defaults to `false`).

Better reporting of line/column numbers for Less CSS compilation errors.

Bug fixes:

Fixed a bug that caused unnecessary recompilation of multi-file JS/Less builds
when `includeFilter` was used.

Removed features:

Reverted to the original placement of Less/CSS `@import` statements.
Imports are once again inlined before the top of the file rather than at
the point of the import. There are two reasons for this change:

 1. By inlining before the top of the file, the plugin can ensure that
    each Less/CSS library is included once and once only in the output file.
    This ensures efficient compilation of complex libraries such as Twitter
    Bootstrap, producing a several hundred percent speedup.

 2. The [W3C specification] for `@import` statements states that they are
    only allowed at the top of a file. The two inlining behaviours of sbt-less
    are consistent if this restriction is applied by the stylesheet author
    (i.e. most people should be unaffected by this regression).

[W3C specification]: http://www.w3.org/TR/CSS21/cascade.html#at-import

Version 0.4
===========

This version works with SBT SBT 0.11.2. Sample `plugins.sbt` file:

    resolvers ++= Resolver.url(
      "sbt-plugin-releases",
      url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"
    ))(Resolver.ivyStylePatterns)

    addSbtPlugin("com.untyped" % "sbt-js"      % "0.4")

    addSbtPlugin("com.untyped" % "sbt-less"    % "0.4")

    addSbtPlugin("com.untyped" % "sbt-runmode" % "0.4")

New features:

You can now specify multiple `sourceDirectories` in `sbt-js` and `sbt-less`,
providing `CLASSPATH`-style semantics when resolving files in `// require` and
`@import` statements.

This is useful if you want to override a single file in a library such as
Twitter Bootstrap. Check the library out as a Git submodule in your project,
and specify your sourceDirectories as follows:

    (sourceDirectories in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile) {
      srcDir =>
        Seq(
          srcDir / "path" / "to" / "my" / "files",
          srcDir / "path" / "to" / "twitter" / "bootstrap"
        )
    }

Any `@import` statements are resolved relative to your files first, and then
Twitter's files. You can override `variables.less` and still maintain the ability
to pull the latest fixes from Twitter's Github repo.

Changes and bug fixes:

Less CSS content pulled in by `@import` statements now appears at the point of the
`@import` statement, rather than at the top of the file. For example, the following
three files:

    a.less:
    .a{color:black;}

    b.less:
    .b{color:black;}

    c.less:
    @import "a.less";
    .c{color:black;}
    @import "b.less";

compile to:

    .a{color:black;}
    .c{color:black;}
    .b{color:black;}

where they would previously have compiled to:

    .a{color:black;}
    .b{color:black;}
    .c{color:black;}

Note that `\\ require` statements in Javascript and Coffeescript files are unaffected.

Thanks to [Denis Bardadym] for this fix.

Version 0.3
===========

No new features or bug fixes.

Hosting moved from the Untyped Maven repository to the [SBT community plugins repo].
As a consequence of this, certain things have changed:

 - the plugins have moved from `repo.untyped.com` to `scalasbt.artifactoryonline.com`;
 - the group ID has changed from `untyped` to `com.untyped`;
 - the package names have changed from `untyped.<foo>` to `com.untyped.sbt<foo>`.

These versions work with SBT 0.11.0 and SBT 0.11.2. Sample `plugins.sbt` file:

    resolvers ++= Resolver.url("sbt-plugin-releases", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

    addSbtPlugin("com.untyped" % "sbt-js"      % "0.3")

    addSbtPlugin("com.untyped" % "sbt-less"    % "0.3")

    addSbtPlugin("com.untyped" % "sbt-runmode" % "0.3")

Snapshots are published on [ivy.untyped.com]:

    resolvers ++= Resolver.url("Untyped", url("http://ivy.untyped.com"))(Resolver.ivyStylePatterns)

    addSbtPlugin("com.untyped" % "sbt-js"      % "0.4-SNAPSHOT")

    addSbtPlugin("com.untyped" % "sbt-less"    % "0.4-SNAPSHOT")

    addSbtPlugin("com.untyped" % "sbt-runmode" % "0.4-SNAPSHOT")

Version 0.2
===========

New features:

 - `sbt-js`: experimental support for CoffeeScript;
 - `sbt-mustache`: new experimental plugin for templating arbitrary files (currently ver limited);

Bug fixes:

- `sbt-less`: Import statements in Less CSS files are interpreted relative to the file in which they appear,
   rather than the root file in the dependency graph.

These versions work with SBT 0.11.0 and SBT 0.11.2. Sample `plugins.sbt` file:

    resolvers ++= "untyped" at "http://repo.untyped.com"

    addSbtPlugin("untyped" % "sbt-js" % "0.2")

    addSbtPlugin("untyped" % "sbt-less" % "0.2")

    addSbtPlugin("untyped" % "sbt-runmode" % "0.2")

Licence
=======

Copyright 2011-12 [Dave Gurnell] of [Untyped]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[SBT community plugins repo]: http://www.scala-sbt.org/
[Dave Gurnell]: http://boxandarrow.com
[Untyped]: http://untyped.com
[sbt-less]: https://github.com/untyped/sbt-plugins/tree/master/sbt-less
[sbt-js]: https://github.com/untyped/sbt-plugins/tree/master/sbt-js
[sbt-runmode]: https://github.com/untyped/sbt-plugins/tree/master/sbt-runmode
[sbt-tipi]: https://github.com/untyped/sbt-plugins/tree/master/sbt-tipi
[Tipi]: https://github.com/davegurnell/tipi
[ivy.untyped.com]: http://ivy.untyped.com/com.untyped
[Build.scala]: https://github.com/untyped/sbt-plugins/blob/master/project/Build.scala
[Denis Bardadym]: https://github.com/btd
[Shikhar Bhushan]: https://github.com/shikhar
[Alexandre Richonnier]: https://github.com/heralight

Untyped SBT Plugins
===================

Copyright 2011-12 [Dave Gurnell] of [Untyped]

This repo contains source for three SBT plugins:

 - [sbt-less] - Less CSS compilation, minification, and templating;
 - [sbt-js] - Javascript and Coffeescript compilation, minification, and templating;
 - [sbt-runmode] - specification of Lift run modes using custom jetty-web.xml files.

See the `README` files in the relevant subdirectories for more information and acknowledgements.

Version 0.4
===========

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
[Denis Bardadym]: https://github.com/btd

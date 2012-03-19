Untyped SBT Plugins
===================

Copyright 2011-12 [Dave Gurnell] of [Untyped]

This repo contains source for three SBT plugins:

 - [sbt-less] - Less CSS compilation, minification, and templating;
 - [sbt-js] - Javascript and Coffeescript compilation, minification, and templating;
 - [sbt-runmode] - specification of Lift run modes using custom jetty-web.xml files.

See the `README` files in the relevant subdirectories for more information.

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

    addSbtPlugin("com.untyped" % "sbt-js" % "0.3")

    addSbtPlugin("com.untyped" % "sbt-less" % "0.3")

    addSbtPlugin("com.untyped" % "sbt-runmode" % "0.3")

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

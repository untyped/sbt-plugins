Untyped SBT Plugins
===================

Copyright 2011-12 [Dave Gurnell] of [Untyped]

This repo contains source for three SBT plugins:

 - [sbt-less] - Less CSS compilation, minification, and templating;
 - [sbt-js] - Javascript and Coffeescript compilation, minification, and templating;
 - [sbt-runmode] - specification of Lift run modes using custom jetty-web.xml files.

See the `README` files in the relevant subdirectories for more information.

Changes for v0.3
================

Note that as of version 0.3 we have moved hosting for these plugins from the Untyped 
Maven repository to the [SBT community plugins repo]. As a consequence, certain things 
have changed:

 - the plugins have moved from `repo.untyped.com` to `scalasbt.artifactoryonline.com`;
 - the group ID has changed from `untyped` to `com.untyped`;
 - the package names have changed from `untyped.<foo>` to `com.untyped.sbt<foo>`.

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

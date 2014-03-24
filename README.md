# Untyped SBT Plugins

Copyright 2011-13 [Dave Gurnell] of [Untyped]. See below for licence and acknowledgements.

This repo contains the following SBT plugins:

 - [sbt-js](sbt-js/) - Javascript and Coffeescript compilation, minification, and templating;
 - [sbt-less](sbt-less/) - Less CSS compilation, minification, and templating;
 - [sbt-sass](sbt-sass/) - Sass compilation, minification, and templating;
 - [sbt-mustache](sbt-mustache/) - Mustache templating for HTML files;
 - [sbt-runmode](sbt-runmode/) - specification of Lift run modes using custom jetty-web.xml files.

## Installation

Create a file called `project/plugins.sbt` in your SBT project and add the following:

```scala
// This line is needed for development releases but not stable ones:

resolvers += Resolver.url("untyped", url("http://ivy.untyped.com"))(Resolver.ivyStylePatterns)

// Add whichever plugins you want to use:

addSbtPlugin("com.untyped" % "sbt-js"       % <<VERSION>>)

addSbtPlugin("com.untyped" % "sbt-less"     % <<VERSION>>)

addSbtPlugin("com.untyped" % "sbt-sass"     % <<VERSION>>)

addSbtPlugin("com.untyped" % "sbt-mustache" % <<VERSION>>)

addSbtPlugin("com.untyped" % "sbt-runmode"  % <<VERSION>>)
```

Then, in your `build.sbt`, add the following:

```scala
// Add the lines below for whichever plugins you want to use:

seq(jsSettings : _*)

seq(lessSettings : _*)

seq(sassSettings : _*)

seq(mustacheSettings : _*)

seq(runmodeSettings : _*)
```

See the changelog below for the current stable and development version numbers and their compatibility
with different versions of SBT. Development releases are published with milestone suffixes
(`"0.x-M1"` and so on). See [Build.scala] on the `develop` branch for the latest version number.

## Configuration

Each plugin has its own set of configuration options described in its own README file:

 - [sbt-js](sbt-js/)
 - [sbt-less](sbt-less/)
 - [sbt-sass](sbt-sass/)
 - [sbt-mustache](sbt-mustache/)
 - [sbt-runmode](sbt-runmode/)

## Changelog

## Version 0.7 (current development release; SBT 0.12, 0.13)

New features:

 - Added [sbt-sass](sbt-sass/). Thanks to [Torbjørn Vatn] for this great contribution.

### Version 0.6 (current stable release; SBT 0.12, 0.13)

New features:

 - Added support for SBT 0.13. Thanks to [mdedetrich] for this and for numerous other
   clean-ups to the code.

 - Updated to Google Closure Compiler v20130227 and added the *strict mode*,
   *optimisation level* and *warning level* options. Thanks to [Alexandre Richonnier]
   for these features.

 - Added the option to change the Coffee Script compiler version, changed the default
   version to v1.6.1, and added the *bare* option.

### Version 0.5 (SBT 0.11.3, 0.12)

New features:

 - Added an experimental `useCommandLine` key for `sbt-less`, allowing you to
   use command line `lessc` instead of Rhino (defaults to `false`).

 - Better reporting of line/column numbers for Less CSS compilation errors.

Bug fixes:

 - Fixed a bug that caused unnecessary recompilation of multi-file JS/Less builds
when `includeFilter` was used.

Removed features:

 - Reverted to the original placement of Less/CSS `@import` statements.
Imports are once again inlined before the top of the file rather than at
the point of the import. There are two reasons for this change:

    - By inlining before the top of the file, the plugin can ensure that
      each Less/CSS library is included once and once only in the output file.
      This ensures efficient compilation of complex libraries such as Twitter
      Bootstrap, producing a several hundred percent speedup.

    - The [W3C specification] for `@import` statements states that they are
      only allowed at the top of a file. The two inlining behaviours of sbt-less
      are consistent if this restriction is applied by the stylesheet author
      (i.e. most people should be unaffected by this regression).

[W3C specification]: http://www.w3.org/TR/CSS21/cascade.html#at-import

### Version 0.4 (SBT 0.11.2)

New features:

 - You can now specify multiple `sourceDirectories` in `sbt-js` and `sbt-less`,
   providing `CLASSPATH`-style semantics when resolving files in `// require` and
   `@import` statements.

 - This is useful if you want to override a single file in a library such as
   Twitter Bootstrap. Check the library out as a Git submodule in your project,
   and specify your sourceDirectories as follows:

   ```scala
   (sourceDirectories in (Compile, LessKeys.less)) <<=
     (sourceDirectory in Compile) {
       srcDir =>
         Seq(
           srcDir / "path" / "to" / "my" / "files",
           srcDir / "path" / "to" / "twitter" / "bootstrap"
         )
     }
   ```

 - Any `@import` statements are resolved relative to your files first, and then
   Twitter's files. You can override `variables.less` and still maintain the ability
   to pull the latest fixes from Twitter's Github repo.

Changes and bug fixes:

 - Less CSS content pulled in by `@import` statements now appears at the point of the
   `@import` statement, rather than at the top of the file. `\\ require` statements in
   Javascript and Coffeescript files are unaffected.

   Thanks to [Denis Bardadym] for this fix.

   **Note: this feature was unfortunately reverted in sbt-less version 0.5 due to
   performance problems.**

### Version 0.3 (SBT 0.11.0, 0.11.2)

No new features or bug fixes.

Hosting moved from the Untyped Maven repository to the [SBT community plugins repo].
As a consequence of this, certain things have changed:

 - the plugins have moved from `repo.untyped.com` to `scalasbt.artifactoryonline.com`;
 - the group ID has changed from `untyped` to `com.untyped`;
 - the package names have changed from `untyped.<foo>` to `com.untyped.sbt<foo>`.

### Version 0.2 (SBT 0.11.0, 0.11.2)

New features:

 - `sbt-js`: experimental support for CoffeeScript;
 - `sbt-mustache`: new experimental plugin for templating arbitrary files (currently ver limited);

Bug fixes:

- `sbt-less`: Import statements in Less CSS files are interpreted relative to the file in which they appear,
   rather than the root file in the dependency graph.

## Contributing

Contributions to code and documentation are gratefully accepted. Raise an issue first to discuss,
and then submit a pull request. Please note the following before you start:

### Git Flow

This repo is based on the [git flow] branching model: all development is based off the `develop` branch,
while the `master` branch is reserved for the current stable release.

**Please base pull requests off of the `develop` branch.**

You can grab command line addons to Git to assist with Git Flow. For example, on a Mac with Homebrew:

    brew install git-flow

### Building

We strongly recommend you use the latest version of Paul Phillips' [SBT launcher] script to automatically select and install the correct version of SBT to build this project.

### Tests

Please make sure all tests pass before submitting a pull request. The build script uses the
[sbt-cross-building] plugin to target various SBT and Scala versions, and the [sbt-scripted]
plugin as a test runner.

To compile and test the code for all targetted versions of SBT, do the following:

```
^compile
^scripted
```

To compile and test the code for a single version of SBT, do the following:

```
^^0.13
compile
scripted
```

The the `sbt-cross-building` documentation for more information.

### Release

Note-to-self: tasks to complete for a stable release:

 - `git flow release start x.y`
 - update version number in `Build.scala`
 - update headings in `README.md`
 - `sbt ^scripted`
 - `sbt ^publish`
 - `git flow release finish x.y`

After release:

 - switch to `develop`
 - update version number in `Build.scala` to next milestone
 - push an immediate release to `ivy.untyped.com`

## Licence

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

## Acknowledgements

Many thanks to the following for their contributions (alphabetical order): [Denis Bardadym], [Shikhar Bhushan],
[mdedetrich], [Glade Diviney], [Crisson Jno-Charles], [Tim Nelson], [Alexandre Richonnier], and [Torbjørn Vatn].

**sbt-js**

v0.6+ for SBT 0.11 based on [less-sbt](https://github.com/softprops/less-sbt), Copyright (c) 2011 Doug Tangren.
v0.1-v0.5 for SBT 0.7 based on [Coffee Script SBT plugin], Copyright (c) 2010 Luke Amdor.

Includes an embedded copy of [jCoffeeScript 1.1] (bundled with sbt-js to solve deployment issues).

**sbt-less**

v0.2 for SBT 0.11 based on [less-sbt], Copyright (c) 2011 Doug Tangren.
v0.1 for SBT 0.7 based on [Coffee Script SBT plugin], Copyright (c) 2010 Luke Amdor.

Heavily influenced by the [YUI Compressor SBT plugin] by Jon Hoffman.

v0.1 used a tweaked version of the [Less for Java] wrapper by Asual.

**sbt-sass**

Written by [Torbjørn Vatn].

Includes embedded copies of [Sass] 3.x, Copyright (c) 2006-2013 Hampton Catlin, Nathan Weizenbaum, and Chris Eppstein, distributed under the [MIT License].

[MIT License]: http://sass-lang.com/documentation/file.MIT-LICENSE.html
[Sass]: http://sass-lang.com/
[Torbjørn Vatn]: https://github.com/torbjornvatn
[Crisson Jno-Charles]: https://github.com/crisson
[Alexandre Richonnier]: https://github.com/heralight
[Build.scala]: https://github.com/untyped/sbt-plugins/blob/master/project/Build.scala
[Coffee Script SBT plugin]: https://github.com/rubbish/coffee-script-sbt-plugin
[Dave Gurnell]: http://boxandarrow.com
[Denis Bardadym]: https://github.com/btd
[git flow]: http://nvie.com/posts/a-successful-git-branching-model/
[Glade Diviney]: https://github.com/gladed
[ivy.untyped.com]: http://ivy.untyped.com/com.untyped
[jCoffeeScript 1.1]: https://github.com/yeungda/jcoffeescript
[Less for Java]: http://www.asual.com/lesscss/
[less-sbt]: https://github.com/softprops/less-sbt
[mdedetrich]: https://github.com/mdedetrich
[SBT community plugins repo]: http://www.scala-sbt.org/
[sbt-cross-building]: https://github.com/jrudolph/sbt-cross-building
[sbt-scripted]: https://github.com/sbt/sbt/tree/0.13/scripted
[Shikhar Bhushan]: https://github.com/shikhar
[Tim Nelson]: https://github.com/eltimn
[Untyped]: http://untyped.com
[YUI Compressor SBT plugin]: https://github.com/hoffrocket/sbt-yui
[SBT launcher]: https://github.com/paulp/sbt-extras

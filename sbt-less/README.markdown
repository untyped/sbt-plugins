#sbt-less: SBT Less CSS Plugin

[Simple Build Tool] plugin for compiling [Less CSS] files.

Copyright 2011-12 [Dave Gurnell] of [Untyped]

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Less CSS]: http://lesscss.org
[Dave Gurnell]: http://boxandarrow.com
[Untyped]: http://untyped.com

##Installation

###For SBT 0.11.x:

Create a `project/plugins.sbt` file and paste the following content into it:

    resolvers += Resolver.url(
      "sbt-plugin-releases",
      url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/")
    )(Resolver.ivyStylePatterns)

    addSbtPlugin("com.untyped" %% "sbt-less" % "0.3")

Note that, as of version 0.3, certain things have changed:

 - the plugin has moved from `repo.untyped.com` to `scalasbt.artifactoryonline.com`;
 - the group ID has changed from `untyped` to `com.untyped`;
 - the package name has changed from `untyped.less` to `com.untyped.sbtless`.

In your build.sbt file, put:

    seq(lessSettings : _*)

## Customize it

### If you're using [xsbt-web-plugin](https://github.com/siasia/xsbt-web-plugin "xsbt-web-plugin"),
    add the output files to the webapp with:

    (webappResources in Compile) <+= (resourceManaged in Compile)

### To change the directory that is scanned, use:

    (sourceDirectory in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile)(_ / "path" / "to" / "less-files")

### To specify multiple source directories, use:

    (sourceDirectories in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile) {
      srcDir =>
        Seq(
          srcDir / "first" / "path",
          srcDir / "second" / "path"
        )
    }

When using multiple source directories, files in earlier directories will "shadow" similarly named files in later directories, allowing you you to override individual files in a library without destructively editing the whole thing.

### To change the destination directory to `src/main/webapp` in an `xsbt-web-plugin` project, use:

    (resourceManaged in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile)(_ / "webapp")

### To cause the `less` task to run automatically when you run `compile`:

    (compile in Compile) <<= compile in Compile dependsOn (LessKeys.less in Compile)

### To use pretty-printing instead of regular CSS minification:

    (LessKeys.prettyPrint in (Compile, LessKeys.less)) := true
	
### To include, exclude (filter) less files:

    (includeFilter in (Compile, LessKeys.less)) := ("*.include.less": FileFilter)

    (excludeFilter in (Compile, LessKeys.less)) := ("*.exclude*": FileFilter)

### To specify which version of the Less CSS compiler to use:
####  Options:
    * Less113
    * Less115
    * Less121
    * Less130
    * Less130b (with less 1.3.0 bug fix: duplicate import files https://github.com/cloudhead/less.js/pull/431

    LessKeys.lessVersion in (Compile, LessKeys.less) := LessVersion.Less130

Take a look at the unit test for more details.

## Usage

To compile Less CSS sources, use the `less` command in sbt. Read the installation instructions
above to see how to include Less CSS compilation as part of the regular `compile` command.

The default behaviour of the plugin is to scan your `src/main` directory and look files with the
extension `.less`.

These files are compiled to CSS using Less CSS v1.1.3 and placed in equivalent locations under
`target/scala-2.9.x/resource_managed`.

## Templating

It is sometime useful to template Less files. For example, you might want scripts
to refer to one value during development and another value once deployed to production.

Javascript files with the extension `.template.less` are passed through a [Mustache]
template processor before being passed to the Less compiler.

Property names and values are drawn from a properties file that is located and parsed
in an identical manner to the Lift web framework (though the implementation has no
dependency on Lift). The default location for property files is `src/main/resources/props`.
See the [Lift documentation] for file formats and naming conventions.

[Mustache]: http://mustache.github.com/
[Lift documentation]: http://www.assembla.com/spaces/liftweb/wiki/Properties

## Acknowledgements

v0.2 for SBT 0.11 based on [less-sbt], Copyright (c) 2011 Doug Tangren.
v0.1 for SBT 0.7 based on [Coffee Script SBT plugin], Copyright (c) 2010 Luke Amdor.

Heavily influenced by the [YUI Compressor SBT plugin] by Jon Hoffman.

v0.1 used a tweaked version of the [Less for Java] wrapper by Asual.

### Thanks to:

 - [Tim Nelson](https://github.com/eltimn) for his work on the SBT 0.11
   migration and dramatic improvements to this README.

 - [Glade Diviney](https://github.com/gladed) for help producing test cases
   and debugging various issues.

 - [Alexandre Richonnier]: http://www.hera.cc less 1.3 and less 1.3 fix duplicate import
   
[less-sbt]: https://github.com/softprops/less-sbt
[Coffee Script SBT plugin]: https://github.com/rubbish/coffee-script-sbt-plugin
[YUI Compressor SBT plugin]: https://github.com/hoffrocket/sbt-yui
[Less for Java]: http://www.asual.com/lesscss/

## Licence

###Copyright 2011-12 [Dave Gurnell] of [Untyped]

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

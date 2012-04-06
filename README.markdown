sbt-less: SBT Less CSS Plugin
=============================

[Simple Build Tool] plugin for compiling [Less CSS] files.

Copyright 2011-12 [Denis Bardadym]

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Less CSS]: http://lesscss.org

Installation
============

For SBT 0.11:

Create a `project/plugins.sbt` file and paste the following content into it:

    resolvers += "btd github" at "http://btd.github.com/maven2"

    addSbtPlugin("com.github.btd" %% "sbt-less-plugin" % "0.0.1")

In your build.sbt file, put:

    seq(lessSettings : _*)

If you're using [xsbt-web-plugin](https://github.com/siasia/xsbt-web-plugin "xsbt-web-plugin"),
add the output files to the webapp with:

    (webappResources in Compile) <+= (resourceManaged in Compile)

To change the directory that is scanned, use:

    (sourceDirectory in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile)(_ / "path" / "to" / "less-files")

To specify multiple source directories, use:

    (sourceDirectories in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile) {
      srcDir =>
        Seq(
          srcDir / "first" / "path",
          srcDir / "second" / "path"
        )
    }

When using multiple source directories, files in last directories will "shadow" similarly named files in later directories, allowing you you to override individual files in a library without destructively editing the whole thing.

To change the destination directory to `src/main/webapp` in an `xsbt-web-plugin` project, use:

    (resourceManaged in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile)(_ / "webapp")

To cause the `less` task to run automatically when you run `compile`:

    (compile in Compile) <<= compile in Compile dependsOn (LessKeys.less in Compile)

To use pretty-printing instead of regular CSS minification:

    (LessKeys.prettyPrint in (Compile, LessKeys.less)) := true

Usage
=====

To compile Less CSS sources, use the `less` command in sbt. Read the installation instructions
above to see how to include Less CSS compilation as part of the regular `compile` command.

The default behaviour of the plugin is to scan your `src/main` directory and look files with the
extension `.less`.

These files are compiled to CSS using Less CSS v1.3.0 and placed in equivalent locations under
`target/scala-2.9.x/resource_managed`.

Acknowledgements
================

Based on sbt-less of Dave Gurnell.

Licence
=======

Copyright 2011-12 [Denis Bardadym]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

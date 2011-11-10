sbt-less: SBT Less CSS Plugin
=============================

[Simple Build Tool] plugin for compiling [Less CSS] files.

Copyright (c) 2011 [Dave Gurnell] of [Untyped].

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Less CSS]: http://lesscss.org
[Dave Gurnell]: http://boxandarrow.com
[Untyped]: http://untyped.com

Installation
============

For SBT 0.11:

Create a `project/plugins.sbt` file and paste the following content into it:

    resolvers += "Untyped Public Repo" at "http://repo.untyped.com"
    
    addSbtPlugin("untyped" % "sbt-less" % "0.2-SNAPSHOT")

Then, in your build.sbt file, put:

    seq(lessSettings : _*)

If you're using [xsbt-web-plugin](https://github.com/siasia/xsbt-web-plugin "xsbt-web-plugin"), 
add the output files to the webapp with:

    (webappResources in Compile) <+= (resourceManaged in Compile)

To change the directory that is scanned, use:

    (sourceDirectory in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile)(_ / "path" / "to" / "less-files")

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

These files are compiled to CSS using Less CSS v1.1.3 and placed in equivalent locations under 
`target/scala-2.9.x/resource_managed`.

Templating
==========

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

Acknowledgements
================

v0.2 for SBT 0.11 based on [less-sbt], Copyright (c) 2011 Doug Tangren.
v0.1 for SBT 0.7 based on [Coffee Script SBT plugin], Copyright (c) 2010 Luke Amdor.

Heavily influenced by the [YUI Compressor SBT plugin] by Jon Hoffman.

v0.1 used a tweaked version of the [Less for Java] wrapper by Asual.

Thanks to [Tim Nelson](https://github.com/eltimn) for his work on the SBT 0.11 
migration and dramatic improvements to this README.

[less-sbt]: https://github.com/softprops/less-sbt
[Coffee Script SBT plugin]: https://github.com/rubbish/coffee-script-sbt-plugin
[YUI Compressor SBT plugin]: https://github.com/hoffrocket/sbt-yui
[Less for Java]: http://www.asual.com/lesscss/

Licence
=======

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

# sbt-js: SBT Javascript Plugin

See [README](../README.md) for copyright, licence, and acknowledgements.

[Simple Build Tool] plugin for compiling Javascript and [Coffeescript] files from multiple sources
using Google's [Closure compiler]. Coffeescript is currently experimental.

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Closure compiler]: http://code.google.com/p/closure-compiler

## Configuration

Follow the installation instructions in [README](../README.md) and then...

If you're using [xsbt-web-plugin](https://github.com/siasia/xsbt-web-plugin "xsbt-web-plugin"),
add the output files to the webapp with:

```scala
(webappResources in Compile) <+= (resourceManaged in Compile)
```

To change the directory that is scanned, use:

```scala
(sourceDirectory in (Compile, JsKeys.js)) <<=
  (sourceDirectory in Compile)(_ / "path" / "to" / "js-and-coffee-files")
```

To specify multiple source directories, use:

```scala
(sourceDirectories in (Compile, JsKeys.js)) <<=
  (sourceDirectory in Compile) {
    srcDir =>
      Seq(
        srcDir / "first" / "path",
        srcDir / "second" / "path"
      )
  }
```

When using multiple source directories, files in earlier directories will "shadow" similarly
named files in later directories, allowing you you to override individual files in a library
without destructively editing the whole thing.

To change the destination directory to `src/main/webapp` in an `xsbt-web-plugin` project, use:

```scala
(resourceManaged in (Compile, JsKeys.js)) <<= (sourceDirectory in Compile)(_ / "webapp")
```

To automatically add generated Javascript files to the application JAR:

```scala
(resourceGenerators in Compile) <+= (JsKeys.js in Compile)
```

To cause the `js` task to run automatically when you run `compile`:

```scala
(compile in Compile) <<= compile in Compile dependsOn (JsKeys.js in Compile)
```

To switch to Coffeescript 1.1.0 (default 1.6.1):

```scala
(JsKeys.coffeeVersion in (Compile)) := CoffeeVersion.Coffee110
```

To tell the Coffeescript compiler not to wrap code in an anonymous function wrapper:

```scala
(JsKeys.coffeeBare in (Compile)) := true
```

To use pretty-printing instead of regular Javascript minification:

```scala
(JsKeys.prettyPrint in (Compile)) := true
```

To use more aggressive variable renaming (producing smaller output files that are less
likely to work without care):

```scala
(JsKeys.variableRenamingPolicy in (Compile)) := VariableRenamingPolicy.ALL
```

Or to turn variable renaming off altogether:

```scala
(JsKeys.variableRenamingPolicy in (Compile)) := VariableRenamingPolicy.OFF
```

To use ECMASCRIPT5_STRICT instead of regular ECMASCRIPT5 Language mode:

```scala
(JsKeys.strictMode in (Compile)) := true
```

To make Closure Compiler verbose (levels are `QUIET`, `DEFAULT`, `VERBOSE`, default `QUIET`):

```scala
(JsKeys.warningLevel in (Compile)) := WarningLevel.VERBOSE
```

To disable Closure Compiler optimisations (levels are `WHITESPACE_ONLY`,
`SIMPLE_OPTIMIZATIONS`, `ADVANCED_OPTIMIZATIONS`, default `SIMPLE_OPTIMIZATIONS`):

```scala
(JsKeys.compilationLevel in (Compile, JsKeys.js)) := CompilationLevel.WHITESPACE_ONLY
```

## Usage

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

### *Require* statements

You can add *require* statements to your Javascript files to specify dependencies
on other files or URLs. Require statements are comments of the following forms:

In Javascript files:

```js
// require "path/to/my/file.js"
// require "path/to/my/file.coffee"
// require "http://mywebsite.com/path/to/my/file.js"
// require "http://mywebsite.com/path/to/my/file.coffee"
```

In Coffeescript files:

```coffee
\# require "path/to/my/file.js"
\# require "path/to/my/file.coffee"
\# require "http://mywebsite.com/path/to/my/file.js"
\# require "http://mywebsite.com/path/to/my/file.coffee"
```

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

```
\# You can specify remote files using URLs...
http://code.jquery.com/jquery-1.5.1.js

\# ...and local files using regular paths
\#    (relative to the location of the manifest):
lib/foo.js
bar.js

\# Blank lines and bash-style comments are also supported.
\# These may be swapped for JS-style comments in the future.
```

The sources are cached and inlined into one large Javascript file, which is then
passed to the Closure compiler. The compiler outputs a file of the same name and
relative path of the manifest, but with a `.js` extension. For example, if your
manifest file is at `src/main/javascript/static/js/kitchen-sink.jsm` in the source
tree, the final path would be `resource_managed/main/static/js/kitchen-sink.js`
in the target tree.

### Templating

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

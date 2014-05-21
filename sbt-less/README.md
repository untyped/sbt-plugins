# sbt-less: SBT Less CSS Plugin

See [README](../README.md) for copyright, licence, and acknowledgements.

[Simple Build Tool] plugin for compiling [Less CSS] files.

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Less CSS]: http://lesscss.org

## Configuration

Follow the installation instructions in [README](../README.md) and then...

If you're using [xsbt-web-plugin](https://github.com/siasia/xsbt-web-plugin "xsbt-web-plugin"),
add the output files to the webapp with:

```scala
(webappResources in Compile) <+= (resourceManaged in Compile)
```

To change the directory that is scanned, use:

```scala
(sourceDirectory in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile)(_ / "path" / "to" / "less-files")
```

To specify multiple source directories, use:

```scala
(sourceDirectories in (Compile, LessKeys.less)) <<=
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
(resourceManaged in (Compile, LessKeys.less)) <<= (sourceDirectory in Compile)(_ / "webapp")
```

To version your assets to properly handle browser caching (Lifters, see [this gist](https://gist.github.com/barnesjd/129a1a9f90305b798539)):

```scala
LessKeys.filenameSuffix in Compile <<= version ("-"+_)
```

To automatically add generated CSS files to the application JAR:

```scala
(resourceGenerators in Compile) <+= (LessKeys.less in Compile)
```

To cause the `less` task to run automatically when you run `compile`:

```scala
(compile in Compile) <<= compile in Compile dependsOn (LessKeys.less in Compile)
```

To use pretty-printing instead of regular CSS minification:

```scala
(LessKeys.prettyPrint in (Compile, LessKeys.less)) := true
```

To include, exclude (filter) less files:

```scala
(includeFilter in (Compile, LessKeys.less)) := "*.include.less"

(excludeFilter in (Compile, LessKeys.less)) := "*.exclude*"
```

To specify which version of the Less CSS compiler to use:

```scala
LessKeys.lessVersion in (Compile, LessKeys.less) := LessVersion.Less130
```

valid Less versions include:

 - `LessVersion.Less113`
 - `LessVersion.Less115`
 - `LessVersion.Less130`
 - `LessVersion.Less142` (the default)

## Usage

To compile Less CSS sources, use the `less` command in sbt. Read the installation instructions
above to see how to include Less CSS compilation as part of the regular `compile` command.

The default behaviour of the plugin is to scan your `src/main` directory and look files with the
extension `.less`.

These files are compiled to CSS using Less CSS v1.1.3 and placed in equivalent locations under
`target/scala-2.9.x/resource_managed`.

### Templating

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

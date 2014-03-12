# sbt-sass: SBT Sass CSS Plugin

See [README](../README.md) for copyright, licence, and acknowledgements.

[Simple Build Tool] plugin for compiling [Sass CSS] files.

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Sass CSS]: http://sass-lang.com/

## Configuration

Follow the installation instructions in [README](../README.md) and then...

If you're using [xsbt-web-plugin](https://github.com/siasia/xsbt-web-plugin "xsbt-web-plugin"),
add the output files to the webapp with:

```scala
(webappResources in Compile) <+= (resourceManaged in Compile)
```

To change the directory that is scanned, use:

```scala
(sourceDirectory in (Compile, SassKeys.sass)) <<= (sourceDirectory in Compile)(_ / "path" / "to" / "sass-files")
```

To specify multiple source directories, use:

```scala
(sourceDirectories in (Compile, SassKeys.sass)) <<=
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
(resourceManaged in (Compile, SassKeys.sass)) <<= (sourceDirectory in Compile)(_ / "webapp")
```

To automatically add generated CSS files to the application JAR:

```scala
(resourceGenerators in Compile) <+= (SassKeys.sass in Compile)
```

To cause the `sass` task to run automatically when you run `compile`:

```scala
(compile in Compile) <<= compile in Compile dependsOn (SassKeys.sass in Compile)
```

To use pretty-printing instead of regular CSS minification:

```scala
(SassKeys.prettyPrint in (Compile, SassKeys.sass)) := true
```

To include, exclude (filter) sass files:

```scala
(includeFilter in (Compile, SassKeys.sass)) := "*.include.scss"
```

```scala
(excludeFilter in (Compile, SassKeys.sass)) := "*.exclude*"
```

To specify which version of the Sass CSS compiler to use:

```scala
SassKeys.sassVersion in (Compile, SassKeys.sass) := SassVersion.Sass3214
```

To set output style used when compiling Sass

Valid output styles are ['nested(default), 'expanded, 'compact, 'compressed]

```scala
SassKeys.sassOutputStyle in (Compile, SassKeys.sass) := 'compressed
```

valid Sass versions include:

 - `SassVersion.Sass3214`
 - `SassVersion.Sass332` (the default)

## Usage

To compile Sass CSS sources, use the `sass` command in sbt. Read the installation instructions
above to see how to include Sass CSS compilation as part of the regular `compile` command.

The default behaviour of the plugin is to scan your `src/main` directory and look files with the
extension `.scss`.

These files are compiled to CSS using Sass CSS v3.3.2 and placed in equivalent locations under
`target/scala-2.9.x/resource_managed` or `target/scala-2.10.x/resource_managed`.

### Templating

It is sometime useful to template Sass files. For example, you might want scripts
to refer to one value during development and another value once deployed to production.

Javascript files with the extension `.template.scss` are passed through a [Mustache]
template processor before being passed to the Less compiler.

Property names and values are drawn from a properties file that is located and parsed
in an identical manner to the Lift web framework (though the implementation has no
dependency on Lift). The default location for property files is `src/main/resources/props`.
See the [Lift documentation] for file formats and naming conventions.

[Mustache]: http://mustache.github.com/
[Lift documentation]: http://www.assembla.com/spaces/liftweb/wiki/Properties

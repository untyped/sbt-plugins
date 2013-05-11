// import com.untyped.sbtassets._

// import sbt.{ Path => SbtPath, Resolvers => SbtResolvers, _ }

// object Synopsis extends App {
//   override def main(args: Array[String]) = {
//     val rootDir = file("/Users/dave/Desktop/asset-test")
//     val srcDir  = rootDir / "src"
//     val tempDir = rootDir / "temp"
//     val distDir = rootDir / "dist"

//     val mainSrc   = Selectors.Deps(Path.Root / "main", Resolvers.Extensions(List(".js"), Resolvers.Dir(srcDir)))

//     val jsSrc     = Rules.Filter(mainSrc, _.file.ext == ".js")
//     val coffeeSrc = Rules.Filter(mainSrc, _.file.ext == ".coffee")

//     val coffeeJs  = Rules.Coffee(tempDir / "js", coffeeSrc)
//     val dist      = Rules.Cat(distDir / "dist.js", Rules.Append(List(jsSrc, coffeeJs)))
//     val distMin   = Rules.UglifyJs(distDir / "dist.min.js", dist)

//     distMin.compile
//   }
// }

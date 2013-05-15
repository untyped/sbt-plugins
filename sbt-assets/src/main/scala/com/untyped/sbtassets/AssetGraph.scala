package com.untyped.sbtassets

import scala.collection.mutable

object AssetGraph {
  def apply(assets: List[Asset]): AssetGraph =
    new AssetGraph(assets)
}

class AssetGraph(val unsorted: List[Asset]) extends Ordering[Asset] {
  // Key depends on values
  val transitiveDependents = mutable.Map[Path, List[Path]]()

  // Values depend on key
  val transitivePrecedents = mutable.Map[Path, List[Path]]()

  for {
    a <- unsorted
    b <- a.dependencies
  } addDependencies(a.path, b)

  private def addDependencies(a: Path, b: Path) = {
    addDependency(a, b)
    for(c <- transitiveDependents.getOrElse(b, Nil)) addDependency(a, c)
    for(c <- transitivePrecedents.getOrElse(a, Nil)) addDependency(c, b)
  }

  // a depends on b
  private def addDependency(a: Path, b: Path) = {
    transitiveDependents.put(a, (b :: transitiveDependents.getOrElse(a, Nil)).distinct)
    transitivePrecedents.put(b, (a :: transitivePrecedents.getOrElse(b, Nil)).distinct)
  }

  lazy val sorted =
    unsorted.sorted(this)

  def compare(a: Asset, b: Asset) =
    if(transitivePrecedents.getOrElse(a.path, Nil) contains b.path) {
      -1 // a comes before b
    } else if(transitiveDependents.getOrElse(a.path, Nil) contains b.path) {
      +1 // a comes after b
    } else {
      // If there's no direct dependency, maintain the same relative position in the original list.
      // This allows us to (sort-of) rely on implicit dependencies from the orderings in require
      // statements.
      unsorted.indexOf(a) - unsorted.indexOf(b)
    }
}

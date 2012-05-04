package project

import tipi.core._
import tipi.core.Implicits._

class Echo extends Env(
  "echo" -> Transform.Simple {
    case Block(_, args, _) =>
      Text(args.get[String]("text").getOrElse("<no 'text' argument>"))
  }
)

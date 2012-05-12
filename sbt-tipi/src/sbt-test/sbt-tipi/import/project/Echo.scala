package project

import tipi.core._
import tipi.core.Implicits._

class Echo extends Env(
  "echo" -> Transform.Full {
    case (env, Block(_, args, _)) =>
      (env, Text(args.get[String](env, "text").getOrElse("<no 'text' argument>")))
  }
)

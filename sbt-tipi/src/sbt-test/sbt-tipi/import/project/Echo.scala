package project

import tipi.core._
import tipi.core.Implicits._

class Echo extends CustomEnv {
  def echo(env: Env, doc: Doc) = {
    doc match {
      case Block(_, args, _) =>
        (env, Text(args.string(env, "text").getOrElse("<no 'text' argument>")))
    }
  }
}

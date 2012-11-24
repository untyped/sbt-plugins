package project

import tipi.core._
import tipi.core.Implicits._

class Echo extends Env.Custom {
  def echo(env: Env, docIn: Doc): (Env, Doc) = {
    docIn match {
      case Block(_, args, _) =>
        (env, Text(args.string(env, "text").getOrElse("<no 'text' argument>")))
    }
  }
}

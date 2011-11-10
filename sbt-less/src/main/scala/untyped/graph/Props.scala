package untyped.graph

import java.io.{File,FileInputStream}
import java.net.InetAddress
import java.util.Properties

// Heavily based on Lift's Props. Reimplemented here as
// there were a few things in Lift's properties handling
// that made it hard to reuse.

/** Enumeration of available run modes */
object RunModes extends Enumeration {
  val Development = Value(1, "Development")
  val Test        = Value(2, "Test")
  val Staging     = Value(3, "Staging")
  val Production  = Value(4, "Production")
  val Pilot       = Value(5, "Pilot")
  val Profile     = Value(6, "Profile")
}

class Props(val basePath: File) {
  def addDot(s: String):String = s match {
    case null | "" => s
    case _ => s + "."
  }

  implicit def file2String(in: File) = in.getCanonicalPath
    
  lazy val modeName = addDot(Props.mode.toString.toLowerCase)
  lazy val userName = addDot(Props.userName.toString.toLowerCase)
  lazy val hostName = addDot(Props.hostName.toString.toLowerCase)

  // Lift has weird behaviour that we replicate:
  // If the mode is development, the modename is dropped from the search.
  // The 'spec' doesn't say anything about this but it's in the implementation so we do it.
  lazy val searchPaths: List[String] = {
    val mode = if (Props.mode == RunModes.Development) "" else modeName
    List(
      basePath + "/props/" + mode + userName + hostName + "props",
      basePath + "/props/" + mode + userName + "props",
      basePath + "/props/" + mode + hostName + "props",
      basePath + "/props/" + mode + "default." + "props",
      basePath + "/" + mode + userName + hostName + "props",
      basePath + "/" + mode + userName + "props",
      basePath + "/" + mode + hostName + "props",
      basePath + "/" + mode + "default." + "props")
  }

  lazy val file: Option[File] =
    searchPaths.map(new File(_)).find(_.exists)

  lazy val properties: Option[Properties] = {
    file map { propFile =>
      val props = new Properties()
      props.load(new FileInputStream(new File(propFile)))
      props
    }
  }
}

object Props {
  lazy val mode = {
    import RunModes._
    
    val mode = System.getProperty("run.mode")
    if (mode == null) 
      Development
    else
      mode.toLowerCase match {
        case "test" => Test
        case "staging" => Staging
        case "production" => Production
        case "pilot" => Pilot
        case "profile" => Profile
        case _ => Development
      }
  }

  lazy val userName = System.getProperty("user.name")
  lazy val hostName = InetAddress.getLocalHost.getHostName
}
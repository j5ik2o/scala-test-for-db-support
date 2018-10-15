import Utils._
import sbtrelease._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

val sonatypeURL = "https://oss.sonatype.org/service/local/repositories/"

val updateReadme: State => State = { state =>
  val extracted         = Project.extract(state)
  val scalaV            = extracted get scalaBinaryVersion
  val v                 = extracted get version
  val org               = extracted get organization
  val n                 = extracted get name
  val snapshotOrRelease = if (extracted get isSnapshot) "snapshots" else "releases"
  val readme            = "README.md"
  val readmeFile        = file(readme)
  val newReadme = Predef
    .augmentString(IO.read(readmeFile))
    .lines
    .map { line =>
      val matchReleaseOrSnapshot = line.contains("SNAPSHOT") == v.contains("SNAPSHOT")
      if (line.startsWith("libraryDependencies") && matchReleaseOrSnapshot) {
        s"""libraryDependencies += "${org}" %% "${n}" % "$v""""
      } else line
    }
    .mkString("", "\n", "\n")
  IO.write(readmeFile, newReadme)
  val git = new Git(extracted get baseDirectory)
  git.add(readme) ! state.log.toScalaProcessLogger
  git.commit(message = "update " + readme, sign = false) ! state.log.toScalaProcessLogger
  git.cmd("git diff HEAD^") ! state.log.toScalaProcessLogger
  state
}

commands += Command.command("updateReadme")(updateReadme)

val updateReadmeProcess: ReleaseStep = updateReadme

releaseCrossBuild := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
//  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  updateReadmeProcess,
  tagRelease,
  releaseStepCommand("+publishSigned"),
  setNextVersion,
  commitNextVersion,
  updateReadmeProcess,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)

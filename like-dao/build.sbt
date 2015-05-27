import BuildSettings.Versions._

name := """like-dao"""

version := "1.0"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"  % scalatestVersion % "test"
)


import scalariform.formatter.preferences._

name := """like-bearychat-robot"""

version := "1.0.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  ws,
  "org.jsoup"         %  "jsoup"        % "1.8.2",
  "org.scalaj"        %% "scalaj-http"  % "1.1.4",
  "com.typesafe.akka" %% "akka-actor"   % "2.3.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test"
)

routesGenerator := InjectedRoutesGenerator

//********************************************************
// Scalariform settings
//********************************************************
scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)

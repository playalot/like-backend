import scalariform.formatter.preferences._

name := """like-bearychat-robot"""

version := "1.0.0"

libraryDependencies ++= Seq(
  ws,
  "org.jsoup"         %  "jsoup"        % "1.8.3",
  "org.scalaj"        %% "scalaj-http"  % "1.1.5",
  "com.typesafe.akka" %% "akka-actor"   % "2.3.12",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.12" % "test"
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

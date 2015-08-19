import scalariform.formatter.preferences._

import com.tuplejump.sbt.yeoman.Yeoman

name := """like-dashboard"""

version := "1.1.0"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers +=  "NLPChina Releases" at "http://maven.ansj.org/"

resolvers += "nlpcn-mvn-repo" at "http://maven.nlpcn.org/"

libraryDependencies ++= Seq(
  cache,
  filters,
  ws,
  "com.qiniu"                 %  "qiniu-java-sdk"   % "7.0.4",
  "com.typesafe.akka"         %% "akka-remote"      % "2.3.12",
  "com.adrianhurt"            %% "play-bootstrap3"  % "0.4.4-P24",
  "net.codingwell"            %% "scala-guice"      % "4.0.0",
  "net.ceedubs"               %% "ficus"            % "1.1.2",
  "org.scalatest"             %% "scalatest"                 % "2.2.5"    % "test",
  specs2                      %  Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

Yeoman.yeomanSettings ++ Yeoman.withTemplates

//********************************************************
// Scalariform settings
//********************************************************
scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)


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
  "com.adrianhurt"            %% "play-bootstrap3"  % "0.4.4-P24",
  "net.ceedubs"               %% "ficus"            % "1.1.2",
  "org.webjars"               %% "webjars-play"     % "2.4.0",
  "org.webjars"               %  "bootstrap"        % "3.3.5",
  "org.scalatest"             %% "scalatest"        % "2.2.5" % "test",
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


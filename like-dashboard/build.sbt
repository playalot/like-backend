import scalariform.formatter.preferences._

name := """like-dashboard"""

version := "1.0.0"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers +=  "NLPChina Releases" at "http://maven.ansj.org/"

resolvers += "nlpcn-mvn-repo" at "http://maven.nlpcn.org/"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.nlpcn"                 %  "nlp-lang"         % "0.3",
  "org.ansj"                  %  "ansj_seg"         % "2.0.8" classifier "min",
  "mysql"                     %  "mysql-connector-java" % "5.1.36",
  "redis.clients"             %  "jedis"            % "2.7.2",
  "com.typesafe.akka"         %% "akka-remote"      % "2.3.12",
  "com.mohiva"                %% "play-silhouette"  % "3.0.0-RC2",
  "net.codingwell"            %% "scala-guice"      % "4.0.0",
  "com.mohiva"                %% "play-silhouette-testkit"   % "3.0.0-RC2" % "test",
  "org.scalatest"             %% "scalatest"                 % "2.2.5"     % "test",
  specs2                      %  Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
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

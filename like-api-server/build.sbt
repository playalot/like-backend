import scalariform.formatter.preferences._

name := """like-api-server"""

version := "1.1.0"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers +=  "NLPChina Releases" at "http://maven.ansj.org/"

resolvers += "nlpcn-mvn-repo" at "http://maven.nlpcn.org/"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  ws,
  cache,
  filters,
  "com.qiniu"                 %  "qiniu-java-sdk"   % "7.0.4.1",
  "com.typesafe.akka"         %% "akka-remote"      % "2.3.12",
  "com.github.nscala-time"    %% "nscala-time"      % "2.2.0",
  "com.typesafe.play"         %% "play-mailer"      % "3.0.1",
  "net.ceedubs"               %% "ficus"            % "1.1.2",
  "net.codingwell"            %% "scala-guice"      % "4.0.0",
  "com.github.cb372"          %% "scalacache-memcached"      % "0.6.4",
  "org.scalatest"             %% "scalatest"                 % "2.2.5" % "test"
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

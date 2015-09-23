import scalariform.formatter.preferences._

name := """like-api-server"""

version := "1.1.0"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers += "nlpcn-mvn-repo" at "http://maven.nlpcn.org/"

resolvers += Resolver.sonatypeRepo("snapshots")

val kamonVersion = "0.5.1"
val akkaVersion = "2.3.14"

libraryDependencies ++= Seq(
  ws,
  cache,
  filters,
  "com.typesafe.akka"         %% "akka-remote"      % akkaVersion,
  "com.github.nscala-time"    %% "nscala-time"      % "2.2.0",
  "com.typesafe.play"         %% "play-mailer"      % "3.0.1",
  "net.ceedubs"               %% "ficus"            % "1.1.2",
  "com.github.cb372"          %% "scalacache-memcached" % "0.6.4",
  "io.kamon"                  %% "kamon-core"           % kamonVersion,
//  "io.kamon"                  %% "kamon-akka"           % kamonVersion,
  "io.kamon"                  %% "kamon-play-24"        % kamonVersion,
  "io.kamon"                  %% "kamon-statsd"         % kamonVersion,
  "io.kamon"                  %% "kamon-log-reporter"   % kamonVersion,
  "io.kamon"                  %% "kamon-system-metrics" % kamonVersion,
  "org.aspectj"               %  "aspectjweaver"        % "1.8.6",
  "org.scalatest"             %% "scalatest"            % "2.2.5" % "test"
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

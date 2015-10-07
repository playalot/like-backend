import NativePackagerHelper._
import scalariform.formatter.preferences._

name := """like-event-cluster"""

version := "1.0.0"

val akkaVersion = "2.3.14"

libraryDependencies ++= Seq(
  "com.typesafe"      %  "config"         % "1.3.0",
  "redis.clients"     %  "jedis"          % "2.7.3",
  "joda-time"         %  "joda-time"      % "2.8.2",
  "org.joda"          %  "joda-convert"   % "1.8.1",
  "ch.qos.logback"    %  "logback-classic"% "1.1.3",
  "mysql"             %  "mysql-connector-java" % "5.1.36",
  "org.apache.spark"  %% "spark-core"     % "1.5.1",
  "org.apache.spark"  %% "spark-mllib"    % "1.5.1",
  "org.mongodb"       %% "casbah"         % "2.8.2",
  "com.typesafe.play" %% "play-ws"        % "2.4.3",
  "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
  "com.typesafe.akka" %% "akka-remote"    % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion,
  "com.typesafe.akka" %% "akka-contrib"   % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster"   % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core-experimental"  % "1.0",
  "com.typesafe.akka" %% "akka-stream-experimental"     % "1.0",
  "com.typesafe.akka" %% "akka-testkit"   % akkaVersion  % "test",
  "org.scalatest"     %% "scalatest"      % "2.2.5"   % "test"
)

//dependencyOverrides ++= Set(
//  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.4"
//)

mainClass in Compile := Some("com.likeorz.cluster.LikeEventClusterApp")

mappings in Universal <++= (packageBin in Compile, sourceDirectory ) map { (_, src) =>
  directory(src / "main" / "resources")
  // the user can override settings here
}

// add 'config' directory first in the classpath of the start script,
// an alternative is to set the config file locations via CLI parameters
// when starting the application
scriptClasspath := Seq("../config/") ++ scriptClasspath.value

bashScriptExtraDefines += """addJava "-Dconfig.resource=application.prod.conf""""

//********************************************************
// Scalariform settings
//********************************************************
scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)

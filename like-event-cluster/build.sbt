import NativePackagerHelper._
import scalariform.formatter.preferences._

name := """like-event-cluster"""

version := "1.0.0"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "com.typesafe"      %  "config"         % "1.3.0",
  "redis.clients"     %  "jedis"          % "2.7.3",
  "joda-time"         %  "joda-time"      % "2.8.1",
  "org.joda"          %  "joda-convert"   % "1.7",
  "ch.qos.logback"    %  "logback-classic"% "1.1.3",
  "mysql"             %  "mysql-connector-java" % "5.1.36",
  "org.spark-project.akka" %% "akka-actor"  % "2.3.4-1-spark",
  "org.spark-project.akka" %% "akka-remote" % "2.3.4-1-spark",
  "org.apache.spark"  %% "spark-core"     % "1.4.1",
  "org.apache.spark"  %% "spark-mllib"    % "1.4.1",
  "org.mongodb"       %% "casbah"         % "2.8.2",
  "com.typesafe.play" %% "play-ws"        % "2.4.2",
  "com.typesafe.akka" %% "akka-actor"     % "2.3.12",
  "com.typesafe.akka" %% "akka-remote"    % "2.3.12",
  "com.typesafe.akka" %% "akka-slf4j"     % "2.3.12",
  "com.typesafe.akka" %% "akka-contrib"   % "2.3.12",
  "com.typesafe.akka" %% "akka-cluster"   % "2.3.12",
  "com.typesafe.akka" %% "akka-http-core-experimental"  % "1.0",
  "com.typesafe.akka" %% "akka-stream-experimental"     % "1.0",
  "com.typesafe.akka" %% "akka-testkit"   % "2.3.12"  % "test",
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

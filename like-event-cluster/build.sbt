import NativePackagerHelper._

name := """like-event-cluster"""

version := "1.0.0"

libraryDependencies ++= Seq(
  "com.typesafe"      %  "config"         % "1.3.0",
  "joda-time"         %  "joda-time"      % "2.8.1",
  "org.joda"          %  "joda-convert"   % "1.7",
  "ch.qos.logback"    %  "logback-classic"% "1.1.3",
  "org.mongodb"       %% "casbah"         % "2.8.1",
  "com.typesafe.play" %% "play-ws"        % "2.4.2",
  "com.typesafe.akka" %% "akka-actor"     % "2.3.12",
  "com.typesafe.akka" %% "akka-remote"    % "2.3.12",
  "com.typesafe.akka" %% "akka-contrib"   % "2.3.12",
  "com.typesafe.akka" %% "akka-slf4j"     % "2.3.12",
  "com.typesafe.akka" %% "akka-cluster"   % "2.3.12",
  "com.typesafe.akka" %% "akka-http-core-experimental"  % "1.0-RC4",
  "com.typesafe.akka" %% "akka-stream-experimental"     % "1.0-RC4",
  "com.typesafe.akka" %% "akka-testkit"   % "2.3.12"  % "test",
  "org.scalatest"     %% "scalatest"      % "2.2.5"   % "test"
)


mainClass in Compile := Some("com.likeorz.LikeEventClusterApp")

mappings in Universal <++= (packageBin in Compile, sourceDirectory ) map { (_, src) =>
  directory(src / "main" / "resources")
  // the user can override settings here
}

// add 'config' directory first in the classpath of the start script,
// an alternative is to set the config file locations via CLI parameters
// when starting the application
scriptClasspath := Seq("../config/") ++ scriptClasspath.value
import scalariform.formatter.preferences._

name := """like-ml"""

version := "1.0.0"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "org.apache.lucene"         %  "lucene-analyzers-common" % "5.2.1",
  "redis.clients"             %  "jedis"            % "2.7.2",
  "org.nlpcn"                 %  "nlp-lang"         % "0.3",
  "org.ansj"                  %  "ansj_seg"         % "2.0.8" classifier "min",
  "joda-time"                 %  "joda-time"        % "2.8.1",
  "org.joda"                  %  "joda-convert"     % "1.7",
  "org.apache.spark"          %% "spark-core"       % "1.4.0",
  "org.apache.spark"          %% "spark-mllib"      % "1.4.0",
  "org.clapper"               %% "grizzled-slf4j"   % "1.0.2",
  "com.typesafe.akka"         %% "akka-actor"       % "2.3.12",
  "com.typesafe.akka"         %% "akka-remote"      % "2.3.12",
  "com.typesafe.akka"         %% "akka-http-core-experimental"  % "1.0-RC4",
  "com.typesafe.akka"         %% "akka-stream-experimental"     % "1.0-RC4",
  "com.github.scopt"          %% "scopt"            % "3.3.0",
  "org.scalatest"             %% "scalatest"        % "2.2.5" % "test"
)

//********************************************************
// Scalariform settings
//********************************************************
scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)

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
  "mysql"                     %  "mysql-connector-java" % "5.1.36",
  "org.apache.spark"          %% "spark-core"       % "1.4.1",
  "org.apache.spark"          %% "spark-mllib"      % "1.4.1",
  "org.clapper"               %% "grizzled-slf4j"   % "1.0.2",
  "com.github.scopt"          %% "scopt"            % "3.3.0",
  "org.scalatest"             %% "scalatest"        % "2.2.5" % "test"
)

mappings in Universal += {
  // we are using the reference.conf as default application.conf
  // the user can override settings here
  val conf = (resourceDirectory in Compile).value / "application.prod.conf"
  conf -> "conf/application.conf"
}

mappings in Universal += {
  // we are using the reference.conf as default application.conf
  // the user can override settings here
  val conf = (resourceDirectory in Compile).value / "log4j.properties"
  conf -> "conf/log4j.properties"
}

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

import scalariform.formatter.preferences._

name := """like-ml"""

version := "1.0.0"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "org.apache.lucene"         %  "lucene-analyzers-common" % "5.3.1",
  "redis.clients"             %  "jedis"            % "2.7.3",
  "ch.qos.logback"            %  "logback-classic"  % "1.1.3",
  "joda-time"                 %  "joda-time"        % "2.8.2",
  "org.joda"                  %  "joda-convert"     % "1.8.1",
  "mysql"                     %  "mysql-connector-java" % "5.1.37",
  "org.scalikejdbc"           %% "scalikejdbc"      % "2.2.9",
  "org.scalikejdbc"           %% "scalikejdbc-config" % "2.2.9",
  "org.scalikejdbc"           %% "scalikejdbc-jsr310" % "2.2.9",
  "org.mongodb"               %% "casbah"           % "2.8.2",
  "org.apache.spark"          %% "spark-core"       % "1.5.1",
  "org.apache.spark"          %% "spark-mllib"      % "1.5.1",
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

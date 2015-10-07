import scalariform.formatter.preferences._

name := """like-core"""

version := "1.1.0"

resolvers +=  "NLPChina Releases" at "http://maven.ansj.org/"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "cn.jpush.api"              %  "jpush-client"     % "3.2.7",
  "mysql"                     %  "mysql-connector-java" % "5.1.36",
  "org.nlpcn"                 %  "nlp-lang"         % "1.0.2",
  "redis.clients"             %  "jedis"            % "2.7.3",
  "com.qiniu"                 %  "qiniu-java-sdk"   % "7.0.4.2",
  "org.mongodb"               %% "casbah"           % "2.8.2",
  "net.codingwell"            %% "scala-guice"      % "4.0.0",
  "org.scalaz"                %% "scalaz-core"      % "7.1.4",
  "com.typesafe.play"         %% "play-slick"       % "1.1.0-RC3",
  "com.typesafe.play"         %% "play-slick-evolutions"     % "1.1.0-RC3",
  "com.mohiva"                %% "play-silhouette"           % "3.0.4",
  "com.mohiva"                %% "play-silhouette-testkit"   % "3.0.4"    % "test"
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

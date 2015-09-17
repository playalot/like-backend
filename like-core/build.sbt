import scalariform.formatter.preferences._

name := """like-core"""

version := "1.1.0"

resolvers +=  "NLPChina Releases" at "http://maven.ansj.org/"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "cn.jpush.api"              %  "jpush-client"     % "3.2.7",
  "mysql"                     %  "mysql-connector-java" % "5.1.36",
  "org.postgresql"            %  "postgresql"       % "9.4-1202-jdbc42",
  "org.nlpcn"                 %  "nlp-lang"         % "1.0",
  "org.ansj"                  %  "ansj_seg"         % "2.0.8" classifier "min",
  "redis.clients"             %  "jedis"            % "2.7.3",
  "org.scalaz"                %% "scalaz-core"      % "7.1.3",
  "com.typesafe.play"         %% "play-slick"       % "1.0.1",
  "com.typesafe.play"         %% "play-slick-evolutions"     % "1.0.1",
  "com.mohiva"                %% "play-silhouette"           % "3.0.2",
  "com.mohiva"                %% "play-silhouette-testkit"   % "3.0.2"    % "test"
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

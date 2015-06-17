import scalariform.formatter.preferences._

name := """like-apiserver"""

version := "1.1.0"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers +=  "NLPChina Releases" at "http://maven.ansj.org/"

resolvers += "nlpcn-mvn-repo" at "http://maven.nlpcn.org/"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  ws,
  cache,
  filters,
  "org.nlpcn"                 %  "nlp-lang"         % "0.3",
  "org.ansj"                  %  "ansj_seg"         % "2.0.8" classifier "min",
  "com.qiniu"                 %  "qiniu-java-sdk"   % "7.0.+",
  "mysql"                     %  "mysql-connector-java" % "5.1.35",
  "com.typesafe.play"         %% "play-slick"       % "1.0.0",
  "com.github.nscala-time"    %% "nscala-time"      % "2.0.0",
  "com.mohiva"                %% "play-silhouette"  % "3.0.0-RC1",
  "com.typesafe.play"         %% "play-mailer"      % "3.0.1",
  "net.codingwell"            %% "scala-guice"      % "4.0.0",
  "com.github.cb372"          %% "scalacache-redis" % "0.6.3",
  "com.github.cb372"          %% "scalacache-memcached"      % "0.6.3",
  "com.mohiva"                %% "play-silhouette-testkit"   % "3.0.0-RC1" % "test"
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

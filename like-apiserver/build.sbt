import scalariform.formatter.preferences._

name := """like-apiserver"""

version := "1.0.0"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers +=  "NLPChina Releases" at "http://maven.ansj.org/"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  ws,
  cache,
  filters,
  "org.ansj"                  %  "ansj_seg"         % "1.4.1",
  "com.qiniu"                 %  "qiniu-java-sdk"   % "7.0.2",
  "redis.clients"             %  "jedis"            % "2.7.2",
  "net.spy"                   %  "spymemcached"     % "2.11.4",
  "mysql"                     %  "mysql-connector-java" % "5.1.32",
  "com.typesafe.play"         %% "play-slick"       % "1.0.0-RC3",
  "com.github.nscala-time"    %% "nscala-time"      % "2.0.0",
  "com.mohiva"                %% "play-silhouette"  % "3.0.0-SNAPSHOT",
  "com.typesafe.play"         %% "play-mailer"      % "2.4.1",
  "net.codingwell"            %% "scala-guice"      % "4.0.0-beta5",
  "com.github.cb372"          %% "scalacache-redis" % "0.6.2",
  "com.mohiva"                %% "play-silhouette-testkit"   % "3.0.0-SNAPSHOT" % "test"
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

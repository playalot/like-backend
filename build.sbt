organization in ThisBuild := "com.likeorz"

scalaVersion in ThisBuild := "2.11.6"

scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.7",
  "-encoding", "UTF-8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-adapted-args",
  "-Ywarn-value-discard",
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code"
)

lazy val likeDao = (project in file("like-dao"))

lazy val likeApiServer = (project in file("like-apiserver"))
  .enablePlugins(PlayScala)

lazy val likeBot = (project in file("like-bot"))
  .enablePlugins(PlayScala)

lazy val root = (project in file(".")).aggregate(
	likeDao,
	likeApiServer,
  likeBot)

resolvers ++= Seq(
	  Resolver.mavenLocal,
	  Resolver.sbtPluginRepo("snapshots"),
	  Resolver.sonatypeRepo("snapshots"),
	  Resolver.typesafeRepo("snapshots"),
	  Resolver.typesafeIvyRepo("releases")
)


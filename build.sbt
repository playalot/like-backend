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

lazy val likeApiServer = (project in file("like-api-server"))
  .enablePlugins(PlayScala)

lazy val likeActorCluster = (project in file("like-actor-cluster"))

lazy val likeBearychatRobot = (project in file("like-bearychat-robot"))
  .enablePlugins(PlayScala)

lazy val root = (project in file(".")).aggregate(
	likeDao,
	likeApiServer,
  likeActorCluster,
  likeBearychatRobot)

resolvers ++= Seq(
	  Resolver.mavenLocal,
	  Resolver.sbtPluginRepo("snapshots"),
	  Resolver.sonatypeRepo("snapshots"),
	  Resolver.typesafeRepo("snapshots"),
	  Resolver.typesafeIvyRepo("releases")
)


scalacOptions ++= Seq( "-unchecked", "-deprecation" )

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
// The Play Plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.2")

addSbtPlugin("com.twitter" % "scrooge-sbt-plugin" % "3.16.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.2")

// Require GraphViz
addSbtPlugin("com.gilt" % "sbt-dependency-graph-sugar" % "0.7.5-1")

// Tools
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.9")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0")

addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.0")

addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.5")

// Web Plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.2.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-uglify" % "1.0.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("com.github.ddispaltro" % "sbt-reactjs" % "0.5.2")

addSbtPlugin("com.tuplejump" % "sbt-yeoman" % "0.8.1")
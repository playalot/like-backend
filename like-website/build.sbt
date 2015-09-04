name := """like-website"""

version := "1.1.0"

libraryDependencies ++= Seq(
  cache,
  "org.webjars" % "html5shiv" % "3.7.3",
  "org.webjars" % "respond"   % "1.4.2",
  "org.webjars" % "bootstrap" % "3.3.5"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

pipelineStages := Seq(uglify, digest, gzip)

includeFilter in (Assets, LessKeys.less) := "main.less"

LessKeys.compress := true
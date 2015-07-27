import com.tuplejump.sbt.yeoman.Yeoman

import scalariform.formatter.preferences._

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

RjsKeys.generateSourceMaps := false

pipelineStages := Seq(rjs, digest, gzip)

LessKeys.compress in Assets := true

excludeFilter in rjs := GlobFilter("*.jsx")

libraryDependencies ++= Seq(
  "org.webjars"   %% "webjars-play"    % "2.4.0",
  "org.webjars"    % "bootstrap"       % "3.3.5",
  "org.webjars"    % "jquery"          % "2.1.4",
  "org.webjars"    % "underscorejs"    % "1.8.3",
  "org.webjars"    % "requirejs-text"  % "2.0.14",
  "org.webjars"    % "react-router"    % "0.13.2",
  "org.webjars"    % "react"           % "0.13.3",
  "org.webjars"    % "react-bootstrap" % "0.23.7",
  "org.webjars.bower" % "react-router-bootstrap" % "0.13.4",
  "org.webjars.bower" % "reflux"       % "0.2.9",
  "org.webjars"    % "momentjs"        % "2.10.3",
  "org.webjars"    % "Pikaday"         % "1.3.0",
  "org.webjars"    % "json3"           % "3.3.2"
)

RjsKeys.webJarCdns := Map.empty

// RequireJS with sbt-rjs (https://github.com/sbt/sbt-rjs#sbt-rjs)
// ~~~
RjsKeys.paths += ("jsRoutes" -> ("/jsroutes" -> "empty:"))

RjsKeys.mainModule := "main"

ReactJsKeys.harmony := true

ReactJsKeys.es6module := true

Yeoman.yeomanSettings ++ Yeoman.withTemplates

//********************************************************
// Scalariform settings
//********************************************************

defaultScalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentClassDeclaration, false)
  .setPreference(PreserveDanglingCloseParenthesis, true)
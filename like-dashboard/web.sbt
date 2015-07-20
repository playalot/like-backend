//JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

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
  "org.webjars"    % "momentjs"        % "2.10.3",
  "org.webjars"    % "Pikaday"         % "1.3.0",
  "org.webjars"    % "json3"           % "3.3.2"
)

RjsKeys.webJarCdns := Map.empty

RjsKeys.mainModule := "main"

//RjsKeys.modules ++= Seq(
//  WebJs.JS.Object("name" -> "main",
//    "include" -> "main",
//    "generateSourceMaps" -> false
//  )
//)

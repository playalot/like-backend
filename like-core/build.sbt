import scalariform.formatter.preferences._

name := """like-core"""

version := "1.1.0"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "com.typesafe.play"         %% "play-slick"       % "1.0.0"
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

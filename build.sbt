name := "bot-detector"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.4.3",
  "org.apache.spark" %% "spark-streaming" % "2.4.3",
  "org.apache.spark" %% "spark-sql" % "2.4.3",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.3",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.3",
  "com.datastax.spark" % "spark-cassandra-connector_2.11" % "2.4.1",

  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "net.manub" %% "scalatest-embedded-kafka" % "0.16.0" % "test"
)

assemblyJarName in assembly := s"${name.value.replace(' ','-')}-${version.value}.jar"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

assemblyMergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case "log4j.properties"                                  => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf"                                    => MergeStrategy.concat
  case _                                                   => MergeStrategy.first
}


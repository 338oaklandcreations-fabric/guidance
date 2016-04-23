import AssemblyKeys._
import com.typesafe.sbt.SbtStartScript

name := "Guidance"

version := "1.0"

scalaVersion := "2.11.7"

assemblySettings

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "Spray" at "http://repo.spray.io"

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

seq(SbtStartScript.startScriptForClassesSettings: _*)

libraryDependencies ++= {
  val sprayV = "1.3.3"
  val akkaV = "2.3.6"
  Seq(
    "io.spray"            %%  "spray-can"            % sprayV,
    "io.spray"            %%  "spray-routing"        % sprayV,
    "io.spray"            %%  "spray-testkit"        % sprayV  % "test",
    "io.spray"            %%  "spray-json"           % "1.3.1",
    "com.typesafe.slick"  %%  "slick"                % "2.1.0",
    "com.typesafe.akka"   %%  "akka-actor"           % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"         % akkaV % "test",
    "com.typesafe.akka"   %%  "akka-slf4j"           % akkaV,
    "org.slf4j"           %   "slf4j-api"            % "1.7.10",
    "ch.qos.logback"      %   "logback-classic"      % "1.1.3",
    "org.specs2"          %%  "specs2-core"          % "2.3.11" % "test",
    "mysql"               %   "mysql-connector-java" % "latest.release",
    "joda-time"           %   "joda-time"            % "2.7",
    "org.joda"            %   "joda-convert"         % "1.2")
}

Revolver.settings
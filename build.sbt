import scala.util.Try


name := "belch"

organization := "im.mange"

version := Try(sys.env("TRAVIS_BUILD_NUMBER")).map("0.0." + _).getOrElse("1.0-SNAPSHOT")

scalaVersion:= "2.11.7"

//crossScalaVersions := Seq("2.10.4"/*, "2.11.0"*/)

resolvers ++= Seq(
  "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies ++= Seq(
//	"junit" % "junit" % "4.11" % "test->default",
//	"org.scalatest" %% "scalatest" % "2.2.0" % "test"
)

//libraryDependencies := {
//  CrossVersion.partialVersion(scalaVersion.value) match {
//    case Some((2, scalaMajor)) if scalaMajor >= 11 => libraryDependencies.value :+ "org.scala-lang.modules" %% "scala-xml" % "1.0.1"
//    case _ => libraryDependencies.value
//  }
//}

//net.virtualvoid.sbt.graph.Plugin.graphSettings

//TIP: for lib publishing ... start uncommenting from here
//sonatypeSettings

//publishTo <<= version { project_version ⇒
//  val nexus = "https://oss.sonatype.org/"
//  if (project_version.trim.endsWith("SNAPSHOT"))
//    Some("snapshots" at nexus + "content/repositories/snapshots")
//  else
//    Some("releases" at nexus + "service/local/staging/deploy/maven2")
//}

//publishMavenStyle := true

//publishArtifact in Test := false
//TIP: for lib publishing ... finish uncommenting here

homepage := Some(url("https://github.com/alltonp/belch"))

licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

//TIP: for lib publishing ... uncommenting this too
//credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USER"), System.getenv("SONATYPE_PASSWORD"))

pomExtra :=
    <scm>
      <url>git@github.com:alltonp/belch.git</url>
      <connection>scm:git:git@github.com:alltonp/belch.git</connection>
    </scm>
    <developers>
      <developer>
        <id>alltonp</id>
      </developer>
    </developers>

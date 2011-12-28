resolvers ++= Seq(
    DefaultMavenRepository,
    Resolver.url("Play", url("http://download.playframework.org/ivy-releases/"))(Resolver.ivyStylePatterns),
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq("play" %% "play" % "2.0-RC1-SNAPSHOT",
    "se.scalablesolutions.akka" % "akka-remote" % "1.2",
    "com.typesafe.akka.demo" % "shared" % "1.0-SNAPSHOT")
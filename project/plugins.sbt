resolvers += Classpaths.typesafeResolver

resolvers += "Typesafe Timestamp Repo" at "http://repo.typesafe.com/typesafe/maven-timestamps/"

//addSbtPlugin("com.typesafe.sbtscalariform" % "sbt-scalariform" % "0.1.4")

addSbtPlugin("com.typesafe.akka" % "akka-sbt-plugin" % "2.0-M1")

resolvers ++= Seq(
  "Maven Repository" at "http://repo1.maven.org/maven2/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.url("Play Ivy Repo", new java.net.URL("http://download.playframework.org/ivy-releases/"))(Resolver.ivyStylePatterns)
)
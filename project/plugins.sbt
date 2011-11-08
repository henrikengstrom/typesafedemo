resolvers += Classpaths.typesafeResolver

resolvers += "Typesafe Timestamp Repo" at "http://repo.typesafe.com/typesafe/maven-timestamps/"

addSbtPlugin("com.typesafe.sbtscalariform" % "sbt-scalariform" % "0.1.4")

addSbtPlugin("com.typesafe.akka" % "akka-sbt-plugin" % "2.0-20111024-000453")


externalResolvers <<= resolvers map { Resolver.withDefaultResolvers(_, mavenCentral = true, scalaTools = false) }

resolvers += Classpaths.typesafeResolver

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.sbtscalariform" % "sbtscalariform" % "0.3.1")

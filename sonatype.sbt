credentials ++= (for {
  user <- sys.env.get("SONATYPE_USERNAME")
  pass <- sys.env.get("SONATYPE_PASSWORD")
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)).toSeq

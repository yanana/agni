credentials ++= (for {
  user <- sys.env.get("SONATYPE_USERNAME")
  pass <- sys.env.get("SONATYPE_PASSWORD")
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)).toSeq

pgpSecretRing := sys.env.get("GPG_SECRET_RING_PATH").fold(pgpSecretRing.value)(file(_))

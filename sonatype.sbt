credentials ++= (for {
  user <- sys.env.get("SONATYPE_USERNAME")
  pass <- sys.env.get("SONATYPE_PASSWORD")
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)).toSeq

pgpPassphrase := sys.env.get("PGP_PASS").map(_.toCharArray)
pgpSecretRing := sys.env.get("PGP_SECRET_RING_PATH").fold(pgpSecretRing.value)(file(_))

useGpg := false

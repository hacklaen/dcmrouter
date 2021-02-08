# Generates a keystore containing the private and root-public key for the alias "identityJava".
# The keys are valid for 20 years (7300 days).

keytool.exe -genkey -keyalg RSA -keystore identityJava.jks -alias identityJava -dname "cn=identityJava, o=dcm4che, c=de" -validity 7300 -keypass secret -storepass secret

pause
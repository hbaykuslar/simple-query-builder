
# we can set all module versions with below command
./mvnw versions:set -DnewVersion=0.1.2-snapshot # for snapshot version
./mvnw versions:set -DnewVersion=0.1.2 # for release version

# we can set release version and deploy to maven central and set to
./mvnw release:clean release:prepare
./mvnw release:perform

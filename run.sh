#!/bin/sh -ex

REPOSITORY="${HOME}/var/cache/maven-repository"

CLASS_PATH="target/com.io7m.resolver-0.0.1.jar"
CLASS_PATH="${CLASS_PATH}:${REPOSITORY}/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar"
CLASS_PATH="${CLASS_PATH}:${REPOSITORY}/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar"
CLASS_PATH="${CLASS_PATH}:${REPOSITORY}/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar"

# exec java -cp "${CLASS_PATH}" com.io7m.resolver.Main

MODULE_PATH="external"
MODULE_PATH="${MODULE_PATH}:${REPOSITORY}/org/slf4j/slf4j-api/1.7.25/"
MODULE_PATH="${MODULE_PATH}:${REPOSITORY}/ch/qos/logback/logback-core/1.2.3/"
MODULE_PATH="${MODULE_PATH}:${REPOSITORY}/ch/qos/logback/logback-classic/1.2.3/"

mkdir -p external
cp -v "${REPOSITORY}/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar" external
cp -v "${REPOSITORY}/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar" external
cp -v "${REPOSITORY}/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar" external
cp -v target/com.io7m.resolver-0.0.1.jar external

exec java -verbose:module -p external -m com.io7m.resolver/com.io7m.resolver.Main

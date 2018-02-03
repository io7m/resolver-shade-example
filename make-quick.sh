#!/bin/sh -ex

exec mvn clean package \
  -Denforcer.skip=true \
  -Dmaven.source.skip=true \
  -Dcheckstyle.skip=true \
  -Dkstructural.skip=true \
  -Dmaven.javadoc.skip=true \
  -DskipTests=true \
  -Dassembly.skipAssembly=true \
  "$@"


#!/usr/bin/env bash
# Writes ~/.gradle/gradle.properties for one bench variant. Starts from the real CI file so that
# "current" is exactly what the PR workflow uses today.
#   current           .github/ci-gradle.properties as is
#   uncapped          drops org.gradle.workers.max, so Gradle uses one worker per CPU
#   uncapped-inc      also drops kotlin.incremental=false, back to the Kotlin default
#   uncapped-lowheap  uncapped with a heap that fits a 7 GB runner
set -eu
mkdir -p ~/.gradle
cp .github/ci-gradle.properties ~/.gradle/gradle.properties
# The source file has no trailing newline, so anything appended would join its last line.
echo >> ~/.gradle/gradle.properties
case "$1" in
  current) ;;
  uncapped) sed -i.bak '/org.gradle.workers.max/d' ~/.gradle/gradle.properties ;;
  uncapped-inc) sed -i.bak -e '/org.gradle.workers.max/d' -e '/kotlin.incremental/d' ~/.gradle/gradle.properties ;;
  uncapped-lowheap)
    sed -i.bak '/org.gradle.workers.max/d' ~/.gradle/gradle.properties
    echo 'org.gradle.jvmargs=-Xmx5g -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -XX:+UseParallelGC -XX:MetaspaceSize=1g -XX:ReservedCodeCacheSize=512m' >> ~/.gradle/gradle.properties
    ;;
  *) echo "unknown props variant $1"; exit 1 ;;
esac
rm -f ~/.gradle/gradle.properties.bak
cat ~/.gradle/gradle.properties

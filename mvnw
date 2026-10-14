#!/bin/sh
set -eu

MAVEN_VERSION=3.9.11
MAVEN_ARCHIVE="apache-maven-${MAVEN_VERSION}-bin.zip"
MAVEN_BASE="${MAVEN_USER_HOME:-${HOME}/.m2/wrapper/dists}"
MAVEN_DIR="${MAVEN_BASE}/apache-maven-${MAVEN_VERSION}"
MAVEN_HOME="${MAVEN_DIR}/apache-maven-${MAVEN_VERSION}"

if [ ! -x "${MAVEN_HOME}/bin/mvn" ]; then
  mkdir -p "${MAVEN_DIR}"
  ARCHIVE_PATH="${MAVEN_DIR}/${MAVEN_ARCHIVE}"
  curl -fL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/${MAVEN_ARCHIVE}" -o "${ARCHIVE_PATH}"
  unzip -q -o "${ARCHIVE_PATH}" -d "${MAVEN_DIR}"
  rm -f "${ARCHIVE_PATH}"
fi

exec "${MAVEN_HOME}/bin/mvn" "$@"

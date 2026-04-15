#!/bin/bash
# Script para baixar e usar Maven automaticamente

MAVEN_VERSION="3.9.6"
MAVEN_DIR="$HOME/.m2/wrapper/apache-maven-$MAVEN_VERSION"
MAVEN_BIN="$MAVEN_DIR/bin/mvn"

if [ ! -f "$MAVEN_BIN" ]; then
    echo "Baixando Apache Maven $MAVEN_VERSION..."
    mkdir -p "$HOME/.m2/wrapper"
    curl -fsSL "https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz" | tar -xz -C "$HOME/.m2/wrapper"
fi

exec "$MAVEN_BIN" "$@"

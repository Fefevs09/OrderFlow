#!/bin/bash
# Script para baixar e usar Maven automaticamente com múltiplos mirrors

MAVEN_VERSION="3.9.6"
MAVEN_DIR="$HOME/.m2/wrapper/apache-maven-$MAVEN_VERSION"
MAVEN_BIN="$MAVEN_DIR/bin/mvn"

download_maven() {
    local url="$1"
    echo "Tentando baixar de: $url"
    curl -fsSL "$url" | tar -xz -C "$HOME/.m2/wrapper" 2>/dev/null
    return $?
}

if [ ! -f "$MAVEN_BIN" ]; then
    echo "Baixando Apache Maven $MAVEN_VERSION..."
    mkdir -p "$HOME/.m2/wrapper"

    # Tenta múltiplas URLs em ordem
    if ! download_maven "https://downloads.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz"; then
        if ! download_maven "https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz"; then
            if ! download_maven "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz"; then
                echo "ERRO: Não foi possível baixar o Maven de nenhuma fonte"
                exit 1
            fi
        fi
    fi
fi

exec "$MAVEN_BIN" "$@"

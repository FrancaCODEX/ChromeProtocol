#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p target/domain
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -d target/domain \
  src/main/java/br/com/chromeprotocol/Appearance.java \
  src/main/java/br/com/chromeprotocol/GameState.java \
  src/main/java/br/com/chromeprotocol/Catalog.java \
  src/main/java/br/com/chromeprotocol/Calendar.java \
  src/main/java/br/com/chromeprotocol/Tour.java \
  src/main/java/br/com/chromeprotocol/Events.java \
  src/main/java/br/com/chromeprotocol/PersonalLife.java \
  src/main/java/br/com/chromeprotocol/GameEngine.java \
  src/test/java/br/com/chromeprotocol/EngineChecks.java
java -cp target/domain br.com.chromeprotocol.EngineChecks

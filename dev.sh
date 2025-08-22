#!/usr/bin/env bash

CFG_FILE="./src/main/resources/rusherhack-plugin.json"
JAVA_FILE="src/main/java/de/kybe/KybesUtils/KybesUtils.java"

MIXIN_ENTRY="mixins.kybes-utils.json"

if [[ "$1" != "true" && "$1" != "false" ]]; then
  echo "Usage: $0 true|false"
  exit 1
fi

if [[ "$1" == "true" ]]; then
  echo "Enabling dev mode..."

  sed -i 's/public static boolean TESTING = .*/public static boolean TESTING = true;/' "$JAVA_FILE"

  jq --arg mixin "$MIXIN_ENTRY" '
    if .["Mixin-Configs"] then
      .["Mixin-Configs"] |= map(select(. != $mixin))
      | if .["Mixin-Configs"] == [] then del(.["Mixin-Configs"]) else . end
    else
      .
    end
  ' "$CFG_FILE" > "$CFG_FILE.tmp" && mv "$CFG_FILE.tmp" "$CFG_FILE"

elif [[ "$1" == "false" ]]; then
  echo "Disabling dev mode..."

  sed -i 's/public static boolean TESTING = .*/public static boolean TESTING = false;/' "$JAVA_FILE"

  jq --arg mixin "$MIXIN_ENTRY" '
      if .["Mixin-Configs"] then
        if (.["Mixin-Configs"] | index($mixin)) then
          . # already present
        else
          .["Mixin-Configs"] += [$mixin]
        end
      else
        . + {"Mixin-Configs": [$mixin]}
      end
    ' "$CFG_FILE" > "$CFG_FILE.tmp" && mv "$CFG_FILE.tmp" "$CFG_FILE"
fi

echo "Done."
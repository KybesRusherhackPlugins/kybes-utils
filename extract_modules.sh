#!/usr/bin/env bash

MAIN_FILE="src/main/java/de/kybe/KybesUtils/KybesUtils.java"
SRC_DIR="src/main/java"
README="README.md"

TMP_FILE=$(mktemp)

grep -oP 'registerFeature\s*\(\s*new\s+\K\w+' "$MAIN_FILE" | sort -u | while read -r CLASS; do
    FILE=$(find "$SRC_DIR" -type f -name "${CLASS}.java" | head -n1)
    if [[ -z "$FILE" ]]; then
        NAME="$CLASS"
        DESC="[file not found]"
    else
        SUPER_LINE=$(grep -oP 'super\s*\([^)]*\)' "$FILE" | head -n1)
        NAME=$(echo "$SUPER_LINE" | grep -oP '"[^"]+"' | sed -n '1p' | sed 's/"//g')
        DESC=$(echo "$SUPER_LINE" | grep -oP '"[^"]+"' | sed -n '2p' | sed 's/"//g')
        [[ -z "$NAME" ]] && NAME="$CLASS"
        [[ -z "$DESC" ]] && DESC="no description"
    fi
    echo "- $NAME"
    echo ": $DESC"
    echo ""
done > "$TMP_FILE"

awk -v newfile="$TMP_FILE" '
BEGIN {insert=0; skip=0}
/^---/ {
    if (insert==0) {
        print
        print ""
        # Insert new content
        while ((getline line < newfile) > 0) print line
        print "---"
        insert=1
        skip=1
        next
    } else if (skip==1) {
        skip=0
        next
    }
}
{if(skip==0) print}
' "$README" > "${README}.tmp" && mv "${README}.tmp" "$README"

rm "$TMP_FILE"
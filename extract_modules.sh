#!/usr/bin/env bash

MAIN_FILE="src/main/java/de/kybe/KybesUtils/KybesUtils.java"
SRC_DIR="src/main/java"
README="README.md"

TMP_FILE=$(mktemp)

generate_entries() {
    local METHOD="$1"
    local HEADER="$2"

    echo "## $HEADER"
    echo ""
    echo "---"
    echo ""

    grep -oP "$METHOD\s*\(\s*new\s+\K\w+" "$MAIN_FILE" | sort -u | while read -r CLASS; do
        FILE=$(find "$SRC_DIR" -type f -name "${CLASS}.java" | head -n1)
        if [[ -z "$FILE" ]]; then
            NAME="$CLASS"
            DESC="[file not found]"
        else
            SUPER_LINE=$(grep -oP 'super\s*\([^)]*\)' "$FILE" | head -n1)
            NAME=$(echo "$SUPER_LINE" | grep -oP '"(\\.|[^"\\])*"' | sed -n '1p' | sed 's/^"//;s/"$//')
            DESC=$(echo "$SUPER_LINE" | grep -oP '"(\\.|[^"\\])*"' | sed -n '2p' | sed 's/^"//;s/"$//')
            [[ -z "$NAME" ]] && NAME="$CLASS"
        fi

        echo "- $NAME"
        if [[ -n "$DESC" && "$DESC" != "[file not found]" ]]; then
            echo ": $DESC"
        fi
        echo ""
    done

    echo "---"
    echo ""
}

{
    generate_entries "moduleManager\.registerFeature" "Modules"
    generate_entries "commandManager\.registerFeature" "Commands"
    generate_entries "windowManager\.registerFeature" "Windows"
    generate_entries "hudManager\.registerFeature" "HUDs"
} > "$TMP_FILE"

awk -v newfile="$TMP_FILE" '
BEGIN {insert=0}
/^# Features/ {
    print
    print ""
    print "---"
    print ""
    while ((getline line < newfile) > 0) print line
    insert=1
    next
}
insert==0 {print}
' "$README" > "${README}.tmp" && mv "${README}.tmp" "$README"

rm "$TMP_FILE"
#!/bin/bash
set -e 

if [ "$#" -ne 2 ]; then
  echo "Usage: <input file> <output file>"
  exit 1
fi

INPUT=$1
OUTPUT=$2


sed -E "s/node /node version='1' /" $INPUT \
 | sed -E "s/way /way version='1' /" \
 | sed -E "s/relation /relation version='1' /" \
 | sed -E "s/(.*)version='1'(.*)version=('|\")(.*)('|\")(.*)/\1 \2 version=\3\4\5\6/"\
 | sed -E "s/<tag k=('|\")(note|fixme).*//" \
 > $OUTPUT
# First, add version attributes. Files created via josm don't have a version. (13-15)
# Then, remove duplicate version attributes (16)
# Then, remove notes and fixmes -- they get broken with the added version tags because they contain the words way node relation.

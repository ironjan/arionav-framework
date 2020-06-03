#!/bin/bash
set -e 

if [ "$#" -ne 2 ]; then
  echo "Usage: <input file> <output file>"
  exit 1
fi

INPUT=$1
OUTPUT=$2

osmosis --rx enableDateParsing=no $INPUT --sort type="TypeThenId" --wx $OUTPUT

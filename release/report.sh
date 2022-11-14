#!/bin/bash
# Script to generate a single file with all the metadata/specs

report_date=$(date +%Y_%m_%d)
echo "Generating report ${report_date}..."

mvn -f .. prepare-package -PtemplatesSpec -DskipTests -Djib.skip -Djacoco.skip

rm -rf "/tmp/report_${report_date}.json" "report_${report_date}.json"

for file in $(find .. -name '*-generated-metadata.json' | grep -v 'spec-generated-metadata.json' | sort); do
  echo "Reading ${file}..."
  cat $file >> "/tmp/report_${report_date}.json"

done


cat "/tmp/report_${report_date}.json" | jq --slurp '.' > "report_${report_date}.json"
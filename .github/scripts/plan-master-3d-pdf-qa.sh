#!/usr/bin/env bash
set -euo pipefail

cd "$GITHUB_WORKSPACE/plan-master-3d"
RESULTS_DIR="app/build/outputs/androidTest-results/connected/debug"
REPORT_DIR="app/build/reports/androidTests/connected/debug"

echo "Running PDF instrumentation tests on the Android emulator..."
gradle connectedDebugAndroidTest --info

echo "Checking that instrumentation tests actually executed..."
if [[ ! -d "$RESULTS_DIR" ]]; then
  echo "ERROR: Android instrumentation result directory is missing: $RESULTS_DIR"
  find app/build -maxdepth 6 -type f \( -name "*.xml" -o -name "*.html" \) -print || true
  exit 1
fi

find "$RESULTS_DIR" -type f -name "*.xml" -print
if ! grep -R -E 'tests="[1-9][0-9]*"' "$RESULTS_DIR" --include="*.xml"; then
  echo "ERROR: No executed Android instrumentation tests were found."
  [[ ! -d "$REPORT_DIR" ]] || find "$REPORT_DIR" -type f -maxdepth 3 -print
  exit 1
fi

mkdir -p pdf-artifacts
adb exec-out run-as ru.planmaster3d.app cat files/PLAN-MASTER-3D-technical-test.pdf > pdf-artifacts/PLAN-MASTER-3D-technical-test.pdf
adb exec-out run-as ru.planmaster3d.app cat files/PLAN-MASTER-3D-proposal-test.pdf > pdf-artifacts/PLAN-MASTER-3D-proposal-test.pdf

for pdf in pdf-artifacts/*.pdf; do
  test -s "$pdf"
  test "$(head -c 5 "$pdf")" = "%PDF-"
  test "$(wc -c < "$pdf")" -gt 5000
  echo "Validated $pdf ($(wc -c < "$pdf") bytes)"
done

echo "PDF QA completed successfully."

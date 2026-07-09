#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
INTELLIJ_MVN="/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn"

if command -v mvn >/dev/null 2>&1; then
  MVN_BIN="mvn"
elif [[ -x "${INTELLIJ_MVN}" ]]; then
  MVN_BIN="${INTELLIJ_MVN}"
else
  echo "Cannot find mvn. Install Maven or keep IntelliJ IDEA Maven at: ${INTELLIJ_MVN}" >&2
  exit 1
fi

"${MVN_BIN}" -q -DskipTests package -f "${REPO_ROOT}/pom.xml"
java -cp "${REPO_ROOT}/target/classes" fca.experiment.BuildCategoricalExperimentInput "$@"

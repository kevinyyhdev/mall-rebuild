#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

python3 - "$repository_root" <<'PY'
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

repository_root = Path(sys.argv[1])
modules = (
    "mall-common",
    "mall-persistence",
    "mall-security",
    "mall-admin",
    "mall-portal",
    "mall-search",
)

total_tests = 0
total_failures = 0
total_errors = 0
total_skipped = 0

for module in modules:
    reports = repository_root / module / "target" / "surefire-reports"
    if not reports.is_dir():
        raise SystemExit(f"ERROR: missing Surefire report directory for {module}: {reports}")

    xml_reports = sorted(reports.glob("TEST-*.xml"))
    if not xml_reports:
        raise SystemExit(f"ERROR: no Surefire XML reports found for {module}")

    counts = {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}
    for report in xml_reports:
        try:
            root = ET.parse(report).getroot()
        except ET.ParseError as error:
            raise SystemExit(f"ERROR: malformed Surefire XML {report}: {error}") from error

        tag = root.tag.rsplit("}", 1)[-1]
        suites = [root] if tag == "testsuite" else list(root.findall("./testsuite"))
        if not suites:
            raise SystemExit(f"ERROR: no test suite found in {report}")

        for suite in suites:
            for attribute in counts:
                raw_value = suite.attrib.get(attribute, "0")
                try:
                    counts[attribute] += int(raw_value)
                except ValueError as error:
                    raise SystemExit(
                        f"ERROR: non-numeric {attribute}={raw_value!r} in {report}"
                    ) from error

    if counts["tests"] < 1:
        raise SystemExit(f"ERROR: {module} executed zero tests")
    if counts["failures"] or counts["errors"]:
        raise SystemExit(
            f"ERROR: {module} reports failures={counts['failures']} errors={counts['errors']}"
        )

    print(
        f"{module}: tests={counts['tests']} failures={counts['failures']} "
        f"errors={counts['errors']} skipped={counts['skipped']}"
    )
    total_tests += counts["tests"]
    total_failures += counts["failures"]
    total_errors += counts["errors"]
    total_skipped += counts["skipped"]

print(
    f"TOTAL: tests={total_tests} failures={total_failures} "
    f"errors={total_errors} skipped={total_skipped}"
)
PY

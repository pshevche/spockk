from pathlib import Path

FIXTURE_TREE = Path(__file__).parent / "fixtures"
MANIFEST = {"classes": [{"key": "org.spockframework.smoke.A",
                         "features": [{"name": "one", "hash": "aaaa1111"}]}]}
KEY = "org.spockframework.smoke.A#one"
FEATURE = "one"
CLASS = MANIFEST["classes"][0]


def ported(source="PortedTest.kt:10"):
    from scanner import Coverage
    return Coverage(status="ported", gap=None, source=source)


def pending(gap, source="PortedTest.kt:20"):
    from scanner import Coverage
    return Coverage(status="pending", gap=gap, source=source)


def na_exclusion(reason="Kotlin has no GString"):
    from exclusions import Exclusion
    return Exclusion(status="not-applicable", reason=reason, gap=None, decided="2026-09-19")


def class_with(feature_count):
    return {"key": "org.spockframework.smoke.Big",
            "features": [{"name": f"f{i}", "hash": f"{i:08x}"} for i in range(feature_count)]}

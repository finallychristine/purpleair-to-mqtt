import os
import tomllib
from pathlib import Path

catalog = Path(os.environ["GITHUB_WORKSPACE"]) / "gradle/libs.versions.toml"
with catalog.open("rb") as file:
    print(f"version={tomllib.load(file)['versions']['jdk']}")

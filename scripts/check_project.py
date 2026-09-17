#!/usr/bin/env python3
"""Fast, dependency-free checks. Scans nested modules and fails on read/parse errors."""
import argparse
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

SKIP = {'.git', '.gradle', '.kotlin', '.idea', 'build', 'node_modules', '__pycache__'}

def files(root):
    import os
    for base, dirs, names in os.walk(root):
        dirs[:] = [d for d in dirs if d not in SKIP and not d.startswith('.')]
        for name in names:
            yield Path(base) / name

PLATFORM_IMPORT = re.compile(
    r'\s*import\s+('
    r'android\.|java\.|javax\.'
    r'|androidx\.(?!compose|lifecycle|navigation|savedstate)'
    r')'
)
DISPATCHERS_IMPORT = re.compile(r'\s*import\s+kotlinx\.coroutines\.Dispatchers$')
# DispatcherProvider.kt is the sanctioned abstraction everything else must inject
# instead of importing Dispatchers directly; commonTest may still call
# Dispatchers.setMain/resetMain to control the test dispatcher.
DISPATCHERS_ALLOWED_FILE = 'DispatcherProvider.kt'

def purity(root):
    errors = []
    for path in files(root):
        if path.suffix != '.kt' or not {'commonMain', 'commonTest'}.intersection(path.parts):
            continue
        check_dispatchers = 'commonMain' in path.parts and path.name != DISPATCHERS_ALLOWED_FILE
        for line, text in enumerate(path.read_text().splitlines(), 1):
            if PLATFORM_IMPORT.match(text) or (check_dispatchers and DISPATCHERS_IMPORT.match(text)):
                errors.append(f'{path.relative_to(root)}:{line}: platform-only import: {text.strip()}')
    return errors

def check(root, only=False):
    errors = purity(root)
    if only:
        return errors
    base = root / 'core/designsystem/src/commonMain/composeResources'
    def strings(path):
        parsed = ET.parse(path)
        rows = parsed.findall('string')
        result = {e.attrib['name']: ''.join(e.itertext()) for e in rows}
        if len(result) != len(rows):
            errors.append(f'{path}: duplicate string key')
        return result
    default = strings(base / 'values/strings.xml')
    for path in base.glob('values-*/strings.xml'):
        localized = strings(path)
        if default.keys() != localized.keys():
            errors.append(f'{path}: string keys differ from default locale')
        for key in default.keys() & localized.keys():
            placeholders = lambda s: sorted(re.findall(r'%(?:\d+\$)?[dsf]', s))
            if placeholders(default[key]) != placeholders(localized[key]):
                errors.append(f'{path}: format placeholders differ for {key}')
    for path in files(root):
        if path.suffix == '.xml':
            ET.parse(path)
        if path.name == 'build.gradle.kts':
            module = path.parent.relative_to(root).parts
            source = path.read_text()
            dependencies = re.findall(r'project\("(:[^"\n]+)"\)', source)
            dependencies += [':' + item.replace('.', ':') for item in re.findall(r'projects\.((?:core|feature)\.[a-zA-Z0-9_.]+)', source)]
            for dependency in dependencies:
                parts = dependency.strip(':').split(':')
                if module and module[0] == 'core' and parts[0] in {'feature', 'androidApp', 'sharedApp'}:
                    errors.append(f'{path}: core cannot depend on {dependency}')
                if module and module[0] == 'feature' and parts[0] == 'feature' and len(module) > 1 and module[1] != parts[1]:
                    errors.append(f'{path}: cross-feature dependency {dependency}')
    manifest = root / 'androidApp/src/main/AndroidManifest.xml'
    for node in ET.parse(manifest).iter():
        for key, value in node.attrib.items():
            if key.endswith('screenOrientation') or (key.endswith('resizeableActivity') and value == 'false'):
                errors.append(f'{manifest}: orientation/resizing restriction')
            if key.endswith('configChanges') and {'orientation', 'screenSize', 'smallestScreenSize'}.intersection(value.split('|')):
                errors.append(f'{manifest}: configuration changes hide recreation')
    return errors

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--purity-only', action='store_true')
    args = parser.parse_args()
    result = check(Path(__file__).resolve().parents[1], args.purity_only)
    if result:
        print('\n'.join(result), file=sys.stderr)
        sys.exit(1)
    print('Project checks passed')

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

# Every language AppLocale ships, by resource-directory suffix. A language listed in the enum with
# no resource directory does not fail the build on its own -- Compose Resources silently falls back
# to values/ -- so the pairing is asserted here instead. `iw` is the generated legacy mirror of `he`
# (see common/core/designsystem/build.gradle.kts); it is required too, because without it Hebrew silently
# renders English on Android <= 33.
REQUIRED_LOCALES = ('he', 'iw', 'ru', 'fr')

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

def layer_imports(root):
    """Protect co-located domain/data/ui layers without forcing separate modules."""
    errors = []
    for path in files(root):
        if path.suffix != '.kt' or 'commonMain' not in path.parts:
            continue
        source = path.read_text()
        package = re.search(r'^\s*package\s+([\w.]+)', source, re.MULTILINE)
        if not package:
            continue
        owner = re.search(r'\.feature\.([^.]+)\.(domain|data|ui)(?:\.|$)', package.group(1))
        pure = re.search(r'\.core\.(model|common)(?:\.|$)', package.group(1))
        for line, text in enumerate(source.splitlines(), 1):
            imported = re.match(r'\s*import\s+([\w.]+)', text)
            if not imported:
                continue
            target = imported.group(1)
            framework = target.startswith(('androidx.', 'org.koin.', 'io.ktor.', 'platform.'))
            target_feature = re.search(r'\.feature\.([^.]+)\.([^.]+)', target)
            target_layer = re.search(r'\.feature\.[^.]+\.(data|ui|di|navigation)(?:\.|$)', target)
            if owner:
                layer = owner.group(2)
                wrong_feature = target_feature and target_feature.group(1) != owner.group(1)
                wrong_layer = target_layer and (
                    layer == 'domain' or
                    (layer == 'data' and target_layer.group(1) in {'ui', 'di', 'navigation'}) or
                    (layer == 'ui' and target_layer.group(1) in {'data', 'di'}))
                infrastructure = re.search(r'\.core\.(designsystem|network)(?:\.|$)', target)
                if wrong_feature or wrong_layer or (layer == 'domain' and (framework or infrastructure)):
                    errors.append(f'{path.relative_to(root)}:{line}: forbidden {layer} import: {target}')
            elif pure and (framework or re.search(r'\.(feature|designsystem|network)\.', target)):
                errors.append(f'{path.relative_to(root)}:{line}: pure core imports outer layer: {target}')
    return errors

def check(root, only=False):
    errors = purity(root)
    if only:
        return errors
    base = root / 'common/core/designsystem/src/commonMain/composeResources'
    def strings(path):
        parsed = ET.parse(path)
        rows = parsed.findall('string')
        result = {e.attrib['name']: ''.join(e.itertext()) for e in rows}
        if len(result) != len(rows):
            errors.append(f'{path}: duplicate string key')
        return result
    default = strings(base / 'values/strings.xml')
    for code in REQUIRED_LOCALES:
        if not (base / f'values-{code}/strings.xml').exists():
            errors.append(f'{base}: missing values-{code}/strings.xml')
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
            if module and module[0] == 'common':
                module = module[1:]
            source = path.read_text()
            dependencies = re.findall(r'project\(\s*["\'](:[^"\'\n]+)["\']\s*\)', source)
            dependencies += [':' + item.replace('.', ':') for item in re.findall(
                r'projects\.((?:common\.)?(?:core|feature|sharedApp|androidApp)(?:\.[a-zA-Z0-9_]+)*)', source)]
            for dependency in dependencies:
                parts = dependency.strip(':').split(':')
                if parts[0] == 'common':
                    parts = parts[1:]
                if not parts:
                    continue
                if module and module[0] == 'core' and parts[0] in {'feature', 'androidApp', 'sharedApp'}:
                    errors.append(f'{path}: core cannot depend on {dependency}')
                if len(module) > 1 and module[0] == 'core' and module[1] in {'model', 'common'} and len(parts) > 1 and parts[0] == 'core' and parts[1] in {'network', 'designsystem'}:
                    errors.append(f'{path}: pure core cannot depend on {dependency}')
                if module and module[0] == 'feature':
                    if parts[0] in {'androidApp', 'sharedApp'}:
                        errors.append(f'{path}: feature cannot depend on {dependency}')
                    if parts[0] == 'feature' and len(module) > 1 and len(parts) > 1 and module[1] != parts[1]:
                        errors.append(f'{path}: cross-feature dependency {dependency}')
    errors.extend(layer_imports(root))
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

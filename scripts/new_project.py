#!/usr/bin/env python3
"""Create a renamed project from an explicit allowlist; never copy local credentials."""
import argparse
import keyword
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIRECTORIES = ('androidApp', 'core', 'feature', 'sharedApp', 'iosApp', 'build-logic', 'gradle', 'scripts', '.github', 'docs')
FILES = ('README.md', 'AGENTS.md', 'CLAUDE.md', '.gitignore', 'gradlew', 'gradlew.bat', 'gradle.properties', 'settings.gradle.kts', 'build.gradle.kts', 'local.properties.example')
SKIP = {'.git', '.gradle', '.kotlin', '.idea', 'build', '__pycache__', 'local.properties', 'DerivedData', 'MODERNIZATION_PLAN.md'}
RESERVED = {'assert', 'boolean', 'byte', 'case', 'char', 'const', 'default', 'double', 'enum', 'extends', 'final', 'finally', 'float', 'goto', 'implements', 'instanceof', 'int', 'long', 'native', 'new', 'private', 'protected', 'public', 'short', 'static', 'strictfp', 'switch', 'synchronized', 'throws', 'transient', 'void', 'volatile', 'class', 'fun', 'object', 'val', 'var', 'when', 'is', 'in', 'as', 'package', 'import', 'return', 'interface', 'null', 'true', 'false', 'this', 'super', 'if', 'else', 'for', 'while', 'do', 'try', 'catch', 'throw', 'break', 'continue', 'typealias', 'typeof'}

def create(destination, name, package):
    destination = Path(destination).expanduser().resolve()
    if destination.exists():
        raise ValueError('Destination already exists; choose a new directory')
    if destination == ROOT or ROOT in destination.parents:
        raise ValueError('Destination must be outside the template')
    if not re.fullmatch(r'[A-Z][A-Za-z0-9]*', name):
        raise ValueError('Name must be a PascalCase identifier, for example MyNotes')
    if not re.fullmatch(r'[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+', package) or any(p in RESERVED for p in package.split('.')):
        raise ValueError('Package must be a lowercase reverse-DNS identifier without Kotlin keywords')
    slug = name.lower()
    old_name = re.search(r'rootProject.name\s*=\s*"([^"]+)"', (ROOT / 'settings.gradle.kts').read_text()).group(1)
    old_package = re.search(r'namespace\s*=\s*"([^"]+)\.app"', (ROOT / 'androidApp/build.gradle.kts').read_text()).group(1)
    destination.mkdir(parents=True)
    try:
        for namepart in FILES:
            source = ROOT / namepart
            if source.exists():
                shutil.copy2(source, destination / namepart)
        for dirname in DIRECTORIES:
            source = ROOT / dirname
            if source.exists():
                shutil.copytree(source, destination / dirname, ignore=shutil.ignore_patterns(*SKIP, '*.keystore', '*.jks', '*.p12', '*.mobileprovision', '.env*', '*.xcworkspace', '*.xcodeproj'))
        # Only known template tokens are substituted; binary wrapper/icons stay intact.
        for path in sorted(destination.rglob('*'), key=lambda p: len(p.parts), reverse=True):
            if path.is_file():
                try:
                    contents = path.read_text()
                except UnicodeDecodeError:
                    contents = None
                if contents is not None and path.relative_to(destination).as_posix() not in {'scripts/new_project.py', 'scripts/tests/test_template_tools.py'}:
                    for old, new in [(old_package, package), (old_name, name), (old_name.lower(), slug)]:
                        contents = contents.replace(old, new)
                    path.write_text(contents)
                renamed = path.name.replace(old_name, name)
                if renamed != path.name:
                    path.rename(path.with_name(renamed))
        # Relocate all Kotlin package trees, independent of package depth.
        for source in list(destination.glob('**/' + old_package.replace('.', '/'))):
            source_root = source.parents[len(old_package.split('.')) - 1]
            target = source_root.joinpath(*package.split('.'))
            if source == target:
                continue
            # Stage outside the old package so a destination nested inside the old
            # package (for example com.kmptemplate.notes) is also safe to rename.
            staging = source_root / '.package-rename'
            source.rename(staging)
            target.parent.mkdir(parents=True, exist_ok=True)
            staging.rename(target)
            old_parent = source.parent
            while old_parent != source_root and old_parent.exists() and not any(old_parent.iterdir()):
                old_parent.rmdir()
                old_parent = old_parent.parent
        (destination / 'androidApp/src/main/res/values/strings.xml').write_text(f'<resources>\n    <string name="app_name">{name}</string>\n</resources>\n')
        (destination / 'TASKS.md').write_text(f'# {name} startup tasks\n\n- [ ] Run scripts/verify.sh on this generated project.\n- [ ] Run device restoration/inset tests.\n- [ ] Build and run the iOS simulator host on macOS.\n- [ ] Replace the example with the first real feature, including DI and tests.\n- [ ] Configure product identity, icons, backup policy and signing before release.\n')
        (destination / 'docs/VALIDATION.md').write_text('# Generated project validation\n\nThis copy has not been validated yet. The source template validation does not\nprove this project builds. Run `./scripts/verify.sh`, `:androidApp:assembleRelease`,\ndevice tests and the macOS checks; record exact results here.\n')
        readme = destination / 'README.md'
        readme.write_text(readme.read_text().replace(', and the workspace [project plans](../MODERNIZATION.md)', ''))
        (destination / 'gradlew').chmod(0o755)
    except Exception:
        shutil.rmtree(destination)
        raise
    return destination

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--name', required=True)
    parser.add_argument('--package', required=True)
    parser.add_argument('--destination', required=True)
    args = parser.parse_args()
    try:
        result = create(args.destination, args.name, args.package)
    except ValueError as error:
        parser.error(str(error))
    print(f'Created {result}\nSet ANDROID_HOME or local.properties, then run ./scripts/verify.sh')

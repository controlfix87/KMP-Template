import importlib.util
import tempfile
import unittest
from pathlib import Path

def load(name):
    spec = importlib.util.spec_from_file_location(name, Path(__file__).resolve().parents[1] / f'{name}.py')
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module

class TemplateToolsTest(unittest.TestCase):
    def test_nested_platform_leak_fails(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            path = root / 'common/feature/foo/domain/src/commonMain/kotlin/Bad.kt'
            path.parent.mkdir(parents=True)
            path.write_text('import java.time.Instant\n')
            self.assertEqual(1, len(load('check_project').purity(root)))
            path.write_text('import kotlinx.datetime.LocalDate\n')
            self.assertEqual([], load('check_project').purity(root))

    def test_nested_android_only_androidx_leak_fails(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            path = root / 'common/feature/foo/data/src/commonMain/kotlin/Bad.kt'
            path.parent.mkdir(parents=True)
            path.write_text('import androidx.work.WorkManager\n')
            self.assertEqual(1, len(load('check_project').purity(root)))
            path.write_text('import androidx.lifecycle.ViewModel\nimport androidx.navigation3.runtime.NavKey\nimport androidx.savedstate.SavedState\n')
            self.assertEqual([], load('check_project').purity(root))

    def test_nested_direct_dispatchers_import_fails_outside_dispatcher_provider(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            bad = root / 'common/core/foo/src/commonMain/kotlin/Bad.kt'
            bad.parent.mkdir(parents=True)
            bad.write_text('import kotlinx.coroutines.Dispatchers\n')
            self.assertEqual(1, len(load('check_project').purity(root)))

            allowed = root / 'common/core/common/src/commonMain/kotlin/DispatcherProvider.kt'
            allowed.parent.mkdir(parents=True, exist_ok=True)
            allowed.write_text('import kotlinx.coroutines.Dispatchers\n')
            self.assertEqual(1, len(load('check_project').purity(root)))

            test_file = root / 'common/feature/foo/src/commonTest/kotlin/FooTest.kt'
            test_file.parent.mkdir(parents=True)
            test_file.write_text('import kotlinx.coroutines.Dispatchers\n')
            bad.unlink()
            self.assertEqual([], load('check_project').purity(root))

    def test_generator_renames_and_excludes_machine_files(self):
        with tempfile.TemporaryDirectory() as temp:
            module = load('new_project')
            result = module.create(Path(temp) / 'app', 'TestNotes', 'org.example.notes')
            self.assertTrue((result / 'androidApp/src/main/kotlin/org/example/notes/app/TestNotesApplication.kt').exists())
            self.assertFalse((result / 'local.properties').exists())
            self.assertFalse((result / '.git').exists())
            self.assertFalse((result / 'androidApp/build').exists())
            self.assertIn('org.example.notes.app', (result / 'androidApp/build.gradle.kts').read_text())
            self.assertEqual([], load('check_project').check(result))
            with self.assertRaises(ValueError):
                module.create(result, 'TestNotes', 'org.example.notes')

    def test_generator_rejects_unsafe_names(self):
        module = load('new_project')
        for name, package in [('bad/name', 'com.example'), ('Notes', 'com.class'), ('Notes', 'not-a-package'), ('Notes', 'com.int')]:
            with tempfile.TemporaryDirectory() as temp, self.assertRaises(ValueError):
                module.create(Path(temp) / 'app', name, package)

    def test_missing_required_locale_fails(self):
        # A language listed in AppLocale with no values-<code>/strings.xml does not fail the build
        # on its own -- Compose Resources silently falls back to values/ -- so a deleted directory
        # ships as English text under, say, an RTL layout. It has to be caught here.
        module = load('check_project')
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            base = root / 'common/core/designsystem/src/commonMain/composeResources'
            body = '<resources><string name="a">x</string></resources>'
            for folder in ('values', *(f'values-{code}' for code in module.REQUIRED_LOCALES)):
                (base / folder).mkdir(parents=True)
                (base / folder / 'strings.xml').write_text(body)
            (root / 'androidApp/src/main').mkdir(parents=True)
            (root / 'androidApp/src/main/AndroidManifest.xml').write_text('<manifest><application /></manifest>')

            self.assertEqual([], module.check(root))

            (base / f'values-{module.REQUIRED_LOCALES[0]}/strings.xml').unlink()
            self.assertEqual(1, len(module.check(root)))

    def test_generator_supports_package_nested_under_template_package(self):
        module = load('new_project')
        with tempfile.TemporaryDirectory() as temp:
            result = module.create(Path(temp) / 'app', 'NestedNotes', 'com.kmptemplate.notes')
            self.assertTrue((result / 'androidApp/src/main/kotlin/com/kmptemplate/notes/app/NestedNotesApplication.kt').exists())

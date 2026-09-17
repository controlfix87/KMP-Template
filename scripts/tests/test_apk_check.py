import importlib.util
import struct
import tempfile
import unittest
import zipfile
from pathlib import Path

spec = importlib.util.spec_from_file_location('check_apk', Path(__file__).resolve().parents[1] / 'check_apk.py')
checker = importlib.util.module_from_spec(spec)
spec.loader.exec_module(checker)

class ApkCheckTest(unittest.TestCase):
    def fixture(self, path, alignment):
        data = bytearray(120)
        data[:6] = b'\x7fELF\x02\x01'
        struct.pack_into('<Q', data, 32, 64)
        struct.pack_into('<HH', data, 54, 56, 1)
        struct.pack_into('<IIQQQQQQ', data, 64, 1, 5, 0, 0, 0, 120, 120, alignment)
        with zipfile.ZipFile(path, 'w', compression=zipfile.ZIP_DEFLATED) as archive:
            archive.writestr('lib/arm64-v8a/libfixture.so', data)

    def test_rejects_4k_load_segment(self):
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / 'bad.apk'
            self.fixture(path, 4096)
            with self.assertRaisesRegex(ValueError, 'LOAD segment'):
                checker.check(path)

    def test_accepts_16k_load_segment_in_compressed_library(self):
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / 'good.apk'
            self.fixture(path, 16384)
            self.assertEqual(1, checker.check(path))

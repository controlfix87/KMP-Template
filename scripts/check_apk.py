#!/usr/bin/env python3
"""Check 64-bit ELF load segments and uncompressed ZIP alignment for 16 KB devices."""
import argparse
import struct
import zipfile
from pathlib import Path

PAGE = 16 * 1024

def check(apk):
    errors = []
    checked = 0
    with zipfile.ZipFile(apk) as archive, open(apk, 'rb') as raw:
        for entry in archive.infolist():
            if not entry.filename.endswith('.so') or not entry.filename.startswith(('lib/arm64-v8a/', 'lib/x86_64/')):
                continue
            checked += 1
            data = archive.read(entry)
            if data[:6] != b'\x7fELF\x02\x01':
                errors.append(f'{entry.filename}: unsupported ELF format')
                continue
            offset = struct.unpack_from('<Q', data, 32)[0]
            size, count = struct.unpack_from('<HH', data, 54)
            for index in range(count):
                kind, flags, file_offset, address, physical, file_size, memory_size, alignment = struct.unpack_from('<IIQQQQQQ', data, offset + size * index)
                if kind == 1 and (alignment < PAGE or file_offset % PAGE != address % PAGE):
                    errors.append(f'{entry.filename}: LOAD segment {index} is not 16 KB compatible')
            if entry.compress_type == zipfile.ZIP_STORED:
                raw.seek(entry.header_offset + 26)
                name_size, extra_size = struct.unpack('<HH', raw.read(4))
                data_offset = entry.header_offset + 30 + name_size + extra_size
                if data_offset % PAGE:
                    errors.append(f'{entry.filename}: uncompressed ZIP data not 16 KB aligned')
    if errors:
        raise ValueError('\n'.join(errors))
    return checked

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('apk', type=Path)
    args = parser.parse_args()
    try:
        count = check(args.apk)
    except (ValueError, zipfile.BadZipFile, OSError, struct.error) as error:
        parser.exit(1, f'{error}\n')
    print(f'16 KB packaging checks passed: {count} native 64-bit libraries in {args.apk}')

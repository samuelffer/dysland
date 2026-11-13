#!/usr/bin/env python3
"""Create an immutable, installable Android debug package; never bundles signing keys."""
from pathlib import Path
import hashlib
import zipfile
import sys
import tempfile

root=Path(__file__).resolve().parent.parent
apk=root/'app/build/outputs/apk/debug/app-debug.apk'
version='0.1.0'
destination=root/'Zips'/f'dysland-{version}-android-debug.zip'
if not apk.is_file(): sys.exit('Expected debug APK is missing')
if destination.exists(): sys.exit('Refusing to overwrite a published version; bump app and package version first')
with zipfile.ZipFile(apk) as z:
    if 'AndroidManifest.xml' not in z.namelist() or 'classes.dex' not in z.namelist(): sys.exit('Invalid APK')
destination.parent.mkdir(exist_ok=True)
digest=hashlib.sha256(apk.read_bytes()).hexdigest()
with zipfile.ZipFile(destination,'x',zipfile.ZIP_DEFLATED) as z:
    z.write(apk,'dysland-0.1.0-debug.apk')
    z.write(root/'docs/INSTALL.md','INSTALAR.md')
    z.write(root/'LICENSE','LICENSE')
    z.write(root/'licenses/Manrope-OFL.txt','Manrope-OFL.txt')
    z.writestr('SHA256SUMS',f'{digest}  dysland-0.1.0-debug.apk\n')
with zipfile.ZipFile(destination) as z:
    assert z.testzip() is None
    assert len(z.namelist())==5
    with tempfile.TemporaryDirectory() as temp:
        z.extractall(temp)
        assert hashlib.sha256((Path(temp)/'dysland-0.1.0-debug.apk').read_bytes()).hexdigest()==digest
print(destination)

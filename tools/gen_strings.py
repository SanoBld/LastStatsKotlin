#!/usr/bin/env python3
"""Builds the shared string tables (JSON) from the Android strings_l10n.xml files.

Source of truth: app/src/main/res/values*/strings_l10n.xml
Output:          shared/src/commonMain/composeResources/files/strings/<lang>.json
Run it from the project root after changing a translation: python3 tools/gen_strings.py
"""
import json
import os
import re
import sys
import xml.etree.ElementTree as ET

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")
OUT = os.path.join(ROOT, "shared", "src", "commonMain", "composeResources", "files", "strings")

ESCAPES = {"n": "\n", "t": "\t", "'": "'", '"': '"', "@": "@", "?": "?", "\\": "\\"}


def unescape(text):
    if text is None:
        return ""
    s = text
    if len(s) >= 2 and s.startswith('"') and s.endswith('"'):
        s = s[1:-1]
    out = []
    i = 0
    while i < len(s):
        c = s[i]
        if c == "\\" and i + 1 < len(s):
            n = s[i + 1]
            if n == "u" and i + 5 < len(s) + 0 and re.fullmatch(r"[0-9a-fA-F]{4}", s[i + 2:i + 6] or ""):
                out.append(chr(int(s[i + 2:i + 6], 16)))
                i += 6
                continue
            out.append(ESCAPES.get(n, n))
            i += 2
            continue
        out.append(c)
        i += 1
    return "".join(out)


def read(path):
    root = ET.parse(path).getroot()
    strings, arrays = {}, {}
    for e in root:
        name = e.get("name")
        if e.tag == "string":
            strings[name] = unescape("".join(e.itertext()))
        elif e.tag == "string-array":
            arrays[name] = [unescape("".join(i.itertext())) for i in e.findall("item")]
    return strings, arrays


def main():
    os.makedirs(OUT, exist_ok=True)
    count = 0
    for d in sorted(os.listdir(RES)):
        if not d.startswith("values"):
            continue
        f = os.path.join(RES, d, "strings_l10n.xml")
        if not os.path.exists(f):
            continue
        m = re.fullmatch(r"values(?:-([a-z]{2}))?", d)
        if not m:
            continue
        code = m.group(1) or "en"
        strings, arrays = read(f)
        with open(os.path.join(OUT, code + ".json"), "w", encoding="utf-8", newline="\n") as fh:
            json.dump({"s": strings, "a": arrays}, fh, ensure_ascii=False, separators=(",", ":"), sort_keys=True)
        print(code, len(strings), "strings", len(arrays), "arrays")
        count += 1
    if count == 0:
        sys.exit("no strings_l10n.xml found")


if __name__ == "__main__":
    main()

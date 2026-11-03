#!/usr/bin/env python3
"""Generates textures, block/item models, blockstates and lang files for the bench.
Run from project root: python3 tools/gen_assets.py (outputs already committed)."""
import json, os, random, struct, zlib

MOD = "lowensburg_railway_decor"
A = f"src/main/resources/assets/{MOD}"

def png(path, rows):
    raw = b"".join(b"\x00" + bytes(c for px in row for c in px) for row in rows)
    def chunk(t, d):
        c = struct.pack(">I", len(d)) + t + d
        return c + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)
    h, w = len(rows), len(rows[0])
    data = (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, "wb").write(data)

rnd = random.Random(1234)
def clamp(v): return max(0, min(255, v))

# brushed grey metal
metal = []
for y in range(16):
    row = []
    for x in range(16):
        n = rnd.randint(-5, 5)
        base = 150 + (8 if y == 0 else 0) - (8 if y == 15 else 0)
        row.append((clamp(base + n), clamp(base + 4 + n), clamp(base + 9 + n), 255))
    metal.append(row)
png(f"{A}/textures/block/bench_metal.png", metal)

# perforated seat / back
seat = []
for y in range(16):
    row = []
    for x in range(16):
        n = rnd.randint(-3, 3)
        hole = (x % 4 in (1, 2)) and (y % 4 in (1, 2))
        if hole: row.append((64, 67, 72, 255))
        else:    row.append((clamp(166 + n), clamp(170 + n), clamp(176 + n), 255))
    seat.append(row)
png(f"{A}/textures/block/bench_seat.png", seat)

# ---- geometry (pixels, bench faces NORTH, back at +z); keep in sync with BenchBlock.java
COMMON = [([0,7,2,16,8.5,14],"seat"), ([0,8.5,12.5,16,15.5,14],"seat"),
          ([0,5.5,3,16,7,4.5],"metal"), ([0,5.5,11.5,16,7,13],"metal")]
LEFT_END = [([0.5,0,3,2,5.5,4.5],"metal"), ([0.5,0,11.5,2,5.5,13],"metal"),
            ([0,8.5,3,1.5,11,4.5],"metal"), ([0,11,3,1.5,12,13],"metal")]
def mirror(boxes):
    return [([16-b[3], b[1], b[2], 16-b[0], b[4], b[5]], t) for b, t in boxes]
PARTS = {"left": COMMON + LEFT_END, "middle": COMMON, "right": COMMON + mirror(LEFT_END)}

def elem(b, tex, dx=0.0, s=1.0, oy=0.0, oz=0.0):
    f = lambda v: round(v, 4)
    return {"from": [f((b[0]+dx)*s), f(b[1]*s+oy), f(b[2]*s+oz)],
            "to":   [f((b[3]+dx)*s), f(b[4]*s+oy), f(b[5]*s+oz)],
            "faces": {d: {"texture": "#" + tex} for d in ("north","east","south","west","up","down")}}

TEX = {"metal": f"{MOD}:block/bench_metal", "seat": f"{MOD}:block/bench_seat",
       "particle": f"{MOD}:block/bench_metal"}

def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(obj, open(path, "w", encoding="utf-8"), indent=2, ensure_ascii=False)

for name, boxes in PARTS.items():
    write(f"{A}/models/block/bench_{name}.json",
          {"textures": TEX, "elements": [elem(b, t) for b, t in boxes]})

# blockstate
variants = {}
for i, face in enumerate(["north", "east", "south", "west"]):
    for part in PARTS:
        v = {"model": f"{MOD}:block/bench_{part}"}
        if i: v["y"] = 90 * i
        variants[f"facing={face},part={part}"] = v
write(f"{A}/blockstates/metal_bench.json", {"variants": variants})

# item models: whole row compressed uniformly into one block
GUI = {2: 1.15, 3: 1.3, 4: 1.4}
for n in (2, 3, 4):
    s = 1.0 / n
    elements = []
    for i in range(n):
        part = "left" if i == 0 else "right" if i == n-1 else "middle"
        for b, t in PARTS[part]:
            elements.append(elem(b, t, dx=16*i, s=s, oy=0.0, oz=8 - 8*s))
    g = GUI[n]
    write(f"{A}/models/item/bench_{n}.json", {
        "textures": TEX, "elements": elements,
        "display": {
            "gui": {"rotation": [30, 225, 0], "translation": [0, -1, 0], "scale": [g, g, g]},
            "ground": {"translation": [0, 3, 0], "scale": [0.6, 0.6, 0.6]},
            "fixed": {"rotation": [0, 180, 0], "scale": [1, 1, 1]},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.6, 0.6, 0.6]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "scale": [0.6, 0.6, 0.6]}}})

# language files
def names(bench, tab, blk):
    d = {f"block.{MOD}.metal_bench": blk, f"itemGroup.{MOD}.main": tab}
    for n in (2, 3, 4): d[f"item.{MOD}.bench_{n}"] = bench[n]
    return d
write(f"{A}/lang/en_us.json", names({2:"Metal Bench (2 seats)",3:"Metal Bench (3 seats)",4:"Metal Bench (4 seats)"},
      "Lowensburg Railway Decor", "Metal Bench"))
write(f"{A}/lang/de_de.json", names({2:"Metallbank (2 Sitze)",3:"Metallbank (3 Sitze)",4:"Metallbank (4 Sitze)"},
      "Lowensburg Railway Decor", "Metallbank"))
write(f"{A}/lang/ru_ru.json", names({2:"Металлическая скамья (2 места)",3:"Металлическая скамья (3 места)",4:"Металлическая скамья (4 места)"},
      "Lowensburg Railway Decor", "Металлическая скамья"))
print("assets generated")

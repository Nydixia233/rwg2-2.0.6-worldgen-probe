# RWG2 2.0.6 worldgen probe bundle

This bundle reproduces the algorithm-level observations used in the issue drafts.

Public repository: [https://github.com/Nydixia233/rwg2-2.0.6-worldgen-probe](https://github.com/Nydixia233/rwg2-2.0.6-worldgen-probe)

## Contents

- `IssueReviewProbe.java` — review probe entry point (6 assertions: lava vent, surface cavity, Y62 floor).
- `probe-src/` — upstream sources, test stubs, and the original 11-assertion probe (`rwg2/deco/trees/Probe.java`).
- `probe-results.txt` — original 11-assertion probe output.
- `issue-review-probe-results.txt` — review probe output.
- `LICENSE.txt` — upstream RWG2 license (GNU GPL v3), copied from the 2.0.6 tag.

## License / provenance

The eight upstream source files listed below are copied from RWG2 **2.0.6**, commit `497fc1f49dcab6032fb828a818931e2c391ff10e`, without logic changes. The upstream repository includes the **GNU GPL v3** license; its original `LICENSE.txt` is included in this bundle. Test stubs and probe entry points are identified separately.

Upstream files (unmodified copies):

- `rwg2/deco/trees/DecoBigTree.java`
- `rwg2/deco/trees/DecoJungleFat.java`
- `rwg2/map/LavaCaveLandmark.java`
- `rwg2/surface/SurfaceBase.java`
- `rwg2/surface/SurfaceGrassland.java`
- `rwg2/surface/SurfaceMountainStoneMix1.java`
- `rwg2/util/CliffCalculator.java`
- `rwg2/util/NoiseGenerator.java`

Test-only files (not upstream generators):

| File | Role |
|---|---|
| `rwg2/util/CellNoise.java` | test stub; fixed edge-distance samples, not a real Voronoi implementation |
| `rwg2/util/ContinentalNoise.java` | minimal coordinate-unpacking stub, not a full continent generator |
| `rwg2/world/ChunkManagerRealistic.java` | test stub; queries return `Long.MIN_VALUE`; probes call `carveColumn` directly |
| `rwg2/deco/trees/Probe.java` | entry point for the original 11 assertions, not an upstream generator |

Minimal Minecraft 1.7.10 API stubs (also test-only): `Block`, `Blocks`, `MathHelper`, `World`, `BiomeGenBase`, `WorldGenerator`.

## Requirements

- JDK 25.0.2 was used here; a JDK 17+ with `javac`/`java` should also work.
- Windows PowerShell commands below; adjust paths on other systems.

## Run

Open PowerShell in the extracted bundle root:

```powershell
$root = (Get-Location).Path
$classes = Join-Path $root 'classes'
New-Item -ItemType Directory -Force -Path $classes | Out-Null

$sources = Get-ChildItem -LiteralPath (Join-Path $root 'probe-src') -Recurse -Filter *.java |
    Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -d $classes @sources (Join-Path $root 'IssueReviewProbe.java')
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }

java -cp $classes rwg2.deco.trees.Probe
if ($LASTEXITCODE -ne 0) { throw 'Original probes failed' }
java -cp $classes IssueReviewProbe
if ($LASTEXITCODE -ne 0) { throw 'Review probes failed' }
```

Expected: **11 + 6 = 17 PASS** (JVM warnings aside).

The original `Math.random()` probe only asserts that more than one height is observed; the sampled set `[7..13]` is illustrative, not a strict acceptance requirement.

Expected `IssueReviewProbe` output:

```text
PASS sandStart=65: new unsupported sand at Y67 after funnel
PASS original six-layer fixture: support branch never entered (top+1 was stone)
Y62..70=[air, air, air, air, air, sand, air, air, air]
PASS sandStart=63: new unsupported sand at Y67 after funnel
PASS eight-layer fixture: support at top=62 remains intact after funnel
Y62..70=[stone, air, air, air, air, sand, air, air, air]
PASS same enclosed cavity: floor Y70 is grass, ceiling underside Y81 remains stone
PASS a solid floor at Y62 takes the top-block branch; actual river solid bed is <=61
```

## What each probe shows

- `carveColumn`, original fixture: stone up to Y70, sand Y65-70, zero noise, distance 8.1, `top=62`; `top+1` is stone, so the `BlockFalling` branch is never entered; after the vent pass, Y67 is sand above Y66 air.
- Control fixture: sand Y63-70; the support branch places stone at Y62; that stone survives the vent pass; the higher unsupported sand ledge still appears.
- `SurfaceGrassland`: a Y71-80 cavity yields grass at Y70 while the ceiling underside at Y81 stays stone.
- A solid floor at Y62 satisfies `k > 61` and becomes grass; an actual river carve leaves its solid bed at `caveFloor - 1 <= 61`, which gets filler in the tested non-cliff configuration.
- The original 11-assertion probe covers the tree, cliff, and general cavity fixtures.

## Scope

These are method-level geometry probes. They do not run the Forge chunk provider, do not estimate in-game frequency, and do not establish 3D connectivity or a truncated-tree chain.

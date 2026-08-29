# Phase 0 spike — findings

**Branch:** `spike/cmp-phase0` (throwaway, not merged to `main`)
**Date:** 2026-08-29

## What was tested

Added the Kotlin Multiplatform Gradle plugin to this project's existing
toolchain (Gradle 9.6.1, AGP 9.2.1, Kotlin 2.2.0) via a new throwaway
`:kmpSpike` module (`jvm()`, `iosX64()`, `iosArm64()`, `iosSimulatorArm64()`,
one trivial `commonMain` file). No existing module was touched.

## Result

- `:kmpSpike:compileKotlinJvm` — **BUILD SUCCESSFUL**, 33s. The KMP plugin
  resolves and integrates cleanly with this project's existing Gradle/AGP/
  Kotlin versions. No version-conflict, no plugin-resolution failure.
- The three iOS targets (`iosX64`, `iosArm64`, `iosSimulatorArm64`) were
  **disabled automatically by the Kotlin Gradle plugin** on this machine,
  with the explicit warning:

  > Disabled Kotlin/Native Targets
  > The following Kotlin/Native targets cannot be built on this machine and
  > are disabled: iosArm64, iosSimulatorArm64, iosX64

## What this means

This is a **host OS gate, not a project-compatibility problem.** The Kotlin
Multiplatform Gradle plugin refuses to build *any* iOS Native target — not
just link/run, but klib compilation too — unless the build runs on macOS
with Xcode installed. There is no cross-compilation path from Windows for
iOS targets, full stop.

Practical consequence for this plan: every phase from Phase 1 onward can be
authored and verified for its Android-side effects on Windows, but the iOS
side of any phase (and all of Phase 0's original "run in iOS simulator"
step) needs a Mac. Options, unchanged from the plan's own §5/§7:

1. Get access to a Mac (owned or cloud, e.g. GitHub Actions `macos-*`
   runner) before starting Phase 1, or
2. Do Phases 1-4's Android-only, common-code work now, and defer any iOS
   target build/verification until Mac access exists.

## Cleanup

This module and this branch are throwaway per the plan's Phase 0 design.
Delete `:kmpSpike/` and drop the branch once the finding above is recorded;
do not merge to `main`.

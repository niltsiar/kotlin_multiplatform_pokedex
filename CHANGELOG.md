# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### ♻️ Refactoring
#### di

- Migrate from Metro to Koin dependency injection ([15c546d](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/15c546d197ecec9357901cd59152fb08b066c58f))

- Improve Koin module organization and wiring patterns ([e792502](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/e79250228cf8cccde0cc1984082cc76ed5b83a8b))

#### ios

- Remove ViewModel wrapper pattern in favor of direct integration ([3eaa9d0](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/3eaa9d0efab0f71107a34583609a2dadd7682c07))



### ✅ Tests
#### pokemonlist

- Add initial unit tests for repository and ViewModel ([a6c3086](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/a6c3086175204122dac90e527ca09d32722a8f66))

- Enhance test coverage with property-based tests ([e653ce8](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/e653ce8f349a81445d985871440856e347671b06))

- Enhance test coverage with property-based tests ([dc7eb87](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/dc7eb87edbd235a6152353c075aa9f7aa991299c))



### ✨ Features
#### designsystem

- Implement Material 3 Expressive theme with Navigation 3 ([8e4dec3](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/8e4dec32f249256b74591c6928989f61e698dd64))

#### ios

- Implement native SwiftUI iOS app with KMP integration ([37cd7a6](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/37cd7a6c165878651e7a2f66b18c0d88ce5addc7))

- Add Compose Multiplatform iOS app (experimental) ([8f4dde6](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/8f4dde662b3d0bb3aa6ddaad16ec048d77e293da))

#### navigation

- Implement Navigation 3 modular architecture with dynamic entry providers ([b6ed773](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/b6ed77354121e07cc725523baf908083e2079243))

#### pokemondetail

- Implement Pokemon detail feature with parametric ViewModel ([70a53d4](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/70a53d4ee7d21b38d54954d8bc007dc6ae6fd038))

#### pokemonlist

- Implement initial feature with repository and ViewModel ([f1fe767](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/f1fe767a20b81aaa26c587cadd10bb4b9870c03e))

- Implement UI screens from product requirements ([b5dae5c](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/b5dae5c8112353abd4326803ef78a6dd5da1a696))



### 🐛 Bug Fixes
#### ios

- Resolve Compose iOS build issues and update integration docs ([15800cb](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/15800cb02ce869a41a7cb566f3f50f849de9f79a))

- Rename Type class to avoid Swift reserved word conflict ([3957efb](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/3957efbf84337853208a04483dd62701b5349fec))

#### server

- Resolve Ktor server configuration and improve test setup ([7df7a3a](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/7df7a3a09f68f30fd50a1118b9b37be094592dbc))



### 📝 Documentation
#### agents

- Create autonomous AI agent workflow guide ([cb1c1b5](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/cb1c1b517383fd17ba85cbe7d2f0e398dd2fdd37))

- Enhance AI agent guide with specialized prompt references ([4319344](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/4319344752fcc5bf5bba1cb97e52fb813736dbdd))

- Add Backend, Testing Strategy, and Documentation Management modes ([e653be6](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/e653be619801e4a994050dcac3a234219596f346))

#### conventions

- Update project conventions and guidelines ([604d73c](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/604d73c145ec9b3be31e27b87790d79b2f543b72))

- Update project conventions and guidelines ([18e5652](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/18e5652f2095d7db5bc7348eadc3c6da700626fd))

- Update project conventions and guidelines ([ce10b33](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/ce10b33e3e8f26ea2d5c32c80237d13ed6134b03))

- Update project conventions and guidelines ([5ad7cda](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/5ad7cda7d3bb46220b87597c47969246dacb722b))

- Update project conventions and guidelines ([9f7f2b6](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/9f7f2b69755657bf9261b90c89ed92292e6eb541))

- Update project conventions and guidelines ([69fc07b](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/69fc07b293f81799f6ebb68f241d535964880154))

- Update project conventions and guidelines ([7d7413d](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/7d7413d928fac1330ecb56c89ac10a297294f23c))

- Update project conventions and guidelines ([7712099](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/77120990034161d95d724d5135344a86fa7edcfa))

- Refine project guidelines and patterns ([c1ad5f2](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/c1ad5f256d9987da13bbf0cf72f289e4ee488fc3))

- Update project conventions and guidelines ([c3d2899](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/c3d28997662503badcd0f7f75cdea71f88a466cc))

- Refine project guidelines and patterns ([9d1fed4](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/9d1fed49cc0aba320cc57bb19fa4e69193b72140))

- Refine project guidelines and patterns ([1c252fd](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/1c252fdb927ddb8abef38029d27e351cac20ecef))

- Refine project guidelines and patterns ([e37d06c](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/e37d06c5b75d34805cdf6a054ebc26f061efaad2))

- Refine project guidelines and patterns ([498bcc5](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/498bcc57edd1329105f51dbd0e0f8da400ad9dbf))

#### di

- Update dependency injection documentation ([712df4e](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/712df4ef51ed46fae5c8a9141fd313a3ba54b858))



### 🔧 Build System
#### gradle

- Apply convention plugins for consistent module configuration ([a2eae50](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/a2eae50394c9de316242bac39f8777639112a36b))

- Configure module dependencies and build settings ([c11902f](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/c11902f44f61778bbf5e093ef95499780b0c6669))

- Refactor convention plugins for better code reuse ([c053bea](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/c053bea0eab6ed4d69a018121fdfabd256c82e27))

- Configure dependency updates plugin and version management ([8375042](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/837504228efef33f380f3fe1b0d6ab90a761e4ed))

- Update Kotlin and dependency versions ([16cd2b7](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/16cd2b79310586da730d6b379eaa7e1ca6729692))

- Enhance convention plugins with shared configuration utilities ([1a24544](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/1a245440e87f0bbe019ffce5a640ea9c14c62ebf))

- Refactor convention plugins for better code reuse ([5d2a634](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/5d2a634af58cf66a98341afd13ee8fbe2dcbf830))

#### ios

- Configure shared module as umbrella framework for iOS export ([1266a99](https://github.com/niltsiar/kotlin_multiplatform_pokedex/commit/1266a99538976d9bce486a052372457692e30885))



### 🧹 Chores


<!-- generated by git-cliff -->

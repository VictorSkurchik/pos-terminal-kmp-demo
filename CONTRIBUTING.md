# Contributing

## Commit messages — Conventional Commits

This repo follows [Conventional Commits](https://www.conventionalcommits.org/). Every commit
subject must be:

```
<type>(<optional scope>): <subject>
```

**Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`.

Examples:

```
feat(pos): add split-payment flow
fix(mdm): re-throw CancellationException in the poll loop
refactor(core-data): map DTOs to domain models in the repository
build: introduce build-logic convention plugins
docs: document the Clean Architecture layering
```

Breaking changes: add `!` after the type/scope, e.g. `feat(api)!: rename device id field`.

### Enforcement

A `commit-msg` git hook rejects non-conforming messages. Install it once after cloning:

```bash
./gradlew installGitHooks      # runs: git config core.hooksPath .githooks
```

CI also validates PR commit subjects against `commitlint.config.js`.

## Code style & static analysis

- **Formatting:** ktlint (`./gradlew ktlintFormat` to auto-fix, `./gradlew ktlintCheck` to verify).
- **Static analysis:** detekt (`./gradlew detekt`), config in `detekt.yml`.
- **Android Lint:** `./gradlew lint`.

All three run in CI on every PR. Please run `ktlintFormat` before pushing.

## Build variants

The app ships three environments (see `README.md` → *Build variants*):

```bash
./gradlew :app:androidApp:assembleDevDebug        # local backend (10.0.2.2:8080)
./gradlew :app:androidApp:assembleStagingDebug     # staging backend
./gradlew :app:androidApp:assembleProdRelease      # production (Render), minified + signed
```

## Tests

```bash
./gradlew test              # all module unit tests
./gradlew :core:jvmTest     # shared KMP tests
```

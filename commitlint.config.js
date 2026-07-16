// Conventional Commits config — used by CI (and optionally by a local commitlint setup).
// The repo's canonical guard is .githooks/commit-msg; this mirrors it for tooling that
// expects a commitlint config.
module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      ['feat', 'fix', 'docs', 'style', 'refactor', 'perf', 'test', 'build', 'ci', 'chore', 'revert'],
    ],
    'subject-case': [0],
    'header-max-length': [2, 'always', 100],
  },
};

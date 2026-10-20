# Steiger

Integrates the [steiger](https://github.com/feature-sliced/steiger) linter into JetBrains IDEs via the Language Server Protocol. Architecture violations reported by steiger are highlighted directly in the editor.

## Requirements

- JetBrains IDE with LSP support: WebStorm, IntelliJ IDEA, GoLand, and other.

> [!NOTE]
> This extension supports JetBrains IDEs without JavaScript support (like Intellij IDEA Community Edition), but configuration may be limited.

## Installation

**From Marketplace:** Settings → Plugins → Marketplace, search for *Steiger*.

**From source:**

```shell
./gradlew buildPlugin
```

Then install `build/distributions/*.zip` via Settings → Plugins → ⚙ → *Install Plugin from Disk…*.

## Configuration

Settings → Tools → Steiger:

| Setting | Description |
|---|---|
| Node.js interpreter | Project default (Settings → Languages & Frameworks → Node.js) or an explicit interpreter |
| Language server | The one bundled with the plugin, or a custom steiger language server script |

The language server starts automatically for JavaScript and TypeScript files
(`js`, `jsx`, `ts`, `tsx`, `mjs`, `cjs`, `vue`, `svelte`, `astro`).

## Development

```shell
./gradlew runIde        # sandbox IDE with the plugin
./gradlew runWebStorm   # sandbox WebStorm
./gradlew test
./gradlew verifyPlugin
```

See [CHANGELOG.md](CHANGELOG.md) for release notes.

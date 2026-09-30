# Build troubleshooting

## `Cannot use connection to Gradle distribution '…/gradle-9.2.1-bin.zip' as it has been stopped`

The Gradle wrapper (or your IDE's Gradle integration) opened a connection to
`services.gradle.org` and the connection was closed **before the ~130 MB distribution finished
downloading**. This is a network problem, not a problem with this project:

* Gradle 9.2.1 exists — <https://services.gradle.org/distributions/gradle-9.2.1-bin.zip.sha256>
  returns a checksum, and the real download is served by Gradle's CDN `downloads.gradle.org`.
* Loom 1.14 requires **Gradle 9.2 or newer**, so 9.2.1 is the intended version for this project.

A partial download is thrown away and must be repeated, so the fixes below are about getting the
zip onto your disk in one piece.

### Fixes, in rough order of effort

| # | Fix | How |
| --- | --- | --- |
| 1 | Simply run it again from a terminal | `./gradlew build` (Linux/macOS) or `gradlew.bat build` (Windows). The terminal path does not use the IDE's own downloader, and a flaky connection often succeeds on the second try. |
| 2 | Give the download more time | `networkTimeout=60000` is already configured in `gradle/wrapper/gradle-wrapper.properties`. On a slow line raise it to `120000`. |
| 3 | Check proxy / firewall / antivirus | Corporate proxies and antivirus TLS inspection are the most common cause. Terminal builds honour `HTTPS_PROXY`/`HTTP_PROXY`; Gradle-specific settings go into `~/.gradle/gradle.properties`: `systemProp.https.proxyHost=…`, `systemProp.https.proxyPort=…`, `systemProp.https.proxyUser=…`, `systemProp.https.proxyPassword=…`. |
| 4 | Pre-seed the distribution (also works offline) | Download the zip once by hand and let the wrapper use it — see below. |
| 5 | Build with a local Gradle instead of the wrapper | Install Gradle 9.2+ (SDKMAN `sdk install gradle 9.2.1`, Chocolatey `choco install gradle`, Scoop `scoop install gradle`) and run `gradle build` in the project folder. The wrapper is convenient, not required. |
| 6 | Let a newer wrapper retry automatically | Gradle 8.11+ wrapper jars understand `retries` and `retryBackOffMs` in `gradle-wrapper.properties`. Regenerate the wrapper with a local Gradle 9.x: `gradle wrapper --gradle-version 9.2.1`, then add `retries=3` and `retryBackOffMs=1000`. |
| 7 | IntelliJ settings | *Settings → Build, Execution, Deployment → Build Tools → Gradle*: set **Distribution** to `gradle-wrapper.properties file` (or to a local installation) and set the proxy under *Appearance & Behavior → System Settings → HTTP Proxy*. |

### Pre-seeding the distribution in detail

**Option A – point the wrapper at the zip you downloaded**

```properties
distributionUrl=file\:///C:/Users/<you>/Downloads/gradle-9.2.1-bin.zip
```

Windows: three slashes, forward slashes, escaped colon. Linux/macOS:
`distributionUrl=file\:///home/<you>/Downloads/gradle-9.2.1-bin.zip`.

**Option B – drop the zip into the wrapper cache**

1. Start a build once and let it fail; this creates
   `~/.gradle/wrapper/dists/gradle-9.2.1-bin/<hash>/`
   (`%USERPROFILE%\.gradle\wrapper\dists\gradle-9.2.1-bin\<hash>\` on Windows).
2. Put your downloaded `gradle-9.2.1-bin.zip` into that `<hash>` folder and delete any leftover
   `*.part` file.
3. Run `./gradlew build` again — the wrapper finds the zip and unpacks it.

### Integrity check

The wrapper ships with the official checksum of the distribution, so a truncated download fails
loudly instead of producing a broken install:

```properties
distributionSha256Sum=72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f
```

Both values below are the official ones (Gradle's `versions/all` API and the `.sha256` files next
to each download):

| File | SHA-256 |
| --- | --- |
| `gradle-9.2.1-bin.zip` | `72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f` |
| `gradle-9.2.1-wrapper.jar` (the one committed here) | `423cb469ccc0ecc31f0e4e1c309976198ccb734cdcbb7029d4bda0f18f57e8d9` |

You can verify the committed wrapper yourself:

```bash
# Linux
sha256sum gradle/wrapper/gradle-wrapper.jar
# macOS
shasum -a 256 gradle/wrapper/gradle-wrapper.jar
# Windows PowerShell
Get-FileHash gradle\wrapper\gradle-wrapper.jar
```

It must print `423cb469ccc0ecc31f0e4e1c309976198ccb734cdcbb7029d4bda0f18f57e8d9`.

### Mirrors

If Gradle's CDN is unreachable or extremely slow on your network, point `distributionUrl` at any
trusted mirror of the distribution (many companies and universities mirror Gradle) and keep the
checksum line above so you notice if the mirror serves something else.

## Other common problems

| Symptom | Cause / fix |
| --- | --- |
| `Could not find net.fabricmc.fabric-loom-remap:…` | The Fabric maven must be listed first in `settings.gradle → pluginManagement.repositories` (it already is). A proxy that blocks `maven.fabricmc.net` produces the same message. |
| `Unsupported class file major version` / `Gradle 8.x … requires Gradle 9.2` | The wrapper is running an older Gradle. Delete the stale wrapper (`gradle/wrapper/gradle-wrapper.jar`, or run with a local Gradle 9.2+) — Loom 1.14 needs Gradle 9.2+. |
| `./gradlew: Permission denied` | `chmod +x gradlew` (the executable bit is stored in git, but a zip download can lose it). |
| Mixin errors in the log at startup | Expected to be impossible: every injector uses `require = 0` and the affected feature disables itself, with `[ ChaosUtils ] [ MISS ] …` lines telling you which hook did not apply (see `docs/API_NOTES.md`). |
| `NoClassDefFoundError` for Fabric API classes | Fabric API is missing or the wrong build. This mod needs `0.141.6+1.21.11` or newer for Minecraft 1.21.11. |

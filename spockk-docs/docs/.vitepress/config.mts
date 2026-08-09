import { readFileSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitepress'

// Three levels up from docs/.vitepress/ is the repository root.
const repoRoot = path.resolve(fileURLToPath(new URL('.', import.meta.url)), '../../..')

function readProperty(file: string, pattern: RegExp): string | undefined {
  return readFileSync(file, 'utf-8').match(pattern)?.[1]
}

// The Gradle build passes these in explicitly so a real release always shows
// the exact version it published. Reading the same source files directly
// keeps `pnpm dev`/`pnpm build` (run without Gradle) equally accurate,
// instead of falling back to a hardcoded value that would silently drift.
const spockkVersion =
  process.env.SPOCKK_VERSION ??
  readProperty(path.join(repoRoot, 'gradle.properties'), /^version\s*=\s*(.+)$/m)
const junitPlatformVersion =
  process.env.JUNIT_PLATFORM_VERSION ??
  readProperty(path.join(repoRoot, 'gradle/libs.versions.toml'), /^junit\s*=\s*"([^"]+)"/m)

export default defineConfig({
  title: 'Spockk',
  description: "Spock's expressive specification syntax, natively in Kotlin.",
  lang: 'en-US',
  outDir: '../build/docs',
  cleanUrls: true,
  lastUpdated: false,

  head: [
    ['link', { rel: 'icon', type: 'image/svg+xml', href: '/images/icon_with_background.svg' }],
    ['meta', { name: 'theme-color', content: '#844dfd' }],
  ],

  markdown: {
    config: (md) => {
      md.core.ruler.before('normalize', 'spockk-vars', (state) => {
        state.src = state.src
          .replaceAll('{revnumber}', spockkVersion)
          .replaceAll('{junitPlatformVersion}', junitPlatformVersion)
      })
    },
  },

  themeConfig: {
    logo: {
      light: '/images/name_with_icon_light.svg',
      dark: '/images/name_with_icon_dark.svg',
    },
    siteTitle: false,

    nav: [
      { text: 'Getting Started', link: '/getting-started' },
      { text: 'Examples', link: '/examples' },
      { text: 'Limitations', link: '/limitations' },
      { text: 'Changelog', link: '/changelog' },
    ],

    socialLinks: [{ icon: 'github', link: 'https://github.com/pshevche/spockk' }],

    search: {
      provider: 'local',
    },

    footer: {
      message: 'Released under the Apache License 2.0.',
    },
  },
})

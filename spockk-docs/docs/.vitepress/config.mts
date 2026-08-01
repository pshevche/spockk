import { defineConfig } from 'vitepress'

// Passed in by the :spockk-docs Gradle build so code snippets always show
// the current release. Fall back to the last known-good values for `pnpm dev`.
const spockkVersion = process.env.SPOCKK_VERSION ?? '0.4.1'
const junitPlatformVersion = process.env.JUNIT_PLATFORM_VERSION ?? '6.1.2'

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
      { text: 'Sneak Preview', link: '/sneak-preview' },
      { text: 'Getting Started', link: '/getting-started' },
      { text: 'How It Works', link: '/how-it-works' },
      { text: 'Limitations', link: '/limitations' },
      { text: 'Changelog', link: '/changelog' },
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/pshevche/spockk' },
    ],

    search: {
      provider: 'local',
    },

    footer: {
      message: 'Released under the Apache License 2.0.',
      copyright: 'Spockk is an independent add-on and is not affiliated with the Spock Framework project.',
    },
  },
})

import type { Theme } from 'vitepress'
import DefaultTheme from 'vitepress/theme'

import ComparisonPanels from './components/ComparisonPanels.vue'
import RevealCode from './components/RevealCode.vue'
import TerminalWindow from './components/TerminalWindow.vue'
import TransformCarousel from './components/TransformCarousel.vue'
import TransformSlide from './components/TransformSlide.vue'
import { vReveal } from './reveal'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('RevealCode', RevealCode)
    app.component('TerminalWindow', TerminalWindow)
    app.component('TransformCarousel', TransformCarousel)
    app.component('TransformSlide', TransformSlide)
    app.component('ComparisonPanels', ComparisonPanels)
    app.directive('reveal', vReveal)
  },
} satisfies Theme

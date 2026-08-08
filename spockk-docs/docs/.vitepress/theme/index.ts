/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

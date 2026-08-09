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

import '@fontsource/jetbrains-mono/400.css'
import '@fontsource/jetbrains-mono/500.css'
import '@fontsource/jetbrains-mono/700.css'

import CodeCarousel from './components/CodeCarousel.vue'
import CarouselSlide from './components/CarouselSlide.vue'
import CodeWindow from './components/CodeWindow.vue'
import ComparisonPanels from './components/ComparisonPanels.vue'
import { vReveal } from './reveal'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('CodeWindow', CodeWindow)
    app.component('CodeCarousel', CodeCarousel)
    app.component('CarouselSlide', CarouselSlide)
    app.component('ComparisonPanels', ComparisonPanels)
    app.directive('reveal', vReveal)
  },
} satisfies Theme

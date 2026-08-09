<script setup lang="ts">
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

import { onMounted, useTemplateRef } from 'vue'

withDefaults(defineProps<{ title?: string }>(), {
  title: '',
})

const root = useTemplateRef<HTMLDivElement>('root')

onMounted(() => {
  const el = root.value
  if (!el) return

  const lines = el.querySelectorAll<HTMLElement>('.line')
  lines.forEach((line, i) => line.style.setProperty('--i', String(i)))

  const play = () => el.classList.add('is-playing')

  const reducedMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
  if (reducedMotion || typeof IntersectionObserver === 'undefined') {
    play()
    return
  }

  // Generous rootMargin so the reveal starts just before the block is on
  // screen; the timeout is a safety net in case the observer never fires.
  const observer = new IntersectionObserver(
    (entries) => {
      if (entries.some((entry) => entry.isIntersecting)) {
        play()
        observer.disconnect()
      }
    },
    { rootMargin: '400px 0px' },
  )
  observer.observe(el)
  setTimeout(play, 1500)
})
</script>

<template>
  <div ref="root" class="code-window">
    <div class="code-window-titlebar">
      <span class="dot dot-red" />
      <span class="dot dot-yellow" />
      <span class="dot dot-green" />
      <span v-if="title" class="code-window-title">{{ title }}</span>
    </div>
    <div class="code-window-body">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.code-window {
  border-radius: 10px;
  overflow: hidden;
  background: var(--vp-code-block-bg);
  border: 1px solid var(--vp-c-divider);
  box-shadow: 0 12px 30px -14px rgba(0, 0, 0, 0.4);
}

.code-window-titlebar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: var(--vp-c-bg-soft);
  border-bottom: 1px solid var(--vp-c-divider);
}

.dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  display: inline-block;
}

.dot-red {
  background: #ff5f56;
}

.dot-yellow {
  background: #ffbd2e;
}

.dot-green {
  background: #27c93f;
}

.code-window-title {
  margin-left: 8px;
  font-size: var(--spockk-text-meta);
  color: var(--vp-c-text-3);
  font-family: var(--vp-font-family-mono);
}

.code-window-body {
  padding: 4px 0;
}

/* Don't rely on .vp-doc's ancestor-scoped overflow rule: CodeWindow is
   also used outside .vp-doc (e.g. the home page hero). */
.code-window-body :deep(div[class*='language-']) {
  margin: 0;
  border-radius: 0;
  overflow-x: auto;
}

.code-window-body :deep(pre) {
  overflow-x: auto;
  text-align: left;
}

/* `code` is inline by default outside .vp-doc, so an ancestor's
   text-align (e.g. the centered mobile hero) would center each source
   line independently by its own width instead of flush-left. */
.code-window-body :deep(code) {
  display: block;
  width: fit-content;
  min-width: 100%;
  text-align: left;
}

.code-window-body :deep(.lang) {
  display: none;
}

.code-window-body :deep(.line) {
  opacity: 0;
  transform: translateY(4px);
  animation: reveal-line 0.45s ease forwards;
  animation-play-state: paused;
  animation-delay: calc(var(--i, 0) * 65ms);
}

.code-window.is-playing .code-window-body :deep(.line) {
  animation-play-state: running;
}

@media (prefers-reduced-motion: reduce) {
  .code-window-body :deep(.line) {
    animation: none;
    opacity: 1;
    transform: none;
  }
}

@keyframes reveal-line {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

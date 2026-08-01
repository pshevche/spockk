<script setup lang="ts">
import { onMounted, useTemplateRef } from 'vue'

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
  <div ref="root" class="reveal-code">
    <slot />
  </div>
</template>

<style scoped>
.reveal-code :deep(.line) {
  opacity: 0;
  transform: translateY(4px);
  animation: reveal-line 0.45s ease forwards;
  animation-play-state: paused;
  animation-delay: calc(var(--i, 0) * 65ms);
}

.reveal-code.is-playing :deep(.line) {
  animation-play-state: running;
}

@keyframes reveal-line {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

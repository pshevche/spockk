<script setup lang="ts">
defineProps<{ title: string; description?: string }>()
</script>

<template>
  <div class="transform-slide">
    <div class="transform-slide-header">
      <h3>{{ title }}</h3>
      <p v-if="description">{{ description }}</p>
    </div>
    <div class="transform-slide-panes">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.transform-slide {
  flex: 0 0 100%;
  scroll-snap-align: start;
  box-sizing: border-box;
  padding: 1.75rem 1.5rem;
}

.transform-slide-header {
  max-width: 60ch;
  margin: 0 auto 1.5rem;
  text-align: center;
}

.transform-slide-header h3 {
  font-size: 1.25rem;
  margin: 0 0 0.5rem;
  border: none;
  padding: 0;
}

.transform-slide-header p {
  color: var(--vp-c-text-2);
  margin: 0;
}

.transform-slide-panes {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-auto-flow: column;
  grid-template-rows: auto 1fr;
  gap: 0.5rem 1.25rem;
  align-items: start;
}

.transform-slide-panes :deep(p) {
  margin: 0;
  font-weight: 600;
  font-size: 0.8rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--vp-c-text-3);
}

.transform-slide-panes :deep(p):nth-of-type(1)::before {
  content: '✍️ ';
}

.transform-slide-panes :deep(p):nth-of-type(2)::before {
  content: '⚙️ ';
}

.transform-slide-panes :deep(div[class*='language-']) {
  margin: 0;
  height: 100%;
  font-size: 0.82em;
}

@media (max-width: 640px) {
  .transform-slide-panes {
    grid-template-columns: 1fr;
    grid-auto-flow: row;
    grid-template-rows: auto;
  }
}
</style>

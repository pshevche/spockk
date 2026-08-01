<script setup lang="ts">
withDefaults(defineProps<{ title?: string }>(), {
  title: 'terminal',
})
</script>

<template>
  <div class="terminal-window">
    <div class="terminal-titlebar">
      <span class="dot dot-red" />
      <span class="dot dot-yellow" />
      <span class="dot dot-green" />
      <span class="terminal-title">{{ title }}</span>
    </div>
    <div class="terminal-body">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.terminal-window {
  border-radius: 10px;
  overflow: hidden;
  background: var(--vp-code-block-bg);
  border: 1px solid var(--vp-c-divider);
  box-shadow: 0 12px 30px -14px rgba(0, 0, 0, 0.4);
}

.terminal-titlebar {
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

.terminal-title {
  margin-left: 8px;
  font-size: 12px;
  color: var(--vp-c-text-3);
  font-family: var(--vp-font-family-mono);
}

.terminal-body {
  padding: 4px 0;
}

.terminal-body :deep(div[class*='language-']) {
  margin: 0;
  border-radius: 0;
}
</style>

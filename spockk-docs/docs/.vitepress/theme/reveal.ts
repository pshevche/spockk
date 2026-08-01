import type { Directive } from 'vue'

let observer: IntersectionObserver | null = null

function getObserver(): IntersectionObserver | null {
  if (observer) return observer
  if (typeof window === 'undefined') return null

  observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          entry.target.classList.add('in-view')
          observer?.unobserve(entry.target)
        }
      }
    },
    { threshold: 0.2 },
  )
  return observer
}

/** Adds an `in-view` class to the bound element once it scrolls into the viewport. */
export const vReveal: Directive = {
  mounted(el: Element) {
    getObserver()?.observe(el)
  },
  unmounted(el: Element) {
    observer?.unobserve(el)
  },
}

import { computed, ref } from 'vue'

export function useMemoPanelFocus() {
  const focusSection = ref(null)

  const toggleTodo = () => {
    focusSection.value = focusSection.value === 'todo' ? null : 'todo'
  }

  const toggleImportant = () => {
    focusSection.value = focusSection.value === 'important' ? null : 'important'
  }

  const isTodoFocused = computed(() => focusSection.value === 'todo')
  const isImportantFocused = computed(() => focusSection.value === 'important')
  const isTodoCollapsed = computed(() => focusSection.value === 'important')
  const isImportantCollapsed = computed(() => focusSection.value === 'todo')

  const focusClasses = computed(() => ({
    'is-todo-focused': isTodoFocused.value,
    'is-important-focused': isImportantFocused.value
  }))

  return {
    focusSection,
    toggleTodo,
    toggleImportant,
    isTodoFocused,
    isImportantFocused,
    isTodoCollapsed,
    isImportantCollapsed,
    focusClasses
  }
}

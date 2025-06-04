import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { UserData } from '@/dto/userDto'

export const useUserDataStore = defineStore('userData', () => {
  const userData = ref<UserData | null>(null)
  const value = computed(() => userData.value)
  function set(value: UserData | null) {
    userData.value = value
  }
  function clear() {
    userData.value = null
  }

  return { userData: userData, value, set, clear }
})

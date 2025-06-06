import './assets/main.css'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css' // 用于支持暗黑模式
import '@/assets/iconfont/iconfont.css'

import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn' // 导入dayjs本地化语言
import localizedFormat from 'dayjs/plugin/localizedFormat'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useDark, useToggle } from '@vueuse/core'

const app = createApp(App)

// 实现深色模式切换
export const isDark = useDark()
export const toggleDark = useToggle(isDark)
dayjs.extend(localizedFormat)
dayjs.locale('zh-cn') // dayjs使用本地化语言

app.use(createPinia())
app.use(router)

app.mount('#app')

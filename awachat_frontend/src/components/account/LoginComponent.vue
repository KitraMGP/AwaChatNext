<script setup lang="ts">
import type { LoginRequest } from '@/dto/userDto';
import router from '@/router';
import { checkSuccessful, getErrorMsg, showFailMessage, showSuccessfulMessage } from '@/services/api';
import { loginApi } from '@/services/userApi';
import { useUserDataStore } from '@/stores/userDataStore';
import { ElMessage, type FormRules } from 'element-plus';
import { reactive, ref, watch } from 'vue';

const form = reactive({
  username: "",
  password: ""
})
// 使用全局存储的用户数据
const userDataStore = useUserDataStore()

// 检测用户是否已登录，若已登录则回到上一页
watch(userDataStore, (newValue) => {
  if (newValue.value != null) {
    ElMessage({
      message: "您已登录",
      type: "info",
      duration: 2000
    })
    router.replace("/")
  }
})



// 控制登录按钮是否可用
const loginDisabled = ref(false)

// 用户名校验函数
const validateUsername = (rule: unknown, value: string, callback: (error?: string | Error) => void) => {
  if (value.trim().length === 0) {
    callback(new Error("请输入用户名"))
  } else {
    callback()
  }
}

// 密码校验函数
const validatePassword = (_rule: unknown, value: string, callback: (error?: string | Error) => void) => {
  if (value.trim().length === 0) {
    callback(new Error("请输入密码"))
  } else {
    callback()
  }
}

// 表单校验规则
const rules = reactive<FormRules<typeof form>>({
  username: [{ validator: validateUsername, trigger: "blur" }],
  password: [{ validator: validatePassword, trigger: "blur" }]
})

// 登录
function onLogin() {
  userDataStore.clear()
  const request: LoginRequest = {
    username: form.username,
    password: form.password
  }
  loginDisabled.value = true
  // 发送登录请求
  loginApi(request).then(resp => {
    if (!checkSuccessful(resp)) {
      loginDisabled.value = false
      showFailMessage("登录失败", getErrorMsg(resp))
      return;
    }
    userDataStore.set(resp.data.data.userData)
    // 将token保存到localStorage中
    localStorage.setItem('satoken', resp.data.data.satoken)

    showSuccessfulMessage("登录成功")
  }).catch(e => {
    showFailMessage("登录失败", e)
    loginDisabled.value = false
  })
}
</script>

<template>
  <div class="login">
    <span class="title">
      登录
    </span>
    <el-form :model="form" label-width="auto" size="large" :rules="rules">
      <el-form-item label="用户名" prop="username">
        <el-input placeholder="用户唯一名称" v-model="form.username"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input type="password" placeholder="登录密码" v-model="form.password"></el-input>
      </el-form-item>
      <div class="login-button-container">
        <el-button type="primary" @click="onLogin()" native-type="submit" :disabled="loginDisabled"
          class="login-button">登录</el-button>
      </div>
    </el-form>

    <div class="tips">
      还没有账户？<el-link @click="router.push('/register')">立即注册</el-link>
    </div>
  </div>
</template>

<style lang="css" scoped>
.login {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 400px;
  border: 1px solid var(--el-border-color-darker);
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow);
  padding: 1rem 4rem 1rem 4rem;
}

.title {
  text-align: center;
  font-size: 2rem;
  margin-top: 0.5rem;
  margin-bottom: 1rem;
}

.login-button-container {
  width: 100%;
  text-align: center;
}

.login-button {
  width: 5rem;
}

.tips {
  color: var(--el-color-info);
}
</style>

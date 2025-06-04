<script setup lang="ts">
import router from '@/router';
import { checkSuccessful, getErrorMsg, showFailMessage, showSuccessfulMessage } from '@/services/api';
import { registerApi } from '@/services/userApi';
import { useUserDataStore } from '@/stores/userDataStore';
import { ElNotification, type FormRules } from 'element-plus';
import { reactive, ref, watch } from 'vue';

const form = reactive({
  username: "",
  nickname: "",
  password: ""
})

const userDataStore = useUserDataStore()

// 检测用户是否已登录，若已登录则回到上一页
watch(userDataStore, (newValue) => {
  // 若用户是因为进行登录操作而更改了userDataStore，则不进行这里的跳转
  if (registerDisabled.value) {
    return
  }
  if (newValue.value != null) {
    ElNotification({
      title: "您已登录",
      type: "info",
      duration: 2000
    })
    router.replace("/")
  }
})

// 控制登录按钮是否可用
const registerDisabled = ref(false)

// 用户名校验函数
const validateUsername = (rule: unknown, value: string, callback: (error?: string | Error) => void) => {
  if (value.trim().length === 0) {
    callback(new Error("请输入用户名"))
  } else if (value.indexOf("@") != -1) {
    callback(new Error("用户名中不需要输入@"))
  } else {
    callback()
  }
}

// 昵称校验函数
const validateNickname = (rule: unknown, value: string, callback: (error?: string | Error) => void) => {
  if (value.trim().length === 0) {
    callback(new Error("请输入昵称"))
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
  nickname: [{ validator: validateNickname, trigger: "blur" }],
  password: [{ validator: validatePassword, trigger: "blur" }]
})

function onRegister() {
  registerDisabled.value = true
  registerApi({ username: form.username.trim(), nickname: form.nickname.trim(), password: form.password }).then(resp => {
    if (!checkSuccessful(resp)) {
      showFailMessage("注册失败", getErrorMsg(resp))
      registerDisabled.value = false
      return
    }
    showSuccessfulMessage("注册成功")
    router.back()
  }).catch(e => {
    showFailMessage("注册失败", e)
    registerDisabled.value = false
  })
}

</script>

<template>
  <div class="register">
    <span class="title">
      注册
    </span>
    <el-form :model="form" label-width="auto" size="large" :rules="rules">
      <el-form-item label="用户名" prop="username">
        <el-input placeholder="用户唯一名称" v-model="form.username"></el-input>
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input placeholder="用户昵称" v-model="form.nickname"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input type="password" placeholder="登录密码" v-model="form.password"></el-input>
      </el-form-item>
      <div class="register-button-container">
        <el-button type="primary" @click="onRegister()" native-type="submit" :disabled="registerDisabled"
          class="register-button">注册</el-button>
      </div>
    </el-form>
  </div>
</template>

<style lang="css" scoped>
.register {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 400px;
  border: 1px solid var(--el-border-color-darker);
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow);
  padding: 1rem 4rem 1.5rem 4rem;
}

.title {
  text-align: center;
  font-size: 2rem;
  margin-top: 0.5rem;
  margin-bottom: 1rem;
}

.register-button-container {
  width: 100%;
  text-align: center;
}

.register-button {
  width: 5rem;
}
</style>

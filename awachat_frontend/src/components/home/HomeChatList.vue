<script setup lang="ts">
import type { DisplayChat } from '@/dto/chat';
import SimpleAvatar from '../avatar/SimpleAvatar.vue';
import { ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getUserInfoApi, logoutApi } from '@/services/userApi';
import { useUserDataStore } from '@/stores/userDataStore';
import { websocketService } from '@/services/websocketService';
import { createFriendRequestMessage } from '@/dto/websocket';
import router from '@/router';

const props = defineProps<{
  chats: DisplayChat[],
  selectedChatId: number | null
}>()

const userDataStore = useUserDataStore()
const emit = defineEmits(["select-chat", "update-chatlist"])

// 添加好友对话框
const addFriendDialogVisible = ref(false);
const usernameInput = ref('');

// 下拉菜单选项
function handleCommand(command: string): void {
  // 处理菜单命令
  console.log(`点击了${command}选项`);

  if (command === 'addFriend') {
    // 显示添加好友对话框
    addFriendDialogVisible.value = true;
    usernameInput.value = ''; // 清空输入框
  } else if (command === 'logout') {
    logout()
  }
}

// 退出登录按钮逻辑
async function logout() {
  try {
    const resp = await logoutApi()
    if (resp.data.code === 200) {
      userDataStore.clear()
      localStorage.removeItem('satoken')
      ElMessage({
        message: '您已退出登录',
        type: 'success'
      })
      router.replace('/login')
    } else {
      ElMessage({
        message: resp.data.msg,
        type: 'warning'
      })
    }
  } catch (e) {
    ElMessage({
      message: '发生错误',
      type: 'warning'
    })
    console.error(e)
  }
}

// 处理添加好友
async function handleAddFriend() {
  if (!usernameInput.value) {
    ElMessage.warning('请输入用户名');
    return;
  }

  try {
    // 调用用户信息API查询用户
    const response = await getUserInfoApi(undefined, usernameInput.value);

    if (response.data.code === 200) {
      const userData = response.data.data;

      // 检查用户是否存在
      if (!userData) {
        ElMessage.error('用户不存在');
        return;
      }

      // 检查是否已经是好友
      if (userData.isFriend) {
        ElMessage.warning('该用户已经是您的好友');
        return;
      }

      // 检查是否为自己
      if (userDataStore.value?.userId === userData.userId) {
        ElMessage.warning('不能添加自己为好友');
        return;
      }

      // 确认是否发送好友请求
      ElMessageBox.confirm(
        `确定要向 ${userData.nickname || userData.username} 发送好友请求吗？`,
        '发送好友请求',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
        }
      ).then(() => {
        // 获取当前用户ID
        const currentUserStore = useUserDataStore();
        const currentUserId = currentUserStore.value?.userId;

        if (!currentUserId) {
          ElMessage.error('获取当前用户信息失败');
          return;
        }

        // 创建好友请求消息
        const friendRequestMessage = createFriendRequestMessage(
          0, // chatId为0，表示新会话
          currentUserId,
          userData.userId,
          false // isAccepted初始为false
        );

        // 发送好友请求消息
        websocketService.sendChatMessage(friendRequestMessage);

        ElMessage.success('好友请求已发送');
        emit('update-chatlist') // 要求重新获取聊天列表，因为这个操作创建了新会话
        addFriendDialogVisible.value = false; // 关闭对话框
      }).catch(() => {
        // 用户取消操作
      });
    } else {
      ElMessage.error(response.data.msg || '查询用户失败');
    }
  } catch (error) {
    console.error('查询用户失败:', error);
    ElMessage.error('查询用户失败，请稍后重试');
  }
}
</script>

<template>
  <div class="chat">
    <div class="title-bar">
      <h3>会话列表</h3>
      <el-dropdown @command="handleCommand">
        <span class="el-dropdown-link">
          <i class="iconfont icon-more" title="更多"></i>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>用户名：{{ userDataStore.value?.username }}</el-dropdown-item>
            <el-dropdown-item command="addFriend">添加好友</el-dropdown-item>
            <el-dropdown-item command="newFriends">新朋友</el-dropdown-item>
            <el-dropdown-item command="createGroup">发起群聊</el-dropdown-item>
            <el-dropdown-item command="profile">个人信息</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 添加好友对话框 -->
    <el-dialog v-model="addFriendDialogVisible" title="添加好友" width="30%" center>
      <el-form>
        <el-form-item label="用户名">
          <el-input v-model="usernameInput" placeholder="请输入用户名"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addFriendDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddFriend">查找并添加</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 会话列表 -->
    <div class="chat-items">
      <div v-for="item in props.chats" :key="item.id" class="chat-item"
        :class="{ 'active': selectedChatId === item.id }" @click="emit('select-chat', item.id)">
        <SimpleAvatar :text="item.name" size="medium" />
        <div class="content">
          <div class="name-time">
            <span class="name">{{ item.name }}</span>
            <span class="time">{{ item.time }}</span>
          </div>
          <div class="message-unread">
            <span class="message">{{ item.lastMessage }}</span>
            <el-badge :value="item.unread" :hidden="item.unread === 0" class="unread-badge" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="css" scoped>
/* 左侧会话列表样式 */
.chat {
  width: 280px;
  border-right: 1px solid #dcdfe6;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.title-bar {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
}

.title-bar h3 {
  margin: 0;
  font-size: 16px;
}

.el-dropdown-link {
  cursor: pointer;
  outline: none;
  /* 去除焦点轮廓 */
}

/* 添加全局样式，确保下拉菜单不显示黑色框 */
:deep(.el-dropdown-link:focus) {
  outline: none;
  box-shadow: none;
}

:deep(.el-dropdown-link:focus-visible) {
  outline: none;
  box-shadow: none;
}

.chat-items {
  flex: 1;
  overflow-y: auto;
}

.chat-item {
  padding: 12px 15px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.3s;
}

.chat-item:hover {
  background-color: #f5f7fa;
}

.chat-item.active {
  background-color: #ecf5ff;
}

.content {
  flex: 1;
  overflow: hidden;
}

.name-time {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.name {
  font-weight: bold;
  color: #303133;
}

.time {
  font-size: 12px;
  color: #909399;
}

.message-unread {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message {
  color: #606266;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}

.unread-badge {
  margin-left: 5px;
}
</style>

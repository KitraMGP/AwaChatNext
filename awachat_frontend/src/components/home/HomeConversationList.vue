<script setup lang="ts">
import type { Conversation } from '@/dto/base';
import SimpleAvatar from '../avatar/SimpleAvatar.vue';

const props = defineProps<{
  conversations: Conversation[],
  selectedConversation: number | null
}>()

const emit = defineEmits(["select-conversation"])

// 下拉菜单选项，改为function定义并添加参数类型
function handleCommand(command: string): void {
  // 处理菜单命令
  console.log(`点击了${command}选项`);
}

</script>

<template>
  <div class="conversation-list">
    <div class="title-bar">
      <h3>会话列表</h3>
      <el-dropdown @command="handleCommand">
        <span class="el-dropdown-link">
          <i class="iconfont icon-more" title="更多"></i>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="addFriend">添加好友</el-dropdown-item>
            <el-dropdown-item command="newFriends">新朋友</el-dropdown-item>
            <el-dropdown-item command="createGroup">发起群聊</el-dropdown-item>
            <el-dropdown-item command="profile">个人信息</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div class="conversation-items">
      <div v-for="item in props.conversations" :key="item.id" class="conversation-item"
        :class="{ 'active': selectedConversation === item.id }" @click="emit('select-conversation', item.id)">
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
.conversation-list {
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

.conversation-items {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  padding: 12px 15px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.3s;
}

.conversation-item:hover {
  background-color: #f5f7fa;
}

.conversation-item.active {
  background-color: #ecf5ff;
}

/* .avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  margin-right: 12px;
} */

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

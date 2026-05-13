<script setup>
import router from '@/router/index.js'
import { useUserStore } from '@/stores/user.js'
import { computed } from 'vue'
import defaultAvatar from '@/assets/img/common/user.jpg'
import { User, SwitchButton } from '@element-plus/icons-vue'
import { logout } from '@/utils/login/logout.js'

const userStore = useUserStore()

// OSS 域名配置
const OSS_DOMAIN = 'https://fifteen-hundun.oss-cn-beijing.aliyuncs.com'

const avatarSrc = computed(() => {
  const avatarUrl = userStore.userInfo?.avatarUrl
  if (!avatarUrl) return defaultAvatar
  // 如果已经是完整 URL，直接返回；否则拼接 OSS 域名
  return avatarUrl.startsWith('http') ? avatarUrl : `${OSS_DOMAIN}/${avatarUrl}`
})

const handleCommand =  async (command) => {
  if (command === 'info') {
    router.push('/user/info')
  } else if (command === 'logout') {
    await logout()
  }
}

const list=[
  { id: 1, name:'首页' ,to:'/user'},
  { id: 2, name:'录入' ,to:'/user/insert'},
  { id: 3, name:'翻找' ,to:'/user/select'},
  { id: 4, name:'帮助' ,to:'/help'}
]
const handleRouter = (to)=>{
  if (to) {
    router.replace(to)
  }
}
</script>

<template>
  <div class="header" >
    <div class="logo-box" @click="router.push('/user')">
      <img src="@/assets/img/common/biglogo.png" alt="Logo">
    </div>
    <div class="main">
      <ul>
        <li v-for="(item) in list"
          @click="handleRouter(item.to)">
          <span>{{item.name}}</span>
        </li>
      </ul>
    </div>
    <el-dropdown v-if="userStore.userInfo" @command="handleCommand" trigger="hover">
      <div class="user-box">
        <img :src="avatarSrc" alt="User">
        <span class="username">{{ userStore.userInfo.nickname||userStore.userInfo.username}}</span>
      </div>
      <template #dropdown>
        <el-dropdown-menu class="custom-dropdown">
          <el-dropdown-item command="info">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-dropdown-item>
          <el-dropdown-item divided command="logout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<style scoped lang="less">
  .header{
    display: flex;
    justify-content: space-between;
    align-items: center;
    position:fixed;
    height: 80px;
    width: 100%;
    opacity: 0.95;
    background-color: #FFF8E8;
    overflow: hidden;
    z-index: 999;
    border-radius: 0 0 40px 40px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 6px 25px rgba(0, 0, 0, 0.12);
    }

    .logo-box{
      margin: 10px 10px 10px 50px;
      height: 60px;
      cursor: pointer;
      transition: transform 0.3s ease;

      &:hover {
        transform: scale(1.05);
      }

      img{
        height: 60px;
        object-fit: contain;
      }
    }

    .main{
      ul{
        display: flex;
        flex-direction: row;
        justify-content: center;
        align-items: center;
        height: 80px;
        list-style: none;
        padding: 0;
        margin: 0;
        user-select: none;
        -webkit-user-select: none;
        -moz-user-select: none;
        -ms-user-select: none;

        li{
          position: relative;
          margin: 0 25px;
          padding: 10px 15px;
          cursor: pointer;
          transition: all 0.3s ease;
          border-radius: 8px;

          span {
            font-size: 16px;
            font-weight: 500;
            letter-spacing: 0.5px;
            transition: color 0.3s ease;
          }

          &::after {
            content: '';
            position: absolute;
            bottom: 5px;
            left: 50%;
            width: 0;
            height: 2px;
            background-color: #C3272B;
            transition: all 0.3s ease;
            transform: translateX(-50%);
          }

          &:hover {
            background-color: rgba(195, 39, 43, 0.05);

            span {
              color: #C3272B;
            }

            &::after {
              width: 70%;
            }
          }
        }
      }
    }

    .user-box{
      display: flex;
      align-items: center;
      height: 60px;
      margin: 10px 50px 10px 10px;
      cursor: pointer;
      outline: none;
      padding: 5px 10px;
      border-radius: 30px;
      transition: all 0.3s ease;

      &:hover {
        background-color: rgba(195, 39, 43, 0.05);
        transform: translateY(-2px);
      }

      &:focus {
        outline: none;
      }

      img{
        height: 50px;
        width: 50px;
        border-radius: 50%;
        overflow: hidden;
        object-fit: cover;
        border: 2px solid #FFF8E8;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        transition: all 0.3s ease;
      }

      .username{
        margin-left: 12px;
        font-size: 18px;
        font-weight: 500;
        line-height: 1;
        color: #333;
        user-select: none;
        -webkit-user-select: none;
        -moz-user-select: none;
        -ms-user-select: none;
        transition: color 0.3s ease;
      }

      &:hover .username {
        color: #C3272B;
      }
    }

    // 移除 el-dropdown 的默认边框
    :deep(.el-dropdown) {
      outline: none;

      &:focus {
        outline: none;
      }

      .el-dropdown-link {
        outline: none;
      }
    }

    // 下拉菜单样式优化
    :deep(.custom-dropdown) {
      border-radius: 12px;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      border: 1px solid rgba(0, 0, 0, 0.05);
      padding: 8px 0;

      .el-dropdown-menu__item {
        padding: 12px 20px;
        transition: all 0.2s ease;

        &:hover {
          background-color: rgba(195, 39, 43, 0.08);
          color: #C3272B;
        }

        .el-icon {
          margin-right: 8px;
          font-size: 16px;
        }
      }
    }
  }
</style>

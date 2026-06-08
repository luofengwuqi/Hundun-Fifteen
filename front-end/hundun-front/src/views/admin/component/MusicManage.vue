<template>
  <div v-show="!detailStatus">
    <div class="toolbar">
      <el-button type="primary" class="search-btn" @click="showAddDialog">
        + 添加音乐
      </el-button>
      <el-button @click="fetchMusicList">
        刷新
      </el-button>
    </div>

    <transition-group name="table-list" tag="div">
      <el-table
        :key="tableKey"
        :data="musicList"
        style="width: 100%"
        v-loading="loading"
        :header-cell-style="{ background: '#FFEFD5', color: '#606266', fontWeight: '600' }"
        :cell-style="{ padding: '12px 0' }"
        border
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="歌曲名" min-width="160" show-overflow-tooltip />
        <el-table-column label="时长" width="100" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploaderName" label="上传者" width="120" />
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120" align="center">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              plain
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </transition-group>
  </div>

  <el-dialog v-model="dialogVisible" title="添加音乐" width="500px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="歌曲名" required>
        <el-input v-model="form.title" placeholder="请输入歌曲名" />
      </el-form-item>
      <el-form-item label="上传者">
        <el-input v-model="form.uploaderName" placeholder="请输入上传者" />
      </el-form-item>
      <el-form-item label="封面图片">
        <el-upload
          ref="coverUploadRef"
          :auto-upload="false"
          :show-file-list="true"
          accept="image/*"
          :on-change="handleCoverFileChange"
          :limit="1"
        >
          <el-button type="primary" class="search-btn">选择图片</el-button>
          <template #tip>
            <span class="upload-tip">可选，上传后将自动压缩</span>
          </template>
        </el-upload>
        <img v-if="coverPreviewUrl" :src="coverPreviewUrl" class="cover-preview" />
      </el-form-item>
      <el-form-item label="音频文件" required>
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :show-file-list="true"
          accept="audio/*"
          :on-change="handleFileChange"
          :limit="1"
        >
          <el-button type="primary" class="search-btn">选择文件</el-button>
          <template #tip>
            <span class="upload-tip">支持 mp3/wav/flac 等格式</span>
          </template>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" class="search-btn" @click="handleAdd" :loading="uploading">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMusicStore } from '@/stores/music.js'
import { useUserStore } from '@/stores/user.js'
import { storeToRefs } from 'pinia'

const musicStore = useMusicStore()
const userStore = useUserStore()
const { musicList, loading } = storeToRefs(musicStore)
const { fetchAllMusic, addMusic, removeMusic, uploadAudio, uploadCover } = musicStore

const detailStatus = ref(false)
const dialogVisible = ref(false)
const uploading = ref(false)
const selectedFile = ref(null)
const selectedCoverFile = ref(null)
const coverPreviewUrl = ref('')
const uploadRef = ref(null)
const coverUploadRef = ref(null)

const form = ref({
  title: '',
  duration: 0,
  uploaderName: ''
})

const tableKey = computed(() => musicList.value.length)

const formatDuration = (seconds) => {
  if (!seconds && seconds !== 0) return ''
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${String(s).padStart(2, '0')}`
}

const formatDate = (dateStr) => {
  if (!dateStr) return '未记录'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const getAudioDuration = (file) => {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const audio = new Audio(url)
    audio.addEventListener('loadedmetadata', () => {
      const duration = Math.round(audio.duration)
      URL.revokeObjectURL(url)
      resolve(duration)
    })
    audio.addEventListener('error', () => {
      URL.revokeObjectURL(url)
      reject(new Error('无法解析音频文件'))
    })
  })
}

const compressImage = (file, maxWidth = 800, quality = 0.7) => {
  return new Promise((resolve) => {
    const img = new Image()
    const url = URL.createObjectURL(file)
    img.onload = () => {
      const canvas = document.createElement('canvas')
      let { width, height } = img
      if (width > maxWidth) {
        height = height * (maxWidth / width)
        width = maxWidth
      }
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, width, height)
      canvas.toBlob(
        (blob) => {
          const compressedFile = new File([blob], file.name.replace(/\.\w+$/, '.jpg'), { type: 'image/jpeg' })
          URL.revokeObjectURL(url)
          resolve(compressedFile)
        },
        'image/jpeg',
        quality
      )
    }
    img.src = url
  })
}

const fetchMusicList = async () => {
  await fetchAllMusic()
}

const showAddDialog = () => {
  form.value = { title: '', duration: 0, uploaderName: userStore.userInfo.username || '' }
  selectedFile.value = null
  selectedCoverFile.value = null
  coverPreviewUrl.value = ''
  if (uploadRef.value) uploadRef.value.clearFiles()
  if (coverUploadRef.value) coverUploadRef.value.clearFiles()
  dialogVisible.value = true
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
  if (file.raw) {
    getAudioDuration(file.raw).then(duration => {
      form.value.duration = duration
    }).catch(() => {
      console.warn('自动识别时长失败，请手动输入')
    })
  }
}

const handleCoverFileChange = (file) => {
  selectedCoverFile.value = file.raw
  coverPreviewUrl.value = URL.createObjectURL(file.raw)
}

const handleAdd = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入歌曲名')
    return
  }
  if (!selectedFile.value) {
    ElMessage.warning('请选择音频文件')
    return
  }

  uploading.value = true
  try {
    let coverUrl = ''
    if (selectedCoverFile.value) {
      const compressed = await compressImage(selectedCoverFile.value)
      const coverRes = await uploadCover(compressed)
      if (coverRes.code !== 200 && coverRes.code !== 0) {
        ElMessage.error('封面上传失败')
        return
      }
      coverUrl = coverRes.data
    }

    const uploadRes = await uploadAudio(selectedFile.value)
    if (uploadRes.code !== 200 && uploadRes.code !== 0) {
      ElMessage.error('音频上传失败')
      return
    }
    const audioUrl = uploadRes.data
    await addMusic({
      title: form.value.title,
      duration: form.value.duration,
      coverUrl,
      audioUrl,
      uploaderName: form.value.uploaderName
    })
    ElMessage.success('添加成功(´｡• ᵕ •｡`)')
    dialogVisible.value = false
  } catch (err) {
    console.error('添加音乐失败:', err)
    ElMessage.error('添加失败，请重试')
  } finally {
    uploading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除歌曲「${row.title}」吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await removeMusic(row.id)
    ElMessage.success('删除成功(´｡• ᵕ •｡`)')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

onMounted(() => {
  fetchMusicList()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
  background: #FFF8E8;
  padding: 24px 24px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

/* 查询按钮样式 */
.search-btn {
  background-color: #C3272B !important;
  border-color: #C3272B !important;
}

.search-btn:hover {
  background-color: #D64A4F !important;
  border-color: #D64A4F !important;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background-color: #FFF8E8;
}

/* 表格头部样式 */
.el-table th {
  background-color: #FFEFD5 !important;
  color: #606266;
  font-weight: 600;
}

/* 表格行样式 */
:deep(.el-table__row) {
  background-color: #FFF8E8 !important;
}

:deep(.el-table__row td) {
  background-color: #FFF8E8 !important;
}

:deep(.el-table th.el-table__cell) {
  background-color: #FFEFD5 !important;
}

:deep(.el-table td.el-table__cell) {
  background-color: #FFF8E8 !important;
}

:deep(.el-table__body tr) {
  background-color: #FFF8E8 !important;
}

/* 按钮组样式 */
.el-table .el-button {
  margin: 2px;
  min-width: 60px;
}

/* 表格列表过渡动画 */
.table-list-enter-active,
.table-list-leave-active {
  transition: all 0.4s ease;
}

.table-list-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.table-list-enter-to {
  opacity: 1;
  transform: translateY(0);
}

/* 表格行逐个出现动画 */
:deep(.el-table__body tr) {
  animation: table-row-fade-in 0.3s ease-in-out forwards;
  opacity: 0;
}

:deep(.el-table__body tr:nth-child(1)) { animation-delay: 0.05s; }
:deep(.el-table__body tr:nth-child(2)) { animation-delay: 0.1s; }
:deep(.el-table__body tr:nth-child(3)) { animation-delay: 0.15s; }
:deep(.el-table__body tr:nth-child(4)) { animation-delay: 0.2s; }
:deep(.el-table__body tr:nth-child(5)) { animation-delay: 0.25s; }
:deep(.el-table__body tr:nth-child(6)) { animation-delay: 0.3s; }
:deep(.el-table__body tr:nth-child(7)) { animation-delay: 0.35s; }
:deep(.el-table__body tr:nth-child(8)) { animation-delay: 0.4s; }
:deep(.el-table__body tr:nth-child(9)) { animation-delay: 0.45s; }
:deep(.el-table__body tr:nth-child(10)) { animation-delay: 0.5s; }

@keyframes table-row-fade-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.cover-preview {
  margin-top: 8px;
  max-width: 120px;
  max-height: 120px;
  border-radius: 4px;
  object-fit: cover;
  border: 1px solid #e0e0e0;
}
</style>

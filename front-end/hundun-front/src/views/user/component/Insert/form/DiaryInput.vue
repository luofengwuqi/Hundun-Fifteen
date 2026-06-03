<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { analyzeDiaryAPI, confirmDiaryAPI } from '@/api/ai.js'

const emit = defineEmits(['back'])

// 状态管理
const STEP = { INPUT: 1, REVIEW: 2, SUBMITTING: 3, DONE: 4 }
const step = ref(STEP.INPUT)
const diaryText = ref('')
const analyzing = ref(false)

// AI 返回的 snake_case → 前端 camelCase 映射
const AI_KEY_MAP = {
  food_health: 'foodHealth',
  food_finance: 'foodFinance',
  sleep_health: 'sleepHealth',
  mood_health: 'moodHealth',
  place_health: 'placeHealth',
  place_finance: 'placeFinance',
  payment: 'payments',
  memo_common: 'memoCommons',
  memo_important: 'memoImportants'
}

// AI 分析结果（原始解析后的数据）
const rawData = ref(null)
// 跟踪各模块是否被用户删除
const included = reactive({})
// 可编辑的各模块数据副本
const editable = reactive({})

const MODULE_KEYS = Object.values(AI_KEY_MAP)

const initModules = () => {
  MODULE_KEYS.forEach(k => {
    included[k] = true
    editable[k] = null
  })
}
initModules()

// 判断某个字段是否有实际值（非 null/undefined/空串）
const isFieldFilled = (v) => v !== null && v !== undefined && v !== ''

// 判断某个模块是否有数据
// - 对象类型：至少有一个字段有值
// - 数组类型：数组非空
const hasData = (key) => {
  if (!rawData.value) return false
  const val = rawData.value[key]
  if (val === null || val === undefined) return false
  if (Array.isArray(val)) return val.length > 0
  if (typeof val === 'object') return Object.values(val).some(isFieldFilled)
  return true
}

// 有数据的模块列表
const activeModules = computed(() => {
  if (!rawData.value) return []
  const keys = [
    { key: 'foodHealth', label: '饮食记录', icon: '🍜' },
    { key: 'foodFinance', label: '餐饮花费', icon: '💰' },
    { key: 'sleepHealth', label: '睡眠记录', icon: '😴' },
    { key: 'moodHealth', label: '心情记录', icon: '💭' },
    { key: 'placeHealth', label: '外出活动', icon: '📍' },
    { key: 'placeFinance', label: '交通花费', icon: '🚇' },
    { key: 'payments', label: '消费记录', icon: '🛒' },
    { key: 'memoCommons', label: '待办事项', icon: '📝' },
    { key: 'memoImportants', label: '重要备忘', icon: '📌' }
  ]
  return keys.filter(m => hasData(m.key) && included[m.key])
})

// 是否有任何模块可以提交
const hasAnyData = computed(() => activeModules.value.length > 0)

// 编辑对话框
const editDialogVisible = ref(false)
const editingKey = ref('')
const editingData = ref(null)

const openEdit = (key) => {
  editingKey.value = key
  editingData.value = JSON.parse(JSON.stringify(rawData.value[key]))
  editDialogVisible.value = true
}

const saveEdit = () => {
  rawData.value[editingKey.value] = JSON.parse(JSON.stringify(editingData.value))
  editDialogVisible.value = false
  ElMessage.success('已更新')
}

const removeModule = (key) => {
  included[key] = false
  ElMessage.info('已移除该项')
}

// 解析字段展示
const formatField = (key, field) => {
  const val = rawData.value[key]
  if (!val) return '—'
  const v = val[field]
  if (v === null || v === undefined) return '—'
  return String(v)
}

const formatArrayField = (key, field) => {
  const arr = rawData.value[key]
  if (!arr || !Array.isArray(arr)) return '—'
  return arr.map(item => item[field]).filter(v => v != null).join('、') || '—'
}

// AI 分析
const handleAnalyze = async () => {
  if (!diaryText.value.trim()) {
    ElMessage.warning('请先写点日记内容 (・ω< )')
    return
  }
  analyzing.value = true
  try {
    const res = await analyzeDiaryAPI(diaryText.value)
    const parsed = JSON.parse(res.data)
    // 将 AI 返回的 snake_case 键名转为 camelCase
    const mapped = {}
    for (const [snakeKey, camelKey] of Object.entries(AI_KEY_MAP)) {
      if (parsed[snakeKey] !== undefined) {
        mapped[camelKey] = parsed[snakeKey]
      }
    }
    rawData.value = mapped
    initModules()
    step.value = STEP.REVIEW
  } catch (e) {
    ElMessage.error('分析失败，请稍后再试')
  } finally {
    analyzing.value = false
  }
}

// 确认录入
const handleConfirm = async () => {
  if (!hasAnyData.value) {
    ElMessage.warning('没有可录入的数据')
    return
  }

  step.value = STEP.SUBMITTING
  try {
    const body = {}
    MODULE_KEYS.forEach(k => {
      if (included[k] && hasData(k)) {
        body[k] = rawData.value[k]
      }
    })

    const res = await confirmDiaryAPI(body)
    step.value = STEP.DONE
    ElMessage.success('全部录入成功！')
  } catch (e) {
    ElMessage.error('录入失败，请重试')
    step.value = STEP.REVIEW
  }
}

const handleBackToInput = () => {
  step.value = STEP.INPUT
  rawData.value = null
}

const handleBackHome = () => {
  emit('back')
}
</script>

<template>
  <div class="diary-input-container">
    <!-- ====== 输入步骤 ====== -->
    <template v-if="step === STEP.INPUT">
      <div class="diary-header">
        <h2 class="diary-title">✍️ 日记 AI 模式</h2>
        <p class="diary-subtitle">随便写写今天发生了什么，AI 帮你自动分析 (｡•̀ᴗ-)✧</p>
      </div>
      <div class="diary-body">
        <el-input
          v-model="diaryText"
          type="textarea"
          placeholder="今天早上吃了两个包子和一碗粥，花了12块钱。下午去公园跑了3公里，心情不错。晚上要记得买牙膏。"
          maxlength="2000"
          show-word-limit
          class="diary-textarea"
        />
        <div class="diary-actions">
          <el-button type="danger" :loading="analyzing" @click="handleAnalyze" class="analyze-btn">
            {{ analyzing ? 'AI 分析中...' : '🚀 AI 分析' }}
          </el-button>
        </div>
      </div>
    </template>

    <!-- ====== 审核步骤 ====== -->
    <template v-if="step === STEP.REVIEW">
      <div class="review-container">
        <div class="review-header">
          <h2 class="review-title">📋 AI 识别结果</h2>
          <p class="review-subtitle">请检查以下识别结果，可以编辑或删除，确认无误后点击录入</p>
        </div>

        <div class="review-cards">
          <!-- 饮食记录 -->
          <div v-if="hasData('foodHealth') && included.foodHealth" class="review-card card-food">
            <div class="card-header">
              <span class="card-icon">🍜</span>
              <span class="card-name">饮食记录</span>
              <div class="card-actions">
                <el-button size="small" text @click="openEdit('foodHealth')">编辑</el-button>
                <el-button size="small" text type="danger" @click="removeModule('foodHealth')">删除</el-button>
              </div>
            </div>
            <div class="card-body">
              <div class="field-row"><span class="field-label">早餐</span><span class="field-value">{{ formatField('foodHealth', 'morningFood') }}</span></div>
              <div class="field-row"><span class="field-label">午餐</span><span class="field-value">{{ formatField('foodHealth', 'afternoonFood') }}</span></div>
              <div class="field-row"><span class="field-label">晚餐</span><span class="field-value">{{ formatField('foodHealth', 'eveningFood') }}</span></div>
              <div class="field-row"><span class="field-label">其他</span><span class="field-value">{{ formatField('foodHealth', 'elseFood') }}</span></div>
            </div>
          </div>

          <!-- 餐饮花费 -->
          <div v-if="hasData('foodFinance') && included.foodFinance" class="review-card card-finance">
            <div class="card-header">
              <span class="card-icon">💰</span>
              <span class="card-name">餐饮花费</span>
              <div class="card-actions">
                <el-button size="small" text @click="openEdit('foodFinance')">编辑</el-button>
                <el-button size="small" text type="danger" @click="removeModule('foodFinance')">删除</el-button>
              </div>
            </div>
            <div class="card-body">
              <div class="field-row"><span class="field-label">早餐</span><span class="field-value">{{ formatField('foodFinance', 'morningFoodPrice') }} 元</span></div>
              <div class="field-row"><span class="field-label">午餐</span><span class="field-value">{{ formatField('foodFinance', 'afternoonFoodPrice') }} 元</span></div>
              <div class="field-row"><span class="field-label">晚餐</span><span class="field-value">{{ formatField('foodFinance', 'eveningFoodPrice') }} 元</span></div>
              <div class="field-row"><span class="field-label">其他</span><span class="field-value">{{ formatField('foodFinance', 'elseFoodPrice') }} 元</span></div>
            </div>
          </div>

          <!-- 睡眠记录 -->
          <div v-if="hasData('sleepHealth') && included.sleepHealth" class="review-card card-sleep">
            <div class="card-header">
              <span class="card-icon">😴</span>
              <span class="card-name">睡眠记录</span>
              <div class="card-actions">
                <el-button size="small" text @click="openEdit('sleepHealth')">编辑</el-button>
                <el-button size="small" text type="danger" @click="removeModule('sleepHealth')">删除</el-button>
              </div>
            </div>
            <div class="card-body">
              <div class="field-row"><span class="field-label">质量</span><span class="field-value">{{ { 0: '差', 1: '一般', 2: '好' }[rawData?.sleepHealth?.quality] || '—' }}</span></div>
              <div class="field-row"><span class="field-label">时长</span><span class="field-value">{{ rawData?.sleepHealth?.durationMin ? rawData.sleepHealth.durationMin + ' 分钟' : '—' }}</span></div>
            </div>
          </div>

          <!-- 心情记录 -->
          <div v-if="hasData('moodHealth') && included.moodHealth" class="review-card card-mood">
            <div class="card-header">
              <span class="card-icon">💭</span>
              <span class="card-name">心情记录</span>
              <div class="card-actions">
                <el-button size="small" text @click="openEdit('moodHealth')">编辑</el-button>
                <el-button size="small" text type="danger" @click="removeModule('moodHealth')">删除</el-button>
              </div>
            </div>
            <div class="card-body">
              <div class="field-row"><span class="field-label">心情</span><span class="field-value">{{ { 0: '很差', 1: '差', 2: '一般', 3: '好', 4: '很好' }[rawData?.moodHealth?.mood] || '—' }}</span></div>
              <div class="field-row"><span class="field-label">备注</span><span class="field-value">{{ formatField('moodHealth', 'note') }}</span></div>
            </div>
          </div>

          <!-- 外出活动 -->
          <div v-if="hasData('placeHealth') && included.placeHealth" class="review-card card-place">
            <div class="card-header">
              <span class="card-icon">📍</span>
              <span class="card-name">外出活动</span>
              <div class="card-actions">
                <el-button size="small" text @click="openEdit('placeHealth')">编辑</el-button>
                <el-button size="small" text type="danger" @click="removeModule('placeHealth')">删除</el-button>
              </div>
            </div>
            <div class="card-body">
              <div class="field-row"><span class="field-label">地点</span><span class="field-value">{{ formatField('placeHealth', 'location') }}</span></div>
              <div class="field-row"><span class="field-label">方式</span><span class="field-value">{{ formatField('placeHealth', 'transportMode') }}</span></div>
              <div class="field-row"><span class="field-label">步数</span><span class="field-value">{{ formatField('placeHealth', 'steps') }}</span></div>
            </div>
          </div>

          <!-- 交通花费 -->
          <div v-if="hasData('placeFinance') && included.placeFinance" class="review-card card-place-finance">
            <div class="card-header">
              <span class="card-icon">🚇</span>
              <span class="card-name">交通花费</span>
              <div class="card-actions">
                <el-button size="small" text @click="openEdit('placeFinance')">编辑</el-button>
                <el-button size="small" text type="danger" @click="removeModule('placeFinance')">删除</el-button>
              </div>
            </div>
            <div class="card-body">
              <div class="field-row"><span class="field-label">费用</span><span class="field-value">{{ formatField('placeFinance', 'transportCost') }} 元</span></div>
              <div class="field-row"><span class="field-label">方式</span><span class="field-value">{{ formatField('placeFinance', 'transportMode') }}</span></div>
              <div class="field-row"><span class="field-label">地点</span><span class="field-value">{{ formatField('placeFinance', 'location') }}</span></div>
            </div>
          </div>

          <!-- 消费记录 -->
          <div v-if="hasData('payments') && included.payments" class="review-card card-payment">
            <div class="card-header">
              <span class="card-icon">🛒</span>
              <span class="card-name">消费记录</span>
              <div class="card-actions">
                <el-button size="small" text @click="openEdit('payments')">编辑</el-button>
                <el-button size="small" text type="danger" @click="removeModule('payments')">删除</el-button>
              </div>
            </div>
            <div class="card-body">
              <div v-for="(item, idx) in rawData?.payments" :key="idx" class="payment-item">
                <span class="payment-name">{{ item.name }}</span>
                <span class="payment-price">{{ item.price }} 元</span>
                <span class="payment-type">{{ { 0: '未指定', 1: '生活/健康', 2: '学习/投资', 3: '社交/娱乐', 4: '责任/应急' }[item.type] || '未指定' }}</span>
              </div>
              <div v-if="!rawData?.payments?.length" class="field-value">—</div>
            </div>
          </div>

          <!-- 待办事项 -->
          <div v-if="hasData('memoCommons') && included.memoCommons" class="review-card card-memo">
            <div class="card-header">
              <span class="card-icon">📝</span>
              <span class="card-name">待办事项</span>
              <div class="card-actions">
                <el-button size="small" text @click="openEdit('memoCommons')">编辑</el-button>
                <el-button size="small" text type="danger" @click="removeModule('memoCommons')">删除</el-button>
              </div>
            </div>
            <div class="card-body">
              <div v-for="(item, idx) in rawData?.memoCommons" :key="idx" class="memo-item">
                <span class="memo-text">📌 {{ item.text }}</span>
                <span v-if="item.beginTime" class="memo-time">开始: {{ item.beginTime }}</span>
                <span v-if="item.endTime" class="memo-time">截止: {{ item.endTime }}</span>
              </div>
              <div v-if="!rawData?.memoCommons?.length" class="field-value">—</div>
            </div>
          </div>

          <!-- 重要备忘 -->
          <div v-if="hasData('memoImportants') && included.memoImportants" class="review-card card-memo-important">
            <div class="card-header">
              <span class="card-icon">📌</span>
              <span class="card-name">重要备忘</span>
              <div class="card-actions">
                <el-button size="small" text @click="openEdit('memoImportants')">编辑</el-button>
                <el-button size="small" text type="danger" @click="removeModule('memoImportants')">删除</el-button>
              </div>
            </div>
            <div class="card-body">
              <div v-for="(item, idx) in rawData?.memoImportants" :key="idx" class="memo-item">
                <span class="memo-text">⭐ {{ item.text }}</span>
              </div>
              <div v-if="!rawData?.memoImportants?.length" class="field-value">—</div>
            </div>
          </div>
        </div>

        <div class="review-footer">
          <el-button @click="handleBackToInput">← 返回重写</el-button>
          <el-button type="danger" :disabled="!hasAnyData" @click="handleConfirm" class="confirm-btn">
            ✅ 确认录入
          </el-button>
        </div>
      </div>
    </template>

    <!-- ====== 提交中步骤 ====== -->
    <template v-if="step === STEP.SUBMITTING">
      <div class="submitting-container">
        <div class="submitting-spinner">
          <div class="spinner" />
          <p class="submitting-text">正在录入数据...</p>
        </div>
      </div>
    </template>

    <!-- ====== 完成步骤 ====== -->
    <template v-if="step === STEP.DONE">
      <div class="done-container">
        <div class="done-icon">🎉</div>
        <h2 class="done-title">录入完成！</h2>
        <p class="done-text">AI 分析结果已全部成功录入</p>
        <el-button type="danger" @click="handleBackHome">返回</el-button>
      </div>
    </template>

    <!-- ====== 编辑对话框 ====== -->
    <el-dialog
      v-model="editDialogVisible"
      :title="'编辑 - ' + ({ foodHealth: '饮食记录', foodFinance: '餐饮花费', sleepHealth: '睡眠记录', moodHealth: '心情记录', placeHealth: '外出活动', placeFinance: '交通花费', payments: '消费记录', memoCommons: '待办事项', memoImportants: '重要备忘' })[editingKey] || ''"
      width="500px"
      destroy-on-close
    >
      <!-- 饮食记录编辑 -->
      <template v-if="editingKey === 'foodHealth' && editingData">
        <el-form label-width="80px">
          <el-form-item label="早餐"><el-input v-model="editingData.morningFood" /></el-form-item>
          <el-form-item label="午餐"><el-input v-model="editingData.afternoonFood" /></el-form-item>
          <el-form-item label="晚餐"><el-input v-model="editingData.eveningFood" /></el-form-item>
          <el-form-item label="其他"><el-input v-model="editingData.elseFood" /></el-form-item>
        </el-form>
      </template>

      <!-- 餐饮花费编辑 -->
      <template v-if="editingKey === 'foodFinance' && editingData">
        <el-form label-width="100px">
          <el-form-item label="早餐花费"><el-input-number v-model="editingData.morningFoodPrice" :min="0" :precision="2" style="width:100%" /></el-form-item>
          <el-form-item label="午餐花费"><el-input-number v-model="editingData.afternoonFoodPrice" :min="0" :precision="2" style="width:100%" /></el-form-item>
          <el-form-item label="晚餐花费"><el-input-number v-model="editingData.eveningFoodPrice" :min="0" :precision="2" style="width:100%" /></el-form-item>
          <el-form-item label="其他花费"><el-input-number v-model="editingData.elseFoodPrice" :min="0" :precision="2" style="width:100%" /></el-form-item>
        </el-form>
      </template>

      <!-- 睡眠记录编辑 -->
      <template v-if="editingKey === 'sleepHealth' && editingData">
        <el-form label-width="80px">
          <el-form-item label="质量">
            <el-select v-model="editingData.quality" style="width:100%">
              <el-option :label="'差 (0)'" :value="0" />
              <el-option :label="'一般 (1)'" :value="1" />
              <el-option :label="'好 (2)'" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="时长(分)"><el-input-number v-model="editingData.durationMin" :min="0" style="width:100%" /></el-form-item>
        </el-form>
      </template>

      <!-- 心情记录编辑 -->
      <template v-if="editingKey === 'moodHealth' && editingData">
        <el-form label-width="80px">
          <el-form-item label="心情">
            <el-select v-model="editingData.mood" style="width:100%">
              <el-option label="很差 (0)" :value="0" />
              <el-option label="差 (1)" :value="1" />
              <el-option label="一般 (2)" :value="2" />
              <el-option label="好 (3)" :value="3" />
              <el-option label="很好 (4)" :value="4" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注"><el-input v-model="editingData.note" type="textarea" :rows="2" /></el-form-item>
        </el-form>
      </template>

      <!-- 外出活动编辑 -->
      <template v-if="editingKey === 'placeHealth' && editingData">
        <el-form label-width="80px">
          <el-form-item label="地点"><el-input v-model="editingData.location" /></el-form-item>
          <el-form-item label="方式"><el-input v-model="editingData.transportMode" /></el-form-item>
          <el-form-item label="步数"><el-input-number v-model="editingData.steps" :min="0" style="width:100%" /></el-form-item>
        </el-form>
      </template>

      <!-- 交通花费编辑 -->
      <template v-if="editingKey === 'placeFinance' && editingData">
        <el-form label-width="80px">
          <el-form-item label="费用"><el-input-number v-model="editingData.transportCost" :min="0" :precision="2" style="width:100%" /></el-form-item>
          <el-form-item label="方式"><el-input v-model="editingData.transportMode" /></el-form-item>
          <el-form-item label="地点"><el-input v-model="editingData.location" /></el-form-item>
        </el-form>
      </template>

      <!-- 消费记录编辑 -->
      <template v-if="editingKey === 'payments' && editingData">
        <div v-for="(item, idx) in editingData" :key="idx" style="margin-bottom:12px;padding:12px;background:#FFF8E8;border-radius:8px;">
          <el-form label-width="60px" label-position="top">
            <el-form-item label="名称"><el-input v-model="item.name" /></el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="价格"><el-input-number v-model="item.price" :min="0" :precision="2" style="width:100%" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="类型">
                  <el-select v-model="item.type" style="width:100%">
                    <el-option label="未指定" :value="0" />
                    <el-option label="生活/健康" :value="1" />
                    <el-option label="学习/投资" :value="2" />
                    <el-option label="社交/娱乐" :value="3" />
                    <el-option label="责任/应急" :value="4" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-button size="small" type="danger" text @click="editingData.splice(idx, 1)">删除此项</el-button>
          </el-form>
        </div>
        <el-button size="small" @click="editingData.push({ name: '', price: 0, type: 0 })">+ 添加消费</el-button>
      </template>

      <!-- 待办事项编辑 -->
      <template v-if="editingKey === 'memoCommons' && editingData">
        <div v-for="(item, idx) in editingData" :key="idx" style="margin-bottom:12px;padding:12px;background:#FFF8E8;border-radius:8px;">
          <el-form label-width="80px" label-position="top">
            <el-form-item label="内容"><el-input v-model="item.text" /></el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="开始时间">
                  <el-date-picker v-model="item.beginTime" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm" style="width:100%" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="截止时间">
                  <el-date-picker v-model="item.endTime" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm" style="width:100%" placeholder="可选" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-button size="small" type="danger" text @click="editingData.splice(idx, 1)">删除此项</el-button>
          </el-form>
        </div>
        <el-button size="small" @click="editingData.push({ text: '', beginTime: null, endTime: null, status: 0, email: null, remindBeforeHours: 12 })">+ 添加待办</el-button>
      </template>

      <!-- 重要备忘编辑 -->
      <template v-if="editingKey === 'memoImportants' && editingData">
        <div v-for="(item, idx) in editingData" :key="idx" style="margin-bottom:12px;padding:12px;background:#FFF8E8;border-radius:8px;">
          <el-form label-width="60px" label-position="top">
            <el-form-item label="内容"><el-input v-model="item.text" type="textarea" :rows="2" /></el-form-item>
            <el-button size="small" type="danger" text @click="editingData.splice(idx, 1)">删除此项</el-button>
          </el-form>
        </div>
        <el-button size="small" @click="editingData.push({ text: '' })">+ 添加备忘</el-button>
      </template>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.diary-input-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.diary-header {
  text-align: center;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.diary-title {
  margin: 0 0 8px;
  font-size: 32px;
  font-weight: 700;
  color: #C3272B;
  letter-spacing: 4px;
}

.diary-subtitle {
  margin: 0;
  font-size: 14px;
  color: #AAA;
}

.diary-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
}

.diary-textarea {
  flex: 1;
  min-height: 0;

  :deep(.el-textarea__inner) {
    font-size: 15px;
    line-height: 1.8;
    border-color: #FFEFD5;
    border-radius: 12px;
    padding: 16px;
    height: 100%;

    &:focus {
      border-color: #C3272B;
      box-shadow: 0 0 0 2px rgba(195, 39, 43, 0.1);
    }
  }

  :deep(.el-input__count) {
    background: transparent;
    bottom: 8px;
    right: 12px;
    font-size: 12px;
    color: #BBB;
  }
}

.diary-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-shrink: 0;
}

.analyze-btn {
  padding: 12px 40px;
  font-size: 16px;
  border-radius: 10px;
  letter-spacing: 2px;

  :deep(&) {
    --el-button-bg-color: #C3272B;
    --el-button-border-color: #C3272B;
    --el-button-hover-bg-color: #D64A4F;
    --el-button-hover-border-color: #D64A4F;
    --el-button-active-bg-color: #A81F22;
    --el-button-active-border-color: #A81F22;
  }
}

// ====== 审核面板 ======
.review-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.review-header {
  text-align: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.review-title {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
  color: #C3272B;
}

.review-subtitle {
  margin: 0;
  font-size: 13px;
  color: #AAA;
}

.review-cards {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-content: flex-start;
  overflow-y: auto;
  padding: 4px;
  min-height: 0;

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb {
    background: rgba(0,0,0,0.15);
    border-radius: 3px;
  }
}

.review-card {
  flex: 1 1 280px;
  max-width: calc(50% - 6px);
  min-width: 260px;
  background: #FFF;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  overflow: hidden;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.card-icon {
  font-size: 18px;
}

.card-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.card-actions {
  display: flex;
  gap: 4px;
}

.card-body {
  padding: 12px 16px;
}

.field-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  font-size: 13px;
}

.field-label {
  color: #888;
  flex-shrink: 0;
}

.field-value {
  color: #333;
  text-align: right;
  word-break: break-all;
  max-width: 60%;
}

.payment-item, .memo-item {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px dashed #f0f0f0;
  font-size: 13px;

  &:last-child { border-bottom: none; }
}

.payment-name, .memo-text { color: #333; font-weight: 500; }
.payment-price { color: #C3272B; }
.payment-type, .memo-time { color: #888; font-size: 12px; }

// ====== 底部按钮 ======
.review-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 16px;
  flex-shrink: 0;
  border-top: 1px solid rgba(0,0,0,0.06);
  margin-top: 12px;
}

.confirm-btn {
  padding: 10px 32px;
  font-size: 15px;
  border-radius: 10px;
  letter-spacing: 1px;

  :deep(&) {
    --el-button-bg-color: #C3272B;
    --el-button-border-color: #C3272B;
    --el-button-hover-bg-color: #D64A4F;
    --el-button-hover-border-color: #D64A4F;
    --el-button-active-bg-color: #A81F22;
    --el-button-active-border-color: #A81F22;
  }
}

// ====== 提交中状态 ======
.submitting-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.submitting-spinner {
  text-align: center;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #FFEFD5;
  border-top-color: #C3272B;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.submitting-text {
  color: #888;
  font-size: 15px;
}

// ====== 完成状态 ======
.done-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.done-icon {
  font-size: 64px;
  animation: bounceIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes bounceIn {
  from { transform: scale(0); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.done-title {
  margin: 0;
  font-size: 28px;
  color: #C3272B;
}

.done-text {
  margin: 0;
  color: #888;
  font-size: 15px;
}

// 卡片主题色
.card-food    { border-left: 4px solid #FF8C42; }
.card-finance { border-left: 4px solid #F5A623; }
.card-sleep   { border-left: 4px solid #7B68EE; }
.card-mood    { border-left: 4px solid #FF6B9D; }
.card-place   { border-left: 4px solid #4ECDC4; }
.card-place-finance { border-left: 4px solid #45B7D1; }
.card-payment { border-left: 4px solid #96CEB4; }
.card-memo    { border-left: 4px solid #FFD93D; }
.card-memo-important { border-left: 4px solid #C3272B; }
</style>

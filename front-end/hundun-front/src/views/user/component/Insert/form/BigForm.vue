<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useMemoStore } from '@/stores/memo'
import { insertFoodAPI, updateFoodAPI, insertFoodMoneyAPI, updateFoodMoneyAPI } from '@/api/food.js'
import { insertSleepAPI, updateSleepAPI } from '@/api/sleep.js'
import { insertMoodAPI, updateMoodAPI } from '@/api/mood.js'
import { insertPlaceHealthAPI, updatePlaceHealthAPI, insertPlaceFinanceAPI, updatePlaceFinanceAPI } from '@/api/place.js'
import { savePaymentAPI } from '@/api/payment.js'

const userStore = useUserStore()
const memoStore = useMemoStore()

const MODULES_ENABLED = computed(() => {
  const s = userStore.userModelStatus
  return s && s.health === 1 && s.finance === 1
})

const HEALTH_ENABLED = computed(() => {
  const s = userStore.userModelStatus
  return s && s.health === 1
})

// ===== 饮食记录 =====
const foodForm = reactive({
  morningFood: '',
  afternoonFood: '',
  eveningFood: '',
  elseFood: ''
})
const savingFood = ref(false)

const saveFood = async () => {
  savingFood.value = true
  const dto = {
    morningFood: foodForm.morningFood || null,
    afternoonFood: foodForm.afternoonFood || null,
    eveningFood: foodForm.eveningFood || null,
    elseFood: foodForm.elseFood || null
  }
  try {
    const res = await insertFoodAPI(dto)
    if (res.data === 0) {
      await ElMessageBox.confirm('您今天似乎已经记录过饮食了呢，继续将覆盖先前的记录，确定吗？', '提示', { confirmButtonText: '覆盖', cancelButtonText: '取消', type: 'warning' })
      await updateFoodAPI(dto)
    }
    ElMessage.success('饮食记录已保存 (∠・ω< )⌒★')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('保存失败')
  } finally {
    savingFood.value = false
  }
}

// ===== 餐饮花费 =====
const financeForm = reactive({
  morningFoodPrice: '',
  afternoonFoodPrice: '',
  eveningFoodPrice: '',
  elseFoodPrice: ''
})
const savingFinance = ref(false)

const saveFinance = async () => {
  savingFinance.value = true
  const dto = {
    morningFoodPrice: financeForm.morningFoodPrice || null,
    afternoonFoodPrice: financeForm.afternoonFoodPrice || null,
    eveningFoodPrice: financeForm.eveningFoodPrice || null,
    elseFoodPrice: financeForm.elseFoodPrice || null
  }
  try {
    const res = await insertFoodMoneyAPI(dto)
    if (res.data === 0) {
      await ElMessageBox.confirm('您今天似乎已经记录过花费了呢，继续将覆盖先前的记录，确定吗？', '提示', { confirmButtonText: '覆盖', cancelButtonText: '取消', type: 'warning' })
      await updateFoodMoneyAPI(dto)
    }
    ElMessage.success('餐饮花费已保存 (∠・ω< )⌒★')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('保存失败')
  } finally {
    savingFinance.value = false
  }
}

// ===== 睡眠记录 =====
const sleepForm = reactive({
  quality: null,
  sleepHours: 0,
  sleepMinutes: 0
})
const savingSleep = ref(false)

const sleepDurationMin = computed(() => (Number(sleepForm.sleepHours) || 0) * 60 + (Number(sleepForm.sleepMinutes) || 0))

const saveSleep = async () => {
  if (sleepForm.quality === null) { ElMessage.error('请选择睡眠质量'); return }
  savingSleep.value = true
  const dto = { quality: sleepForm.quality, durationMin: sleepDurationMin.value }
  try {
    const res = await insertSleepAPI(dto)
    if (res.data === 0) {
      await ElMessageBox.confirm('今天已经记过睡眠了，继续将覆盖先前的记录，确定吗？', '提示', { confirmButtonText: '覆盖', cancelButtonText: '取消', type: 'warning' })
      await updateSleepAPI(dto)
    }
    ElMessage.success('睡眠记录已保存 (´∀｀)♡')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('保存失败')
  } finally {
    savingSleep.value = false
  }
}

// ===== 心情记录 =====
const moodForm = reactive({
  mood: null,
  note: ''
})
const savingMood = ref(false)

const saveMood = async () => {
  if (moodForm.mood === null) { ElMessage.error('请选择心情等级'); return }
  savingMood.value = true
  const dto = { mood: moodForm.mood, note: moodForm.note || null }
  try {
    const res = await insertMoodAPI(dto)
    if (res.data === 0) {
      await ElMessageBox.confirm('今天已经记过心情了，继续将覆盖先前的记录，确定吗？', '提示', { confirmButtonText: '覆盖', cancelButtonText: '取消', type: 'warning' })
      await updateMoodAPI(dto)
    }
    ElMessage.success('心情记录已保存 (´∀｀)♡')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('保存失败')
  } finally {
    savingMood.value = false
  }
}

// ===== 外出活动 =====
const placeForm = reactive({
  location: '',
  transportMode: null,
  steps: 0,
  transportCost: ''
})
const savingPlace = ref(false)

const hasTransportCost = computed(() => placeForm.transportCost !== null && placeForm.transportCost !== '')

const savePlace = async () => {
  if (!placeForm.location.trim()) { ElMessage.error('请填写地点'); return }
  savingPlace.value = true
  const healthDto = { location: placeForm.location.trim(), transportMode: placeForm.transportMode, steps: Number(placeForm.steps) || 0 }
  try {
    const healthRes = await insertPlaceHealthAPI(healthDto)
    let healthExisted = healthRes.data === 0
    let financeExisted = false
    if (hasTransportCost.value) {
      const finDto = { transportCost: Number(placeForm.transportCost), transportMode: placeForm.transportMode, location: placeForm.location.trim() }
      const finRes = await insertPlaceFinanceAPI(finDto)
      financeExisted = finRes.data === 0
      if (financeExisted) await updatePlaceFinanceAPI(finDto)
    }
    if (healthExisted) await updatePlaceHealthAPI(healthDto)
    ElMessage.success('外出记录已保存 (´∀｀)♡')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    savingPlace.value = false
  }
}

// ===== 消费记录 =====
const paymentList = reactive([{ id: Date.now(), productName: '', productPrice: '', paymentType: 0 }])
const savingPayment = ref(false)

const addPayment = () => paymentList.push({ id: Date.now(), productName: '', productPrice: '', paymentType: 0 })
const removePayment = (idx) => {
  if (paymentList.length <= 1) { ElMessage.warning('至少保留一组数据哦'); return }
  paymentList.splice(idx, 1)
}

const savePayment = async () => {
  for (const item of paymentList) {
    if (!item.productName.trim()) { ElMessage.error('请填写商品名称'); return }
    if (!item.productPrice) { ElMessage.error('请填写商品价格'); return }
  }
  savingPayment.value = true
  const dto = paymentList.map(i => ({ name: i.productName.trim(), price: Math.round(Number(i.productPrice)) || 0, type: i.paymentType }))
  try {
    await savePaymentAPI(dto)
    ElMessage.success('消费记录已保存 ✧(≖ ◡ ≖✿)')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    savingPayment.value = false
  }
}

// ===== 待办事项 =====
const memoForm = reactive({ beginTime: '', endTime: '', text: '', status: 0, remindBeforeHours: 12 })
const savingMemo = ref(false)

const saveMemo = async () => {
  if (!memoForm.text.trim()) { ElMessage.error('请填写备忘内容'); return }
  savingMemo.value = true
  try {
    await memoStore.addCommonMemo({
      beginTime: memoForm.beginTime,
      endTime: memoForm.endTime,
      text: memoForm.text.trim(),
      status: memoForm.status,
      remindBeforeHours: Number(memoForm.remindBeforeHours)
    })
    ElMessage.success('待办已保存 ✧(≖ ◡ ≖✿)')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    savingMemo.value = false
  }
}

// ===== 重要备忘 =====
const importantForm = reactive({ text: '' })
const savingImportant = ref(false)

const saveImportant = async () => {
  if (!importantForm.text.trim()) { ElMessage.error('请填写备忘内容'); return }
  savingImportant.value = true
  try {
    await memoStore.addImportantMemo({ text: importantForm.text.trim() })
    ElMessage.success('重要备忘已保存 ✧(≖ ◡ ≖✿)')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    savingImportant.value = false
  }
}

const SLEEP_QUALITY = [
  { value: 0, label: '差', icon: '😫' },
  { value: 1, label: '一般', icon: '😐' },
  { value: 2, label: '好', icon: '😴' }
]

const MOOD_OPTIONS = [
  { value: 0, label: '很差', icon: '😫' },
  { value: 1, label: '差', icon: '😞' },
  { value: 2, label: '一般', icon: '😐' },
  { value: 3, label: '好', icon: '😊' },
  { value: 4, label: '很好', icon: '🥰' }
]

const TRANSPORT_OPTIONS = [
  { value: '步行', icon: '🚶' },
  { value: '自行车', icon: '🚲' },
  { value: '公交', icon: '🚌' },
  { value: '地铁', icon: '🚇' },
  { value: '私家车', icon: '🚗' },
  { value: '其他', icon: '🛴' }
]

const PAYMENT_TYPES = [
  { value: 0, label: '未指定' },
  { value: 1, label: '生活/健康' },
  { value: 2, label: '学习/投资' },
  { value: 3, label: '社交/娱乐' },
  { value: 4, label: '责任/应急' }
]
</script>

<template>
  <div class="big-form">
    <h2 class="big-form-title">传统表单模式</h2>
    <p class="big-form-subtitle">一次性填写所有记录 (｡•̀ᴗ-)✧</p>

    <!-- 饮食记录 -->
    <section class="form-section" v-if="MODULES_ENABLED">
      <h3 class="section-title">
        <span>🍜&nbsp;饮食记录</span>
      </h3>
      <div class="section-grid grid-2">
        <div class="field-group">
          <label>🌅 早餐</label>
          <el-input v-model="foodForm.morningFood" placeholder="吃了什么？" />
        </div>
        <div class="field-group">
          <label>☀️ 午餐</label>
          <el-input v-model="foodForm.afternoonFood" placeholder="吃了什么？" />
        </div>
        <div class="field-group">
          <label>🌙 晚餐</label>
          <el-input v-model="foodForm.eveningFood" placeholder="吃了什么？" />
        </div>
        <div class="field-group">
          <label>✨ 其他</label>
          <el-input v-model="foodForm.elseFood" placeholder="有没有吃夜宵？" />
        </div>
      </div>
      <el-button type="danger" :loading="savingFood" @click="saveFood">保存饮食</el-button>
    </section>

    <!-- 餐饮花费 -->
    <section class="form-section" v-if="MODULES_ENABLED">
      <h3 class="section-title">
        <span>💰&nbsp;餐饮花费</span>
      </h3>
      <div class="section-grid grid-2">
        <div class="field-group">
          <label>🌅 早餐花费</label>
          <el-input v-model="financeForm.morningFoodPrice" placeholder="金额（元）" />
        </div>
        <div class="field-group">
          <label>☀️ 午餐花费</label>
          <el-input v-model="financeForm.afternoonFoodPrice" placeholder="金额（元）" />
        </div>
        <div class="field-group">
          <label>🌙 晚餐花费</label>
          <el-input v-model="financeForm.eveningFoodPrice" placeholder="金额（元）" />
        </div>
        <div class="field-group">
          <label>✨ 其他花费</label>
          <el-input v-model="financeForm.elseFoodPrice" placeholder="金额（元）" />
        </div>
      </div>
      <el-button type="danger" :loading="savingFinance" @click="saveFinance">保存花费</el-button>
    </section>

    <!-- 睡眠记录 -->
    <section class="form-section" v-if="HEALTH_ENABLED">
      <h3 class="section-title">
        <span>😴&nbsp;睡眠记录</span>
      </h3>
      <div class="section-grid grid-2">
        <div class="field-group">
          <label>🌙 睡眠质量</label>
          <div class="option-group">
            <button
              v-for="opt in SLEEP_QUALITY"
              :key="opt.value"
              type="button"
              class="option-btn"
              :class="{ active: sleepForm.quality === opt.value }"
              @click="sleepForm.quality = opt.value"
            >
              <span>{{ opt.icon }}</span>&nbsp;{{ opt.label }}
            </button>
          </div>
        </div>
        <div class="field-group">
          <label>⏱ 睡眠时长</label>
          <div class="duration-row">
            <el-input-number v-model="sleepForm.sleepHours" :min="0" :max="24" controls-position="right" />
            <span class="unit">小时</span>
            <el-input-number v-model="sleepForm.sleepMinutes" :min="0" :max="59" controls-position="right" />
            <span class="unit">分钟</span>
          </div>
          <span class="hint">合计 {{ sleepDurationMin }} 分钟</span>
        </div>
      </div>
      <el-button type="danger" :loading="savingSleep" @click="saveSleep">保存睡眠</el-button>
    </section>

    <!-- 心情记录 -->
    <section class="form-section" v-if="HEALTH_ENABLED">
      <h3 class="section-title">
        <span>💭&nbsp;心情记录</span>
      </h3>
      <div class="section-grid">
        <div class="field-group">
          <label>💭 今天心情</label>
          <div class="option-group mood-options">
            <button
              v-for="opt in MOOD_OPTIONS"
              :key="opt.value"
              type="button"
              class="option-btn"
              :class="{ active: moodForm.mood === opt.value }"
              @click="moodForm.mood = opt.value"
            >
              <span>{{ opt.icon }}</span>&nbsp;{{ opt.label }}
            </button>
          </div>
        </div>
        <div class="field-group">
          <label>✏️ 随笔</label>
          <el-input v-model="moodForm.note" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="写写今天的随笔吧…" />
        </div>
      </div>
      <el-button type="danger" :loading="savingMood" @click="saveMood">保存心情</el-button>
    </section>

    <!-- 外出活动 -->
    <section class="form-section" v-if="MODULES_ENABLED">
      <h3 class="section-title">
        <span>📍&nbsp;外出活动</span>
      </h3>
      <div class="section-grid grid-2">
        <div class="field-group">
          <label>📍 地点</label>
          <el-input v-model="placeForm.location" placeholder="今天去了哪里…" />
        </div>
        <div class="field-group">
          <label>🚗 交通方式</label>
          <div class="option-group">
            <button
              v-for="opt in TRANSPORT_OPTIONS"
              :key="opt.value"
              type="button"
              class="option-btn"
              :class="{ active: placeForm.transportMode === opt.value }"
              @click="placeForm.transportMode = opt.value"
            >
              <span>{{ opt.icon }}</span>&nbsp;{{ opt.value }}
            </button>
          </div>
        </div>
        <div class="field-group">
          <label>👣 步数</label>
          <el-input-number v-model="placeForm.steps" :min="0" :max="100000" controls-position="right" />
        </div>
        <div class="field-group">
          <label>🚇 交通花费（元）</label>
          <el-input v-model="placeForm.transportCost" placeholder="optional" />
        </div>
      </div>
      <el-button type="danger" :loading="savingPlace" @click="savePlace">保存外出</el-button>
    </section>

    <!-- 消费记录 -->
    <section class="form-section">
      <h3 class="section-title">
        <span>🛒&nbsp;消费记录</span>
      </h3>
      <div v-for="(item, idx) in paymentList" :key="item.id" class="payment-row">
        <div class="payment-fields">
          <el-input v-model="item.productName" placeholder="买了啥？" />
          <el-input v-model="item.productPrice" type="number" placeholder="价格" step="0.01" min="0" />
          <el-select v-model="item.paymentType" placeholder="类型">
            <el-option v-for="opt in PAYMENT_TYPES" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </div>
        <div class="payment-actions">
          <el-button type="primary" circle size="small" @click="addPayment">+</el-button>
          <el-button type="danger" circle size="small" @click="removePayment(idx)" :disabled="paymentList.length <= 1">-</el-button>
        </div>
      </div>
      <el-button type="danger" :loading="savingPayment" @click="savePayment">保存消费</el-button>
    </section>

    <!-- 待办事项 -->
    <section class="form-section">
      <h3 class="section-title">
        <span>📝&nbsp;待办事项</span>
      </h3>
      <div class="section-grid grid-2">
        <div class="field-group">
          <label>⏰ 开始时间</label>
          <el-date-picker v-model="memoForm.beginTime" type="datetime" placeholder="选择开始时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm" style="width:100%" />
        </div>
        <div class="field-group">
          <label>⏰ 结束时间</label>
          <el-date-picker v-model="memoForm.endTime" type="datetime" placeholder="选择结束时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm" style="width:100%" />
        </div>
        <div class="field-group full-width">
          <label>📝 备忘内容</label>
          <el-input v-model="memoForm.text" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="需要做什么？" />
        </div>
        <div class="field-group">
          <label>📧 邮件通知</label>
          <el-switch v-model="memoForm.status" :active-value="1" :inactive-value="0" active-color="#C3272B" />
        </div>
        <div v-if="memoForm.status === 1" class="field-group">
          <label>⏳ 提前提醒（小时）</label>
          <el-input-number v-model="memoForm.remindBeforeHours" :min="0" :max="720" controls-position="right" />
        </div>
      </div>
      <el-button type="danger" :loading="savingMemo" @click="saveMemo">保存待办</el-button>
    </section>

    <!-- 重要备忘 -->
    <section class="form-section">
      <h3 class="section-title">
        <span>★&nbsp;重要备忘</span>
      </h3>
      <div class="section-grid">
        <div class="field-group">
          <label>★ 记录内容</label>
          <el-input v-model="importantForm.text" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="写下需要长期保留的重要事项..." />
        </div>
      </div>
      <el-button type="danger" :loading="savingImportant" @click="saveImportant">保存备忘</el-button>
    </section>
  </div>
</template>

<style scoped lang="less">
.big-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.big-form-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #C3272B;
  text-align: center;
  letter-spacing: 4px;
}

.big-form-subtitle {
  margin: -12px 0 0;
  font-size: 14px;
  color: #AAA;
  text-align: center;
}

.form-section {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  .section-title {
    margin: 0 0 16px;
    font-size: 17px;
    font-weight: 700;
    color: #5A4A42;
    padding-bottom: 10px;
    border-bottom: 2px solid #FFF8E8;
  }

  .section-grid {
    display: flex;
    flex-direction: column;
    gap: 14px;
    margin-bottom: 16px;

    &.grid-2 {
      display: grid;
      grid-template-columns: 1fr 1fr;
    }
  }

  .field-group {
    display: flex;
    flex-direction: column;
    gap: 6px;

    &.full-width {
      grid-column: 1 / -1;
    }

    label {
      font-size: 13px;
      font-weight: 600;
      color: #5A4A42;
    }
  }

  .option-group {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  .mood-options {
    .option-btn {
      flex: 1;
      min-width: 60px;
    }
  }

  .option-btn {
    display: flex;
    align-items: center;
    gap: 2px;
    padding: 8px 14px;
    border: 2px solid #FFEFD5;
    border-radius: 8px;
    background: #FFF;
    font-size: 13px;
    color: #666;
    cursor: pointer;
    transition: all 0.15s;

    &.active {
      border-color: rgba(195, 39, 43, 0.35);
      background: #FFEFD5;
      color: #333;
      font-weight: 600;
    }

    &:hover {
      border-color: #C3272B;
    }
  }

  .duration-row {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .unit {
    font-size: 13px;
    color: #666;
  }

  .hint {
    font-size: 12px;
    color: #999;
  }

  :deep(.el-button--danger) {
    background: #C3272B;
    border-color: #C3272B;
  }
}

.payment-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;

  .payment-fields {
    flex: 1;
    display: flex;
    gap: 10px;

    :deep(.el-input),
    :deep(.el-select) {
      flex: 1;
    }
  }

  .payment-actions {
    display: flex;
    gap: 4px;
    padding-top: 2px;
  }
}
</style>

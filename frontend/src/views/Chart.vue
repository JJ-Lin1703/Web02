<template>
  <div class="page-container">
    <div class="carousel-wrapper">
      <button class="nav-btn prev-btn" @click="prevChart" :disabled="currentIndex === 0">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M15 19l-7-7 7-7" />
        </svg>
      </button>
      
      <div class="chart-carousel">
        <!-- 体重变化曲线 -->
        <div class="chart-slide" :class="{ active: currentIndex === 0 }">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>体重变化曲线</span>
                <div class="filter-bar">
                  <el-button-group>
                    <el-button 
                      :type="filterType === '7days' ? 'primary' : ''" 
                      @click="setFilter('7days')"
                    >近7天</el-button>
                    <el-button 
                      :type="filterType === '30days' ? 'primary' : ''" 
                      @click="setFilter('30days')"
                    >近30天</el-button>
                    <el-button 
                      :type="filterType === 'all' ? 'primary' : ''" 
                      @click="setFilter('all')"
                    >全部</el-button>
                  </el-button-group>
                </div>
              </div>
            </template>
            <div v-show="!weightEmpty" ref="weightChartRef" class="chart-container" v-loading="weightLoading"></div>
            <EmptyState v-if="weightEmpty" description="暂无体重数据" />
          </el-card>
        </div>
        
        <!-- 周打卡完成率 -->
        <div class="chart-slide" :class="{ active: currentIndex === 1 }">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>周打卡完成率</span>
              </div>
            </template>
            <div v-show="!completionEmpty" ref="completionChartRef" class="chart-container" v-loading="completionLoading"></div>
            <EmptyState v-if="completionEmpty" description="暂无打卡数据" />
          </el-card>
        </div>
        
        <!-- 热量摄入参考 -->
        <div class="chart-slide" :class="{ active: currentIndex === 2 }">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>热量摄入参考</span>
              </div>
            </template>
            <div ref="calorieChartRef" class="chart-container" v-loading="calorieLoading"></div>
          </el-card>
        </div>
      </div>
      
      <button class="nav-btn next-btn" @click="nextChart" :disabled="currentIndex === 2">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 5l7 7-7 7" />
        </svg>
      </button>
    </div>
    
    <div class="indicator-dots">
      <span 
        class="dot"
        :class="{ active: currentIndex === 0 }"
        @click="goToChart(0)"
      ></span>
      <span 
        class="dot"
        :class="{ active: currentIndex === 1 }"
        @click="goToChart(1)"
      ></span>
      <span 
        class="dot"
        :class="{ active: currentIndex === 2 }"
        @click="goToChart(2)"
      ></span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { getWeightHistory, getWeeklyStats } from '@/api/user'
import EmptyState from '@/components/EmptyState.vue'

const currentIndex = ref(0)
const filterType = ref('7days')

// 直接定义ref
const weightChartRef = ref(null)
const completionChartRef = ref(null)
const calorieChartRef = ref(null)

const weightLoading = ref(false)
const completionLoading = ref(false)
const calorieLoading = ref(false)

const weightEmpty = ref(false)
const completionEmpty = ref(false)
const calorieEmpty = ref(false)

let weightChartInstance = null
let completionChartInstance = null
let calorieChartInstance = null

const prevChart = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

const nextChart = () => {
  if (currentIndex.value < 2) {
    currentIndex.value++
  }
}

const goToChart = (index) => {
  currentIndex.value = index
}

const setFilter = (type) => {
  filterType.value = type
  fetchWeightData()
}

const getDateRange = () => {
  const now = new Date()
  let startDate = null
  switch (filterType.value) {
    case '7days':
      startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      break
    case '30days':
      startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
      break
  }
  return {
    startDate: startDate ? startDate.toISOString().split('T')[0] : null
  }
}

const fetchWeightData = async () => {
  weightLoading.value = true
  weightEmpty.value = false
  try {
    const { startDate } = getDateRange()
    const params = { sortBy: 'recordDate' }
    if (startDate) params.startDate = startDate
    
    const res = await getWeightHistory(params)
    const data = res.data?.records || []
    
    await nextTick()
    
    if (data.length === 0) {
      disposeChart(weightChartInstance)
      weightChartInstance = null
      weightEmpty.value = true
    } else {
      if (weightChartInstance) {
        weightChartInstance.dispose()
        weightChartInstance = null
      }
      renderWeightChart(data)
    }
  } catch {
    disposeChart(weightChartInstance)
    weightChartInstance = null
    weightEmpty.value = true
  } finally {
    weightLoading.value = false
  }
}

const fetchCompletionData = async () => {
  completionLoading.value = true
  completionEmpty.value = false
  try {
    const res = await getWeeklyStats()
    const data = res.data || {}
    const weekRecords = data.records || []
    
    await nextTick()
    
    if (weekRecords.length === 0) {
      disposeChart(completionChartInstance)
      completionChartInstance = null
      completionEmpty.value = true
    } else {
      if (completionChartInstance) {
        completionChartInstance.dispose()
        completionChartInstance = null
      }
      renderCompletionChart(weekRecords)
    }
  } catch {
    disposeChart(completionChartInstance)
    completionChartInstance = null
    completionEmpty.value = true
  } finally {
    completionLoading.value = false
  }
}

const fetchCalorieData = async () => {
  calorieLoading.value = true
  try {
    const mockData = { staple: 45, protein: 25, vegetable: 30 }
    
    await nextTick()
    
    if (calorieChartInstance) {
      calorieChartInstance.dispose()
      calorieChartInstance = null
    }
    renderCalorieChart(mockData)
  } catch {
    calorieChartInstance = renderEmptyChart(calorieChartRef.value, '暂无热量数据', calorieChartInstance)
  } finally {
    calorieLoading.value = false
  }
}

const renderWeightChart = (data) => {
  if (!weightChartRef.value) return
  
  const dates = data.map(item => {
    const date = new Date(item.recordDate)
    return `${date.getMonth() + 1}/${date.getDate()}`
  })
  const weights = data.map(item => item.weight)

  if (!weightChartInstance) {
    weightChartInstance = echarts.init(weightChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const { dataIndex, value } = params[0]
        return `${dates[dataIndex]}<br/>体重: ${value} kg`
      }
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { rotate: 45 }
    },
    yAxis: {
      type: 'value',
      name: '体重 (kg)',
      min: (value) => Math.floor(value.min - 2),
      max: (value) => Math.ceil(value.max + 2)
    },
    series: [{
      name: '体重',
      type: 'line',
      data: weights,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { width: 3, color: '#409eff' },
      itemStyle: { color: '#409eff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ])
      }
    }],
    grid: { left: '8%', right: '8%', bottom: '15%', top: '10%' }
  }

  weightChartInstance.setOption(option)
}

const renderCompletionChart = (weekRecords) => {
  if (!completionChartRef.value) return
  
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const completionRates = [0, 0, 0, 0, 0, 0, 0]
  
  weekRecords.forEach(record => {
    if (record.recordDate) {
      const recordDate = new Date(record.recordDate)
      const recordDayIndex = recordDate.getDay() === 0 ? 6 : recordDate.getDay() - 1
      completionRates[recordDayIndex] = parseFloat(record.finishRate) || 0
    }
  })

  if (!completionChartInstance) {
    completionChartInstance = echarts.init(completionChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const { dataIndex, value } = params[0]
        return `${days[dataIndex]}<br/>完成率: ${value}%`
      }
    },
    xAxis: { type: 'category', data: days },
    yAxis: { type: 'value', name: '完成率 (%)', min: 0, max: 100 },
    series: [{
      name: '完成率',
      type: 'bar',
      data: completionRates,
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#67c23a' },
          { offset: 1, color: '#85ce61' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }],
    grid: { left: '8%', right: '8%', bottom: '10%', top: '10%' }
  }

  completionChartInstance.setOption(option)
}

const renderCalorieChart = (data) => {
  if (!calorieChartRef.value) return
  
  const categories = ['主食', '蛋白质', '蔬果']
  const values = [data.staple, data.protein, data.vegetable]
  const colors = ['#f5a623', '#409eff', '#67c23a']

  if (!calorieChartInstance) {
    calorieChartInstance = echarts.init(calorieChartRef.value)
  }

  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c}% ({d}%)' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      name: '热量摄入',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}: {c}%' },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
      },
      data: categories.map((name, index) => ({
        value: values[index],
        name: name,
        itemStyle: { color: colors[index] }
      }))
    }]
  }

  calorieChartInstance.setOption(option)
}

const disposeChart = (chartInstance) => {
  if (chartInstance) {
    chartInstance.dispose()
  }
}

const handleResize = () => {
  if (weightChartInstance) weightChartInstance.resize()
  if (completionChartInstance) completionChartInstance.resize()
  if (calorieChartInstance) calorieChartInstance.resize()
}

const initVisibleChart = async () => {
  await nextTick()
  switch (currentIndex.value) {
    case 0:
      fetchWeightData()
      break
    case 1:
      fetchCompletionData()
      break
    case 2:
      fetchCalorieData()
      break
  }
}

watch(currentIndex, async () => {
  await nextTick()
  handleResize()
  initVisibleChart()
})

onMounted(async () => {
  await nextTick()
  initVisibleChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (weightChartInstance) weightChartInstance.dispose()
  if (completionChartInstance) completionChartInstance.dispose()
  if (calorieChartInstance) calorieChartInstance.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.carousel-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  gap: 20px;
}

.nav-btn {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: none;
  background: #fff;
  color: #666;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
  z-index: 10;
}

.nav-btn:hover:not(:disabled) {
  background: #409eff;
  color: #fff;
}

.nav-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.chart-carousel {
  flex: 1;
  height: 100%;
  overflow: hidden;
}

.chart-slide {
  height: 100%;
  display: none;
}

.chart-slide.active {
  display: block;
}

.chart-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-bar {
  display: flex;
  align-items: center;
}

.chart-container {
  flex: 1;
  width: 100%;
  min-height: 400px;
}

.indicator-dots {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #d9d9d9;
  cursor: pointer;
  transition: all 0.3s ease;
}

.dot.active {
  width: 24px;
  border-radius: 5px;
  background: #409eff;
}
</style>
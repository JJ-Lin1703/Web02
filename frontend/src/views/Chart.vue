<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>体重趋势图</span>
          <div class="filter-bar">
            <el-date-picker
              v-model="startDate"
              type="date"
              placeholder="开始日期"
              value-format="YYYY-MM-DD"
              style="width: 150px"
            />
            <span style="margin: 0 8px">至</span>
            <el-date-picker
              v-model="endDate"
              type="date"
              placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 150px"
            />
            <el-button type="primary" @click="fetchData" style="margin-left: 12px">查询</el-button>
          </div>
        </div>
      </template>
      <div ref="chartRef" class="chart-container" v-loading="loading"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getWeightHistory } from '@/api/user'

const chartRef = ref(null)
const loading = ref(false)
const startDate = ref(null)
const endDate = ref(null)
let chartInstance = null

const fetchData = async () => {
  loading.value = true
  try {
    const params = {}
    if (startDate.value) params.startDate = startDate.value
    if (endDate.value) params.endDate = endDate.value
    params.sortBy = 'recordDate'
    
    const res = await getWeightHistory(params)
    const data = res.data || []
    
    if (data.length === 0) {
      renderEmptyChart()
    } else {
      renderChart(data)
    }
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

const renderChart = (data) => {
  if (!chartRef.value) return
  
  const dates = data.map(item => {
    const date = new Date(item.recordDate)
    return `${date.getMonth() + 1}/${date.getDate()}`
  })
  const weights = data.map(item => item.weight)

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
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
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '体重 (kg)',
      min: (value) => Math.floor(value.min - 2),
      max: (value) => Math.ceil(value.max + 2)
    },
    series: [
      {
        name: '体重',
        type: 'line',
        data: weights,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#409eff'
        },
        itemStyle: {
          color: '#409eff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      }
    ],
    grid: {
      left: '10%',
      right: '10%',
      bottom: '15%',
      top: '10%'
    }
  }

  chartInstance.setOption(option)
}

const renderEmptyChart = () => {
  if (!chartRef.value) return
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  chartInstance.setOption({
    title: {
      text: '暂无体重数据',
      left: 'center',
      top: 'center',
      textStyle: {
        color: '#909399',
        fontSize: 14
      }
    },
    xAxis: { show: false },
    yAxis: { show: false }
  })
}

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(async () => {
  await nextTick()
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-bar {
  display: flex;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 400px;
}
</style>

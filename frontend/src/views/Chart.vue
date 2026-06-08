<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>体重变化趋势</span>
          <div class="filter-bar">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 260px"
            />
            <el-button type="primary" @click="fetchChartData" style="margin-left: 12px">查询</el-button>
          </div>
        </div>
      </template>
      <div ref="chartRef" class="chart-wrapper" v-loading="loading"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getWeightHistory } from '@/api/user'

const chartRef = ref(null)
const loading = ref(false)
const dateRange = ref(null)
let chartInstance = null

const fetchChartData = async () => {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    params.sortBy = 'recordDate'
    const res = await getWeightHistory(params)
    const data = res.data || []
    if (data.length === 0) {
      renderEmptyChart()
    } else {
      renderChart(data)
    }
  } finally {
    loading.value = false
  }
}

const renderChart = (data) => {
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  if (data.length === 0) {
    renderEmptyChart()
    return
  }

  const dates = data.map(item => item.recordDate).reverse()
  const weights = data.map(item => item.weight).reverse()

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>体重：${p.value} kg`
      }
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45,
        formatter: (value) => value?.slice(0, 10) || value
      }
    },
    yAxis: {
      type: 'value',
      name: '体重 (kg)',
      min: (value) => Math.floor(value.min - 2),
      max: (value) => Math.ceil(value.max + 2)
    },
    grid: {
      left: 60,
      right: 30,
      bottom: 80,
      top: 30
    },
    series: [
      {
        name: '体重',
        type: 'line',
        data: weights,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { color: '#409eff', width: 2 },
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        markLine: {
          silent: true,
          lineStyle: { type: 'dashed', color: '#909399' },
          label: { formatter: '平均 {c} kg' },
          data: [
            {
              type: 'average',
              name: '平均值'
            }
          ]
        }
      }
    ]
  })
}

const renderEmptyChart = () => {
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption({
    title: {
      text: '暂无体重记录',
      left: 'center',
      top: 'center',
      textStyle: { color: '#909399', fontSize: 16 }
    },
    xAxis: { show: false },
    yAxis: { show: false },
    series: []
  })
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  fetchChartData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
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
  flex-wrap: wrap;
  gap: 8px;
}

.filter-bar {
  display: flex;
  align-items: center;
}

.chart-wrapper {
  width: 100%;
  height: 450px;
}
</style>
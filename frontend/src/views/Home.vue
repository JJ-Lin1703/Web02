<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6" v-if="healthRecord">
        <div class="metric-card" :class="getBmiClass(parseFloat(healthRecord.bmi))">
          <div class="metric-icon">
            <el-icon :size="28" color="#fff"><ScaleToOriginal /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-label">身体质量指数 (BMI)</div>
            <div class="metric-value">{{ healthRecord.bmi }}</div>
            <div class="metric-desc">{{ getBmiDescription(parseFloat(healthRecord.bmi)) }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6" v-if="healthRecord">
        <div class="metric-card metric-card-bmr">
          <div class="metric-icon">
            <el-icon :size="28" color="#fff"><Lightning /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-label">基础代谢率 (BMR)</div>
            <div class="metric-value">{{ healthRecord.bmr }}<span class="metric-unit">kcal</span></div>
            <div class="metric-desc">每日基础消耗热量</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6" v-if="healthRecord">
        <div class="metric-card metric-card-tdee">
          <div class="metric-icon">
            <el-icon :size="28" color="#fff"><DataAnalysis /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-label">每日总能量消耗 (TDEE)</div>
            <div class="metric-value">{{ healthRecord.tdee }}<span class="metric-unit">kcal</span></div>
            <div class="metric-desc">每日建议摄入热量</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6" v-if="dailyStats">
        <div class="metric-card metric-card-completion">
          <div class="metric-icon">
            <el-icon :size="28" color="#fff"><TrendCharts /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-label">今日完成率</div>
            <div class="metric-value">{{ dailyStats.todayCompletionRate }}%</div>
            <div class="metric-desc">本周累计 {{ dailyStats.weekCompleted }}/{{ dailyStats.weekTotal }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="14">
        <el-card class="checkin-section">
          <template #header>
            <div class="section-header">
              <span class="section-title">今日打卡</span>
              <span class="section-date">{{ todayDate }}</span>
            </div>
          </template>
          
          <div class="checkin-content">
            <div v-if="loadingTasks" class="loading-container">
              <el-icon class="is-loading" :size="48"><Loading /></el-icon>
            </div>
            
            <div v-else-if="!dailyTasks.length" class="empty-container">
              <el-empty description="暂无打卡任务，请先生成AI计划" :image-size="60" />
            </div>
            
            <div v-else class="tasks-container">
            <div class="task-category">
              <div class="category-header">
                <el-icon color="#67c23a"><Bowl /></el-icon>
                <span>饮食</span>
                <span class="category-calorie">约 {{ dietTotalCalorie }} kcal</span>
              </div>
              <div class="task-list">
                <div 
                  v-for="task in dietTasks" 
                  :key="task.id" 
                  class="task-item"
                  :class="{ completed: task.completed, skipped: task.skipped }"
                >
                  <div class="task-checkbox" @click="toggleTask(task)">
                    <el-icon v-if="task.completed" size="22" color="#67c23a"><CircleCheck /></el-icon>
                    <el-icon v-else-if="task.skipped" size="22" color="#909399"><Check /></el-icon>
                    <el-icon v-else size="22" color="#d9d9d9"><CircleCheck /></el-icon>
                  </div>
                  <div class="task-info">
                    <div class="task-name">{{ task.name }}</div>
                    <div class="task-meta">{{ task.type }} · {{ task.calorie }}</div>
                  </div>
                  <div class="task-actions">
                    <el-dropdown @command="(cmd) => handleTaskAction(task, cmd)">
                      <el-button size="small" text>
                        <el-icon :size="18"><MoreFilled /></el-icon>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item v-if="!task.completed && !task.skipped" command="skip">跳过</el-dropdown-item>
                          <el-dropdown-item v-if="task.completed || task.skipped" command="reset">重置</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </div>
              </div>
            </div>

            <div class="task-category">
              <div class="category-header">
                <el-icon color="#409eff"><TrendCharts /></el-icon>
                <span>运动</span>
              </div>
              <div class="task-list">
                <div 
                  v-for="task in exerciseTasks" 
                  :key="task.id" 
                  class="task-item"
                  :class="{ completed: task.completed, skipped: task.skipped }"
                >
                  <div class="task-checkbox" @click="toggleTask(task)">
                    <el-icon v-if="task.completed" size="22" color="#67c23a"><CircleCheck /></el-icon>
                    <el-icon v-else-if="task.skipped" size="22" color="#909399"><Check /></el-icon>
                    <el-icon v-else size="22" color="#d9d9d9"><CircleCheck /></el-icon>
                  </div>
                  <div class="task-info">
                    <div class="task-name">{{ task.name }}</div>
                    <div class="task-meta">{{ task.duration }}</div>
                  </div>
                  <div class="task-actions">
                    <el-dropdown @command="(cmd) => handleTaskAction(task, cmd)">
                      <el-button size="small" text>
                        <el-icon :size="18"><MoreFilled /></el-icon>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item v-if="!task.completed && !task.skipped" command="skip">跳过</el-dropdown-item>
                          <el-dropdown-item v-if="task.completed || task.skipped" command="reset">重置</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </div>
              </div>
            </div>
          </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card class="trend-section">
          <template #header>
            <span class="section-title">近7天完成趋势</span>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <div class="summary-card summary-card-diet">
          <el-card>
            <template #header>
              <span>饮食建议</span>
            </template>
            <div v-if="aiSummary && aiSummary.diet" class="summary-list">
              <div v-for="(item, index) in aiSummary.diet" :key="index" class="summary-item">
                <el-icon color="#67c23a" :size="18"><Check /></el-icon>
                <span>{{ item }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无建议，请先生成AI计划" :image-size="60" />
          </el-card>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="summary-card summary-card-exercise">
          <el-card>
            <template #header>
              <span>运动建议</span>
            </template>
            <div v-if="aiSummary && aiSummary.exercise" class="summary-list">
              <div v-for="(item, index) in aiSummary.exercise" :key="index" class="summary-item">
                <el-icon color="#409eff" :size="18"><Check /></el-icon>
                <span>{{ item }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无建议，请先生成AI计划" :image-size="60" />
          </el-card>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="summary-card summary-card-tips">
          <el-card>
            <template #header>
              <span>健康提示</span>
          </template>
          <div v-if="aiSummary && aiSummary.tips" class="summary-list">
            <div v-for="(item, index) in aiSummary.tips" :key="index" class="summary-item">
              <el-icon color="#e6a23c" :size="20"><Check /></el-icon>
              <span>{{ item }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无提示，请先生成AI计划" :image-size="60" />
          </el-card>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px" v-if="healthRecord && calorieRange">
      <el-col :span="24">
        <el-card>
          <div class="calorie-card">
            <div class="calorie-header">
              <el-icon :size="24" color="#722ed1"><Lightning /></el-icon>
              <span class="calorie-title">建议热量摄入范围</span>
            </div>
            <div class="calorie-content">
              <div class="calorie-item" :class="{ active: healthRecord.healthTarget === '减肥' }">
                <div class="calorie-icon" style="background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%)">
                  <el-icon :size="22" color="#fff"><ArrowDown /></el-icon>
                </div>
                <div class="calorie-info">
                  <div class="calorie-label">减肥</div>
                  <div class="calorie-range">{{ calorieRange.loseWeight.min }} - {{ calorieRange.loseWeight.max }} <span class="calorie-unit">kcal/天</span></div>
                  <div class="calorie-hint">TDEE - 400 ~ TDEE - 200</div>
                </div>
              </div>
              <div class="calorie-item" :class="{ active: healthRecord.healthTarget === '增肌' }">
                <div class="calorie-icon" style="background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%)">
                  <el-icon :size="22" color="#fff"><ArrowUp /></el-icon>
                </div>
                <div class="calorie-info">
                  <div class="calorie-label">增肌</div>
                  <div class="calorie-range">{{ calorieRange.gainMuscle.min }} - {{ calorieRange.gainMuscle.max }} <span class="calorie-unit">kcal/天</span></div>
                  <div class="calorie-hint">TDEE + 200 ~ TDEE + 400</div>
                </div>
              </div>
              <div class="calorie-item" :class="{ active: healthRecord.healthTarget === '维持健康' }">
                <div class="calorie-icon" style="background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%)">
                  <el-icon :size="22" color="#fff"><Minus /></el-icon>
                </div>
                <div class="calorie-info">
                  <div class="calorie-label">维持健康</div>
                  <div class="calorie-range">{{ calorieRange.maintain.min }} - {{ calorieRange.maintain.max }} <span class="calorie-unit">kcal/天</span></div>
                  <div class="calorie-hint">≈ TDEE</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog title="跳过原因" :visible.sync="skipDialogVisible" width="400px">
      <el-form :model="skipForm" label-width="80px">
        <el-form-item label="选择原因">
          <el-select v-model="skipForm.reason" placeholder="请选择跳过原因">
            <el-option label="没时间" value="没时间"></el-option>
            <el-option label="口味不适应" value="口味不适应"></el-option>
            <el-option label="身体不适" value="身体不适"></el-option>
            <el-option label="强度太大" value="强度太大"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注（可选）">
          <el-input v-model="skipForm.note" type="textarea" :rows="2"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="skipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSkip">确认跳过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { TrendCharts, ScaleToOriginal, CircleCheck, Check, Lightning, DataAnalysis, ArrowDown, ArrowUp, Minus, Loading, Bowl, MoreFilled } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { getHealthRecord, getLatestAiPlan, saveClockRecord, getTodayClockRecord, getWeeklyStats } from '@/api/user';
const healthRecord = ref(null);
const aiSummary = ref(null);
const calorieRange = ref(null);
const dailyTasks = ref([]);
const loadingTasks = ref(false);
const todayDate = ref('');
const currentPlanId = ref(null);
const skipDialogVisible = ref(false);
const skipForm = reactive({
 reason: '',
 note: ''
});
const currentSkipTask = ref(null);
const chartRef = ref(null);
let chartInstance = null;
const dietTasks = computed(() => dailyTasks.value.filter(t => t.type === '饮食'));
const exerciseTasks = computed(() => dailyTasks.value.filter(t => t.type === '运动'));
const dietTotalCalorie = computed(() => {
 return dietTasks.value.reduce((sum, task) => {
 const match = task.calorie?.match(/(\d+)/);
 return sum + (match ? parseInt(match[1]) : 0);
 }, 0);
});
const weeklyStatsData = reactive({
 completed: 0,
 total: 0
});
const weeklyRecords = ref([]);

const dailyStats = computed(() => {
 const todayCompleted = dailyTasks.value.filter(t => t.completed).length;
 const todayTotal = dailyTasks.value.length;
 const todayCompletionRate = todayTotal > 0 ? Math.round((todayCompleted / todayTotal) * 100) : 0;
 return {
 todayCompletionRate,
 weekCompleted: weeklyStatsData.completed,
 weekTotal: weeklyStatsData.total
 };
});
const fetchHealthRecord = async () => {
 try {
 const res = await getHealthRecord();
 healthRecord.value = res.data;
 if (res.data && res.data.tdee) {
 const tdee = parseFloat(res.data.tdee);
 calorieRange.value = {
 loseWeight: { min: Math.round(tdee - 400), max: Math.round(tdee - 200) },
 gainMuscle: { min: Math.round(tdee + 200), max: Math.round(tdee + 400) },
 maintain: { min: Math.round(tdee - 50), max: Math.round(tdee + 50) }
 };
 }
 }
 catch {
 }
};
const getBmiClass = (bmi) => {
 if (bmi < 18.5)
 return 'metric-card-underweight';
 if (bmi < 24)
 return 'metric-card-normal';
 if (bmi < 28)
 return 'metric-card-overweight';
 return 'metric-card-obese';
};
const getBmiDescription = (bmi) => {
 if (bmi < 18.5)
 return '偏瘦，建议增加营养摄入';
 if (bmi < 24)
 return '正常，继续保持';
 if (bmi < 28)
 return '偏胖，建议控制饮食';
 return '肥胖，建议就医咨询';
};
const fetchAiSummary = async () => {
 try {
 const res = await getLatestAiPlan();
 if (res.data && res.data.planContent) {
 const content = JSON.parse(res.data.planContent);
 aiSummary.value = content.summary;
 }
 }
 catch {
 }
};
const fetchDailyTasks = async () => {
 loadingTasks.value = true;
 try {
 const res = await getLatestAiPlan();
 if (res.data && res.data.planContent) {
 currentPlanId.value = res.data.id;
 const content = JSON.parse(res.data.planContent);
 const weeklyPlan = content.weeklyPlan || [];
 const today = new Date();
 const dayOfWeek = today.getDay();
 const dayIndex = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
 const todayPlan = weeklyPlan[dayIndex];
 if (todayPlan) {
 const tasks = [];
 let id = 1;
 if (todayPlan.diet) {
 todayPlan.diet.forEach(meal => {
 tasks.push({
 id: id++,
 type: '饮食',
 name: meal.name,
 mealType: meal.type,
 calorie: meal.calorie || '',
 completed: false,
 skipped: false,
 skipReason: ''
 });
 });
 }
 if (todayPlan.exercise) {
 todayPlan.exercise.forEach(ex => {
 tasks.push({
 id: id++,
 type: '运动',
 name: ex.name,
 duration: ex.duration || '',
 completed: false,
 skipped: false,
 skipReason: ''
 });
 });
 }
 dailyTasks.value = tasks;
 await loadTodayClockRecord();
 }
 }
 }
 catch {
 dailyTasks.value = [];
 }
 finally {
 loadingTasks.value = false;
 }
};

const loadTodayClockRecord = async () => {
 if (!currentPlanId.value) return;
 try {
 const res = await getTodayClockRecord(currentPlanId.value);
 if (res.data) {
 const finishItems = JSON.parse(res.data.finishItem || '[]');
 const unfinishReasons = JSON.parse(res.data.unfinishReason || '{}');
 
 dailyTasks.value.forEach(task => {
 const finished = finishItems.find(item => item.name === task.name);
 if (finished) {
 task.completed = true;
 }
 const reason = unfinishReasons[task.name];
 if (reason) {
 task.skipped = true;
 task.skipReason = reason;
 }
 });
 updateChart();
 }
 } catch {
 // 没有记录时忽略错误
 }
};
const toggleTask = (task) => {
 if (task.skipped)
 return;
 task.completed = !task.completed;
 updateChart();
 saveTasksToServer();
};
const handleTaskAction = (task, action) => {
 if (action === 'skip') {
 currentSkipTask.value = task;
 skipForm.reason = '';
 skipForm.note = '';
 skipDialogVisible.value = true;
 }
 else if (action === 'reset') {
 task.completed = false;
 task.skipped = false;
 task.skipReason = '';
 updateChart();
 }
};
const confirmSkip = () => {
 if (!skipForm.reason) {
 ElMessage.warning('请选择跳过原因');
 return;
 }
 if (currentSkipTask.value) {
 currentSkipTask.value.skipped = true;
 currentSkipTask.value.skipReason = skipForm.reason;
 if (skipForm.note) {
 currentSkipTask.value.skipReason += ` - ${skipForm.note}`;
 }
 }
 skipDialogVisible.value = false;
 updateChart();
 saveTasksToServer();
};

const saveTasksToServer = async () => {
 if (!currentPlanId.value) return;
 
 const finishItems = dailyTasks.value
 .filter(t => t.completed)
 .map(t => ({
 id: t.id,
 type: t.type,
 name: t.name,
 mealType: t.mealType,
 calorie: t.calorie,
 duration: t.duration
 }));
 
 const totalItems = dailyTasks.value
 .map(t => ({
 id: t.id,
 type: t.type,
 name: t.name,
 mealType: t.mealType,
 calorie: t.calorie,
 duration: t.duration
 }));
 
 const unfinishReasons = {};
 dailyTasks.value
 .filter(t => t.skipped && t.skipReason)
 .forEach(t => {
 unfinishReasons[t.name] = t.skipReason;
 });
 
 try {
 await saveClockRecord({
 planId: currentPlanId.value,
 finishItems,
 totalItems,
 unfinishReasons
 });
 await fetchWeeklyStats();
 } catch (error) {
 console.error('保存打卡记录失败', error);
 }
};
const initChart = () => {
 if (!chartRef.value)
 return;
 chartInstance = echarts.init(chartRef.value);
 updateChart();
};
const updateChart = () => {
 if (!chartInstance)
 return;
 const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
 const today = new Date();
 const dayOfWeek = today.getDay();
 const dayIndex = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
 
 // 初始化近7天完成率数组（默认为0）
 const completionRates = [0, 0, 0, 0, 0, 0, 0];
 
 // 从后端获取的打卡记录中提取每天的完成率
 weeklyRecords.value.forEach(record => {
 const recordDate = new Date(record.recordDate);
 const recordDayOfWeek = recordDate.getDay();
 const recordDayIndex = recordDayOfWeek === 0 ? 6 : recordDayOfWeek - 1;
 
 // 使用后端计算的 finishRate
 if (record.finishRate !== null && record.finishRate !== undefined) {
 completionRates[recordDayIndex] = parseFloat(record.finishRate) || 0;
 }
 });
 
 // 如果今天有实时数据（用户正在打卡），使用实时数据覆盖
 if (dayIndex < 7 && dailyTasks.value.length > 0) {
 const todayCompleted = dailyTasks.value.filter(t => t.completed).length;
 const todayTotal = dailyTasks.value.length;
 const todayRate = todayTotal > 0 ? Math.round((todayCompleted / todayTotal) * 100) : 0;
 // 只有当实时数据大于后端记录时才更新（用户刚完成更多任务）
 if (todayRate > completionRates[dayIndex]) {
 completionRates[dayIndex] = todayRate;
 }
 }
 
 const option = {
 tooltip: {
 trigger: 'axis',
 formatter: '{b}: {c}%'
 },
 grid: {
 left: '3%',
 right: '4%',
 bottom: '3%',
 containLabel: true
 },
 xAxis: {
    type: 'category',
    data: days,
    axisLine: {
      lineStyle: { color: '#e8e4df' }
    },
    axisLabel: {
      color: '#606266',
      fontSize: 14
    }
  },
  yAxis: {
    type: 'value',
    max: 100,
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: {
      color: '#909399',
      fontSize: 14,
      formatter: '{value}%'
    },
    splitLine: {
      lineStyle: { color: '#f0ece6' }
    }
  },
 series: [{
 name: '完成率',
 type: 'line',
 smooth: true,
 data: completionRates,
 areaStyle: {
 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
 { offset: 0, color: 'rgba(91, 155, 213, 0.3)' },
 { offset: 1, color: 'rgba(91, 155, 213, 0.05)' }
 ])
 },
 lineStyle: {
 width: 3,
 color: '#5b9bd5'
 },
 itemStyle: {
 color: '#5b9bd5'
 },
 symbol: 'circle',
 symbolSize: 8
 }]
 };
 chartInstance.setOption(option);
};
const formatTodayDate = () => {
 const today = new Date();
 const month = today.getMonth() + 1;
 const day = today.getDate();
 const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
 todayDate.value = `${month}月${day}日 ${weekDays[today.getDay()]}`;
};
const fetchWeeklyStats = async () => {
 try {
 const res = await getWeeklyStats();
 if (res.data) {
 weeklyStatsData.completed = res.data.weekCompleted || 0;
 weeklyStatsData.total = res.data.weekTotal || 0;
 weeklyRecords.value = res.data.records || [];
 updateChart();
 }
 } catch {
 weeklyStatsData.completed = 0;
 weeklyStatsData.total = 0;
 weeklyRecords.value = [];
 }
};

onMounted(() => {
 formatTodayDate();
 fetchHealthRecord();
 fetchAiSummary();
 fetchDailyTasks();
 fetchWeeklyStats();
 nextTick(() => {
 initChart();
 window.addEventListener('resize', () => {
 chartInstance?.resize();
 });
 });
});
watch(dailyTasks, () => {
 updateChart();
}, { deep: true });
</script>

<style scoped>
.home-container {
  padding: 0;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 28px;
  border-radius: 24px;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-card);
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 14px 36px rgba(0, 0, 0, 0.16);
  }
}

.metric-card-normal {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.metric-card-underweight {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
}

.metric-card-overweight {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
}

.metric-card-obese {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
}

.metric-card-bmr {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
}

.metric-card-tdee {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
}

.metric-card-completion {
  background: linear-gradient(135deg, #5b9bd5 0%, #85b8e8 100%);
}

.metric-icon {
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  backdrop-filter: blur(4px);
}

.metric-info {
  flex: 1;
}

.metric-label {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 10px;
  font-weight: 500;
}

.metric-value {
  font-size: 40px;
  font-weight: 700;
  color: #ffffff;
  line-height: 1.1;
  display: flex;
  align-items: baseline;
  gap: 8px;
  letter-spacing: -1px;
}

.metric-unit {
  font-size: 18px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.85);
}

.metric-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.section-date {
  font-size: 14px;
  color: #909399;
}

.checkin-section {
  height: 380px;
}

.checkin-section :deep(.el-card__body) {
  padding: 0;
  height: calc(100% - 54px);
}

.checkin-content {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
}

.checkin-content::-webkit-scrollbar {
  width: 6px;
}

.checkin-content::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 3px;
}

.checkin-content::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.checkin-content::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.empty-container {
  padding: 48px;
}

.tasks-container {
  padding: 16px;
}

.task-category {
  margin-bottom: 20px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.category-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  background: #faf8f5;
  border-radius: 14px;
  margin-bottom: 14px;
  font-size: 17px;
  font-weight: 700;
  color: #333;
}

.category-calorie {
  margin-left: auto;
  font-size: 15px;
  font-weight: 600;
  color: #67c23a;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #f0ece6;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: #5b9bd5;
    box-shadow: 0 2px 8px rgba(91, 155, 213, 0.1);
  }
  
  &.completed {
    background: rgba(103, 194, 58, 0.05);
    border-color: rgba(103, 194, 58, 0.3);
    
    .task-name {
      color: #67c23a;
      text-decoration: line-through;
    }
  }
  
  &.skipped {
    background: rgba(144, 147, 153, 0.05);
    border-color: rgba(144, 147, 153, 0.2);
    
    .task-name {
      color: #909399;
      text-decoration: line-through;
    }
  }
}

.task-checkbox {
  cursor: pointer;
  transition: transform 0.2s ease;
  
  &:hover {
    transform: scale(1.1);
  }
}

.task-info {
  flex: 1;
}

.task-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.task-meta {
  font-size: 14px;
  color: #909399;
}

.task-actions {
  opacity: 0;
  transition: opacity 0.2s ease;
}

.task-item:hover .task-actions {
  opacity: 1;
}

.trend-section {
  height: 380px;
}

.trend-section :deep(.el-card__body) {
  padding: 16px;
  height: calc(100% - 54px);
}

.chart-container {
  width: 100%;
  height: 100%;
}

.calorie-card {
  background: #faf8f6;
  border-radius: 32px;
  padding: 24px;
}

.calorie-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.calorie-title {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.calorie-content {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.calorie-item {
  flex: 1;
  min-width: calc(33.33% - 16px);
  background: #ffffff;
  border-radius: 24px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
  
  &.active {
    border-color: #409eff;
    background: rgba(64, 158, 255, 0.05);
  }
}

.calorie-icon {
  width: 56px;
  height: 56px;
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.calorie-info {
  flex: 1;
}

.calorie-label {
  font-size: 17px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.calorie-range {
  font-size: 26px;
  font-weight: 800;
  color: #1a1a2e;
  line-height: 1.2;
}

.calorie-unit {
  font-size: 14px;
  font-weight: 500;
  color: #909399;
  margin-left: 6px;
}

.calorie-hint {
  font-size: 14px;
  color: #909399;
  margin-top: 6px;
}

.summary-card {
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
  }
  
  :deep(.el-card__header) {
    font-size: 18px;
    font-weight: 700;
    color: #333;
    padding: 18px 24px;
    border-bottom: 1px solid #f0ece6;
  }
  
  :deep(.el-card__body) {
    padding: 24px;
  }
}

.summary-card-diet {
  :deep(.el-card) {
    background: linear-gradient(135deg, rgba(103, 194, 58, 0.08) 0%, rgba(103, 194, 58, 0.02) 100%);
    border: 1px solid rgba(103, 194, 58, 0.2);
    border-radius: 24px;
  }
  
  :deep(.el-card__header) {
    color: #52c41a;
  }
}

.summary-card-exercise {
  :deep(.el-card) {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.08) 0%, rgba(64, 158, 255, 0.02) 100%);
    border: 1px solid rgba(64, 158, 255, 0.2);
    border-radius: 24px;
  }
  
  :deep(.el-card__header) {
    color: #1890ff;
  }
}

.summary-card-tips {
  :deep(.el-card) {
    background: linear-gradient(135deg, rgba(230, 162, 60, 0.08) 0%, rgba(230, 162, 60, 0.02) 100%);
    border: 1px solid rgba(230, 162, 60, 0.2);
    border-radius: 24px;
  }
  
  :deep(.el-card__header) {
    color: #d48806;
  }
}

.summary-list {
  padding: 10px 0;
}

.summary-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px 18px;
  margin-bottom: 10px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 18px;
  font-size: 16px;
  color: #4a4a4a;
  line-height: 1.7;
  transition: all 0.25s ease;
  
  &:hover {
    background: #ffffff;
    transform: translateX(5px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  }
  
  &:last-child {
    margin-bottom: 0;
  }
  
  :deep(.el-icon) {
    flex-shrink: 0;
    margin-top: 3px;
  }
}
</style>
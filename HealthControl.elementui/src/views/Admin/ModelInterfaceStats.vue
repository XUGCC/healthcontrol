<template>
  <div class="stats-page">
    <el-card class="box-card">
      <template #header>
        <div class="clearfix">
          <span>模型接口调用统计</span>
        </div>
      </template>
      <div class="filter-row">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
        <el-button type="primary" size="default" class="ml-2" @click="loadStats">
          查询
        </el-button>
        <el-button size="default" @click="reset">
          重置
        </el-button>
      </div>
      <el-row :gutter="16" class="summary-row" v-if="summary">
        <el-col :span="8">
          <el-card shadow="never" class="summary-card">
            <div class="summary-title">调用总量</div>
            <div class="summary-number">{{ summary.TotalCalls || 0 }}</div>
            <div class="summary-sub">
              失败 {{ summary.FailCalls || 0 }} 次（
              {{ (summary.FailRate * 100).toFixed(2) }}%
              ）
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="summary-card">
            <div class="summary-title">平均耗时</div>
            <div class="summary-number">
              {{ summary.AvgCost != null ? summary.AvgCost + ' ms' : '-' }}
            </div>
            <div class="summary-sub">
              仅供趋势观察，不作为性能 SLA 承诺
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="summary-card">
            <div class="summary-title">服务质量分布</div>
            <div v-if="summary.ByQualityLevel && summary.ByQualityLevel.length">
              <div
                v-for="q in summary.ByQualityLevel"
                :key="q.QualityLevel"
                class="quality-row"
              >
                <span class="q-label">{{ qualityText(q.QualityLevel) }}</span>
                <el-progress
                  :percentage="qualityPercent(q.Count)"
                  :stroke-width="10"
                  :show-text="false"
                  :status="qualityStatus(q.QualityLevel)"
                />
                <span class="q-count">{{ q.Count }}</span>
              </div>
            </div>
            <div v-else class="summary-sub">暂无质量等级数据</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="box-card" style="margin-top: 16px" v-if="summary">
      <template #header>
        <div class="clearfix">
          <span>按调用环节统计</span>
        </div>
      </template>
      <el-table
        :data="summary.ByCallLink || []"
        style="width: 100%"
        size="small"
      >
        <el-table-column prop="CallLinkName" label="调用环节" min-width="140" />
        <el-table-column prop="Total" label="总调用次数" min-width="120" />
        <el-table-column
          prop="Fail"
          label="失败次数"
          min-width="120"
        ></el-table-column>
        <el-table-column label="失败率" min-width="120">
          <template #default="{ row }">
            <span>
              {{
                row.Total
                  ? ((row.Fail / row.Total) * 100).toFixed(2) + '%'
                  : '-'
              }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="平均耗时" min-width="120">
          <template #default="{ row }">
            <span>
              {{ row.AvgCost != null ? row.AvgCost + ' ms' : '-' }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Post } from '@/api/http';

const dateRange = ref([]);
const summary = ref(null);

const totalCount = computed(() => {
  if (!summary.value || !summary.value.TotalCalls) return 0;
  return summary.value.TotalCalls;
});

const initDefaultRange = () => {
  const end = new Date();
  const start = new Date();
  start.setDate(end.getDate() - 6);
  const format = (d) => {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  };
  dateRange.value = [format(start), format(end)];
};

const loadStats = async () => {
  const body = {};
  if (dateRange.value && dateRange.value.length === 2) {
    body.DateStart = dateRange.value[0];
    body.DateEnd = dateRange.value[1];
  }
  const { Success, Data, Msg } = await Post(
    '/TModelInterfaceCallLog/StatsSummary',
    body
  );
  if (!Success) {
    ElMessage.error(Msg || '加载统计失败');
    return;
  }
  summary.value = Data || null;
};

const reset = () => {
  initDefaultRange();
  loadStats();
};

const qualityText = (level) => {
  if (level === 0) return '正常';
  if (level === 1) return '轻微异常';
  if (level === 2) return '严重异常';
  return '未知';
};

const qualityStatus = (level) => {
  if (level === 0) return 'success';
  if (level === 1) return 'warning';
  if (level === 2) return 'exception';
  return 'success';
};

const qualityPercent = (count) => {
  if (!totalCount.value) return 0;
  return Math.min(100, ((count || 0) / totalCount.value) * 100);
};

onMounted(() => {
  initDefaultRange();
  loadStats();
});
</script>

<style scoped lang="scss">
.stats-page {
  padding: 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.summary-row {
  margin-top: 8px;
}

.summary-card {
  padding: 8px 12px;
}

.summary-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.summary-number {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.summary-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.quality-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.q-label {
  width: 70px;
  font-size: 12px;
  color: #606266;
}

.q-count {
  font-size: 12px;
  color: #606266;
}

.ml-2 {
  margin-left: 8px;
}
</style>


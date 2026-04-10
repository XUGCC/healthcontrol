<template>
  <div>
    <!-- 搜索表单卡片 -->
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <el-row>
          <el-button type="primary" size="default" @click="SearchClick">
            <el-icon>
              <Search />
            </el-icon>
            查询
          </el-button>
          <el-button type="warning" size="default" @click="ResetClick">
            <el-icon>
              <Refresh />
            </el-icon>
            清空条件
          </el-button>
        </el-row>
      </div>
      <div class="margin-top-sm">
        <el-form :inline="true" :model="searchForm" size="default">
          <el-form-item label="用户">
            <SigleSelect
              url="/User/List"
              class="search-input"
              columnName="Name"
              columnValue="Id"
              :clearable="true"
              v-model="searchForm.UserId"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="searchForm.Status"
              class="search-input"
              :clearable="true"
              placeholder="请选择状态"
            >
              <el-option label="待审核" value="PendingAudit" />
              <el-option label="已通过" value="Approved" />
              <el-option label="已拒绝" value="Rejected" />
              <el-option label="已审核(已完成)" value="Completed" />
              <el-option label="全部" value="" />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="searchForm.CreationTimeRange"
              type="datetimerange"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="软删除标记">
            <el-select
              v-model="searchForm.IsDelete"
              class="search-input"
              :clearable="true"
              placeholder="请选择"
            >
              <el-option label="否" :value="false" />
              <el-option label="是" :value="true" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <PaginationTable
      ref="PaginationTableId"
      url="/TDataExportRequest/List"
      :column="columnList"
      :where="where"
    >
      <template v-slot:header>
        <ExportButton exportUrl="/TDataExportRequest/Export" :where="searchForm" />
        <el-button type="danger" size="default" @click="BatchDelete">
          <el-icon>
            <Refresh />
          </el-icon>
          批量删除
        </el-button>
      </template>

      <template v-slot:AuditAction="{ row }">
        <el-button
          v-if="row.Status === 'PendingAudit'"
          type="success"
          size="small"
          @click="audit(row.Id, 'Approve')"
        >
          通过
        </el-button>
        <el-button
          v-if="row.Status === 'PendingAudit'"
          type="danger"
          size="small"
          @click="auditReject(row.Id)"
        >
          拒绝
        </el-button>
      </template>

      <template v-slot:Status="{ row }">
        <span>{{ statusText(row.Status) }}</span>
      </template>
    </PaginationTable>
  </div>
</template>

<script setup>
import { Post } from "@/api/http";
import { ColumnType } from "@/components/Tables/columnTypes";
import { useCommonStore } from "@/store";

import { Refresh, Search } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, reactive, ref } from "vue";

const commonStore = useCommonStore();
const UserId = computed(() => commonStore.UserId);

// 表格条件
const where = reactive({});
// 搜索表单数据
const searchForm = reactive({
  Status: "PendingAudit",
});

// 表格引用
const PaginationTableId = ref(null);

// 表格列配置
const columnList = ref([
  {
    key: "Id",
    hidden: true,
  },
  {
    key: "UserId",
    hidden: true,
  },
  {
    key: "UserDto.Name",
    title: "用户名称",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "DataType",
    title: "数据类型",
    type: ColumnType.LONGTEXT,
  },
  {
    key: "ExportFormat",
    title: "导出格式",
    width: "120px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "Status",
    title: "状态",
    width: "120px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "FileSize",
    title: "文件大小（字节）",
    width: "160px",
    type: ColumnType.NUMBER,
  },
  {
    key: "CreationTime",
    title: "创建时间",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "ProcessTime",
    title: "完成时间",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "AuditAction",
    title: "审核",
    width: "160px",
    // PaginationTable 的“操作列/自定义列”分支是 type == 4
    type: ColumnType.USERDEFINED,
  },
]);

// 搜索点击
const SearchClick = () => {
  PaginationTableId.value.Reload(searchForm);
};

// 重置搜索条件
const ResetClick = () => {
  Object.keys(searchForm).forEach((key) => {
    searchForm[key] = undefined;
  });
  PaginationTableId.value.Reload(searchForm);
};

const statusText = (s) => {
  if (s === "PendingAudit") return "待审核";
  if (s === "Approved") return "已通过";
  if (s === "Rejected") return "已拒绝";
  if (s === "Processing") return "处理中";
  if (s === "Completed") return "已审核";
  if (s === "Failed") return "失败";
  if (s === "Pending") return "待处理";
  return s || "-";
};

const BatchDelete = async () => {
  const rows = PaginationTableId.value.GetSelectionRow();
  if (!rows || rows.length === 0) {
    ElMessage.warning("请先勾选要删除的记录");
    return;
  }
  const ids = rows.map((x) => x.Id);
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${ids.length} 条导出申请记录？该操作仅删除申请记录本身，不会影响已导出的文件。`, "批量删除", {
      confirmButtonText: "确认删除",
      cancelButtonText: "取消",
      type: "warning",
    });
    const { Success } = await Post("/TDataExportRequest/BatchDelete", { Ids: ids });
    if (Success !== false) {
      ElMessage.success("删除成功");
      PaginationTableId.value.Reload(searchForm);
    }
  } catch (e) {
    // 取消则忽略
  }
};

const audit = async (id, action, remark) => {
  try {
    await Post("/TDataExportRequest/Audit", {
      Id: id,
      Action: action,
      Remark: remark,
    });
    ElMessage.success(action === "Approve" ? "已通过" : "已拒绝");
    PaginationTableId.value.Reload(searchForm);
  } catch (e) {
    console.error(e);
  }
};

const auditReject = async (id) => {
  try {
    const { value } = await ElMessageBox.prompt("可填写拒绝原因（选填）", "拒绝导出申请", {
      confirmButtonText: "确定拒绝",
      cancelButtonText: "取消",
      inputType: "textarea",
      inputPlaceholder: "例如：信息不完整/疑似异常操作/请联系管理员补充说明...",
      inputValue: "",
      showInput: true,
    });
    await audit(id, "Reject", value);
  } catch (e) {
    // 用户取消时会抛异常，这里直接忽略
  }
};
</script>

<style scoped>
</style>


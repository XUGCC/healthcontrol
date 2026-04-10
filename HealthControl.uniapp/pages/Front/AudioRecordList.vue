<template>
  <view class="hc-mobile-shell audio-record-list-page">
    <view class="hc-screen hc-stack record-list-screen" :class="{ 'with-bottom-bar': selectionMode }">
      <view class="bg-head page-head hc-reveal-up">
        <view class="page-head__top">
          <view class="page-head__back hc-interactive-pill" @click="goBack">
            <uni-icons type="left" size="16" color="#132015" />
            <text>返回</text>
          </view>
        </view>
        <view class="page-head__kicker-wrap">
          <view class="hc-kicker">检测记录</view>
        </view>
        <text class="bg-head__title">历史检测记录</text>
        <text class="bg-head__subtitle">长按可多选，支持置顶、删除与快速标注。</text>
      </view>

      <view v-if="selectionMode" class="selection-header hc-reveal-fade">
        <view class="selection-action" @click="exitSelection">
          <uni-icons type="closeempty" size="20" color="#2f4330" />
        </view>
        <text class="selection-title">已选择 {{ selectedIds.length }} 项</text>
        <view class="selection-action" @click="toggleSelectAll">
          <uni-icons :type="isAllSelected ? 'clear' : 'checkbox-filled'" size="20" color="#2f4330" />
        </view>
      </view>

      <view class="record-list" v-if="recordList.length > 0">
        <view
          class="record-item hc-interactive-card hc-reveal-up"
          :class="{ pinned: pinnedIds.includes(item.Id) }"
          :style="{ '--delay': `${120 + index * 40}ms` }"
          v-for="(item, index) in recordList"
          :key="item.Id"
          @click="onItemClick(item.Id)"
          @longpress="onItemLongPress(item.Id)"
        >
          <view class="record-main">
            <view class="record-header">
              <view
                class="record-status"
                :class="{
                  'status-success': item.DetectTotalStatus && !item.PrimaryScreenResult,
                  'status-warning': item.DetectTotalStatus && item.PrimaryScreenResult,
                  'status-processing': !item.DetectTotalStatus,
                }"
              >
                <text v-if="!item.DetectTotalStatus">检测中</text>
                <text v-else-if="item.PrimaryScreenResult">异常</text>
                <text v-else>正常</text>
              </view>
              <view class="record-header-right">
                <text v-if="hasLabel(item.Id)" class="record-label-tag">已标注</text>
                <text v-if="pinnedIds.includes(item.Id)" class="record-pin-tag">置顶</text>
              </view>
            </view>

            <view class="record-content" v-if="item.DetectTotalStatus">
              <view class="record-info">
                <text class="info-label">置信度</text>
                <text class="info-value">{{ (item.PrimaryScreenConfidence * 100).toFixed(1) }}%</text>
                <text v-if="getGenderText(item)" class="info-gender">
                  性别：{{ getGenderText(item) }}
                  <text v-if="getGenderConfidenceText(item)" class="info-gender-conf">({{ getGenderConfidenceText(item) }})</text>
                </text>
              </view>
              <view class="record-footer">
                <text class="record-time">{{ formatTime(item.CreationTime) }}</text>
                <view class="label-entry-btn hc-interactive-pill" @click.stop="goToLabelForm(item)">
                  {{ hasLabel(item.Id) ? "修改标注" : "去标注" }}
                </view>
              </view>
            </view>

            <view class="record-content" v-else>
              <view class="record-info">
                <text v-if="getGenderText(item)" class="info-gender">
                  性别：{{ getGenderText(item) }}
                  <text v-if="getGenderConfidenceText(item)" class="info-gender-conf">({{ getGenderConfidenceText(item) }})</text>
                </text>
              </view>
              <view class="record-footer">
                <text class="record-time">{{ formatTime(item.CreationTime) }}</text>
              </view>
            </view>
          </view>

          <view class="record-tail">
            <view v-if="selectionMode" class="record-checkbox">
              <checkbox
                class="record-checkbox-inner"
                :checked="selectedIds.includes(item.Id)"
                color="#7daf4f"
                @click.stop="toggleSelect(item.Id)"
              />
            </view>
            <uni-icons v-else type="right" size="18" color="#93a38f" />
          </view>
        </view>
      </view>

      <view class="empty-state hc-reveal-up" style="--delay: 120ms" v-else-if="!isLoading">
        <view class="empty-icon"><uni-icons type="list" size="54" color="#819180" /></view>
        <text class="empty-text">暂无检测记录</text>
        <view class="empty-btn hc-pill-button-dark hc-interactive-pill" @click="goToRecord">开始检测</view>
      </view>

      <view class="loading-state" v-if="isLoading">
        <text>加载中...</text>
      </view>

      <view class="load-more" v-if="hasMore && !isLoading">
        <view class="load-more-btn hc-pill-button-soft hc-interactive-pill" @click="loadMore">加载更多</view>
      </view>

      <view v-if="selectionMode && !isPopupOpen" class="bottom-action-bar">
        <view class="bottom-action-item hc-interactive-pill" :class="{ disabled: selectedIds.length === 0 }" @click="pinRecords">
          {{ isAllPinnedSelected ? "取消置顶" : "置顶" }}
        </view>
        <view
          class="bottom-action-item danger hc-interactive-pill"
          :class="{ disabled: selectedIds.length === 0 }"
          @click="confirmDelete"
        >
          删除
        </view>
      </view>
    </view>
    <hc-dialog ref="dialogRef" @popup-change="onPopupChange" />
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Post } from '@/utils/http'
import { useCommonStore } from '@/store'
import HcDialog from '@/components/hc-dialog/hc-dialog.vue'

const commonStore = useCommonStore()

// 状态管理
const recordList = ref([])
const isLoading = ref(false)
const pageIndex = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)
// 选中记录 Id 集合（使用数组，便于在模板中直接 includes 判断，避免 Set 在部分端的响应式问题）
const selectedIds = ref([])
// 置顶记录 Id 集合
const pinnedIds = ref([])
// 是否处于多选操作模式（长按进入）
const selectionMode = ref(false)
const labeledDetectIds = ref([])
const dialogRef = ref(null)
const activePopupCount = ref(0)
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true })

// 本地存储 key（按用户区分）
const getPinnedStorageKey = () => {
    return `audio_record_pinned_${commonStore.UserId || 0}`
}

// 从本地存储加载置顶状态
const loadPinnedIds = () => {
    try {
        const key = getPinnedStorageKey()
        const stored = uni.getStorageSync(key)
        if (stored && Array.isArray(stored)) {
            pinnedIds.value = stored
        }
    } catch (e) {
        console.error('加载置顶状态失败:', e)
    }
}

// 保存置顶状态到本地存储
const savePinnedIds = () => {
    try {
        const key = getPinnedStorageKey()
        uni.setStorageSync(key, pinnedIds.value)
    } catch (e) {
        console.error('保存置顶状态失败:', e)
    }
}

// 是否全选（基于当前列表）
const isAllSelected = computed(() => {
    const ids = recordList.value.map((x) => x.Id)
    return ids.length > 0 && ids.every((id) => selectedIds.value.includes(id))
})

// 当前选中的是否全部为已置顶记录（用于切换按钮文案和行为）
const isAllPinnedSelected = computed(() => {
    if (selectedIds.value.length === 0) return false
    return selectedIds.value.every((id) => pinnedIds.value.includes(id))
})
const isPopupOpen = computed(() => activePopupCount.value > 0)
const onPopupChange = (show) => {
    activePopupCount.value = Math.max(0, activePopupCount.value + (show ? 1 : -1))
}

// 格式化时间
const formatTime = (timeStr) => {
    if (!timeStr) return '--'
    try {
        const date = new Date(timeStr)
        if (isNaN(date.getTime())) return '--'
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        return `${month}-${day} ${hours}:${minutes}`
    } catch (e) {
        console.error('时间格式化失败:', e, timeStr)
        return '--'
    }
}

// 从记录中提取简短的性别文本（优先 VoiceGender，其次 VoiceGenderCode）
const getGenderText = (item) => {
    if (!item) return ''
    if (item.VoiceGender) {
        return item.VoiceGender
    }
    if (typeof item.VoiceGenderCode === 'number') {
        return item.VoiceGenderCode === 0 ? '女性' : '男性'
    }
    return ''
}

// 获取性别置信度文本
const getGenderConfidenceText = (item) => {
    if (!item) return ''
    if (typeof item.VoiceGenderConfidence === 'number') {
        return (item.VoiceGenderConfidence * 100).toFixed(1) + '%'
    }
    return ''
}

const hasLabel = (detectId) => labeledDetectIds.value.includes(detectId)

const loadLabelStatus = async () => {
    if (!commonStore.UserId) {
        labeledDetectIds.value = []
        return
    }
    try {
        const response = await Post('/Front/ModelLabel/List', {
            UserId: commonStore.UserId,
            Page: 1,
            Limit: 500
        })
        if (response.Success && response.Data && Array.isArray(response.Data.Items)) {
            labeledDetectIds.value = response.Data.Items
                .map((item) => item.DetectId)
                .filter((id) => !!id)
        } else {
            labeledDetectIds.value = []
        }
    } catch (error) {
        console.error('加载标注状态失败:', error)
        labeledDetectIds.value = []
    }
}

// 加载记录列表
const loadRecordList = async (isLoadMore = false) => {
    if (isLoading.value) return
    
    if (!isLoadMore) {
        pageIndex.value = 1
        recordList.value = []
        hasMore.value = true
        selectedIds.value = []
    }
    
    isLoading.value = true
    
    try {
        const response = await Post('/TAudioScreenRecord/List', {
            // 后端分页入参继承 PagedInput：字段为 Page / Limit
            Page: pageIndex.value,
            Limit: pageSize.value,
            UserId: commonStore.UserId,
            // 该项目后端排序通常通过 SortItem 处理，这里不强依赖排序字段
        })
        
        if (response.Success && response.Data) {
            const items = response.Data.Items || []
            const totalCount = response.Data.TotalCount || 0
            
            if (isLoadMore) {
                recordList.value = [...recordList.value, ...items]
            } else {
                recordList.value = items
            }
            
            // 根据置顶状态排序：置顶记录在前，其余在后
            const pinnedSet = new Set(pinnedIds.value)
            const pinned = recordList.value.filter((x) => pinnedSet.has(x.Id))
            const others = recordList.value.filter((x) => !pinnedSet.has(x.Id))
            recordList.value = [...pinned, ...others]
            
            // 重新加载后清空选中
            if (!isLoadMore) {
                selectedIds.value = []
                selectionMode.value = false
            }
            
            // 判断是否还有更多
            hasMore.value = recordList.value.length < totalCount
            
            pageIndex.value++

            if (!isLoadMore) {
                loadLabelStatus()
            }
        } else {
            uni.showToast({
                title: response.Msg || '加载失败',
                icon: 'none'
            })
        }
    } catch (error) {
        console.error('加载记录列表失败:', error)
        uni.showToast({
            title: '加载失败，请重试',
            icon: 'error'
        })
    } finally {
        isLoading.value = false
    }
}

// 加载更多
const loadMore = () => {
    if (hasMore.value && !isLoading.value) {
        loadRecordList(true)
    }
}

// 单条选择/取消
const toggleSelect = (id) => {
    const arr = [...selectedIds.value]
    const index = arr.indexOf(id)
    if (index > -1) {
        arr.splice(index, 1)
    } else {
        arr.push(id)
    }
    selectedIds.value = arr
}

// 全选/取消全选（基于当前列表）
const toggleSelectAll = () => {
    if (!selectionMode.value) {
        selectionMode.value = true
    }
    if (isAllSelected.value) {
        selectedIds.value = []
        return
    }
    selectedIds.value = recordList.value.map((item) => item.Id)
}

// 删除前确认
const confirmDelete = () => {
    if (selectedIds.value.length === 0) return

    showDialog({
        title: '确认删除',
        content: `确定删除选中的 ${selectedIds.value.length} 条检测记录吗？`,
    }).then((res) => {
            if (res.confirm) {
                doDelete()
            }
        })
}

// 调用后端删除接口：1 条走 Delete，多条走 BatchDelete
const doDelete = async () => {
    const ids = [...selectedIds.value]
    if (ids.length === 0) return

    try {
        uni.showLoading({
            title: '删除中...',
            mask: true
        })

        if (ids.length === 1) {
            await Post('/TAudioScreenRecord/Delete', {
                Id: ids[0]
            })
        } else {
            await Post('/TAudioScreenRecord/BatchDelete', {
                Ids: ids
            })
        }

        uni.showToast({
            title: '删除成功',
            icon: 'success'
        })

        // 从置顶列表中移除已删除的记录
        const deleteSet = new Set(ids)
        pinnedIds.value = pinnedIds.value.filter((id) => !deleteSet.has(id))
        savePinnedIds()

        // 删除后重新加载列表
        await loadRecordList(false)
    } catch (error) {
        console.error('删除检测记录失败:', error)
        uni.showToast({
            title: '删除失败，请重试',
            icon: 'error'
        })
    } finally {
        uni.hideLoading()
    }
}

// 置顶 / 取消置顶（前端视图置顶：把选中记录移动到列表顶部）
const pinRecords = () => {
    if (selectedIds.value.length === 0) return

    if (isAllPinnedSelected.value) {
        // 如果本次选择的全部是已置顶记录，则执行“取消置顶”
        const removeSet = new Set(selectedIds.value)
        pinnedIds.value = pinnedIds.value.filter((id) => !removeSet.has(id))
    } else {
        // 否则将本次选择加入置顶集合
        pinnedIds.value = Array.from(new Set([...pinnedIds.value, ...selectedIds.value]))
    }

    // 根据 pinnedIds 重新排序：置顶记录在前，其余在后
    const pinnedSet = new Set(pinnedIds.value)
    const pinned = recordList.value.filter((x) => pinnedSet.has(x.Id))
    const others = recordList.value.filter((x) => !pinnedSet.has(x.Id))
    recordList.value = [...pinned, ...others]
    
    // 保存置顶状态到本地存储
    savePinnedIds()
}

// 普通点击：在选择模式下切换勾选，否则进入详情
const onItemClick = (id) => {
    if (selectionMode.value) {
        toggleSelect(id)
        return
    }
    uni.navigateTo({
        url: `/pages/Front/AudioResult?id=${id}`
    })
}

// 长按进入选择模式
const onItemLongPress = (id) => {
    selectionMode.value = true
    if (!selectedIds.value.includes(id)) {
        selectedIds.value = [id]
    }
}

// 退出选择模式
const exitSelection = () => {
    selectionMode.value = false
    selectedIds.value = []
}

// 前往录音页面
const goToRecord = () => {
    uni.navigateTo({
        url: '/pages/Front/AudioRecord'
    })
}

const goToLabelForm = (item) => {
    if (!item || !item.Id) {
        uni.showToast({
            title: '检测记录不存在',
            icon: 'none'
        })
        return
    }
    uni.navigateTo({
        url: `/pages/Front/ModelLabelForm?detectId=${item.Id}`
    })
}

// 返回上一页
const goBack = () => {
    uni.navigateBack()
}

// 页面加载
onMounted(() => {
    if (!commonStore.UserId) {
        uni.showToast({
            title: '请先登录',
            icon: 'none'
        })
        setTimeout(() => {
            uni.reLaunch({ url: '/pages/Front/Login' })
        }, 1500)
        return
    }
    
    // 先加载置顶状态，再加载记录列表（这样排序时会用到置顶状态）
    loadPinnedIds()
    loadRecordList()
})
</script>

<style scoped lang="scss">
.audio-record-list-page {
  min-height: 100vh;
}

.record-list-screen {
  gap: 24upx;
}

.record-list-screen.with-bottom-bar {
  padding-bottom: calc(env(safe-area-inset-bottom) + 210upx);
}

.page-head {
  gap: 12upx;
}

.page-head__top {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.page-head__back {
  min-height: 58upx;
  padding: 0 20upx;
  border-radius: 999upx;
  background: rgba(248, 252, 239, 0.86);
  border: 1upx solid rgba(196, 216, 136, 0.92);
  display: inline-flex;
  align-items: center;
  gap: 8upx;
  font-size: 22upx;
  font-weight: 700;
  color: #152012;
}

.page-head__kicker-wrap {
  margin-top: 4upx;
}

.selection-header {
  min-height: 80upx;
  border-radius: 28upx;
  padding: 12upx 18upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(202, 220, 149, 0.9);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.selection-title {
  font-size: 26upx;
  color: #213021;
  font-weight: 700;
}

.selection-action {
  width: 54upx;
  height: 54upx;
  border-radius: 18upx;
  background: #e7f2ce;
  border: 1upx solid rgba(194, 214, 136, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.record-item {
  border-radius: 36upx;
  padding: 22upx;
  background: rgba(248, 252, 239, 0.9);
  border: 1upx solid rgba(202, 220, 149, 0.86);
  box-shadow: 0 10upx 24upx rgba(35, 48, 35, 0.08);
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 16upx;
}

.record-item.pinned {
  border-color: rgba(144, 185, 73, 0.8);
  box-shadow: 0 14upx 28upx rgba(86, 122, 57, 0.18);
}

.record-main {
  flex: 1;
  min-width: 0;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10upx;
}

.record-status {
  padding: 8upx 18upx;
  border-radius: 999upx;
  font-size: 20upx;
  font-weight: 800;
  line-height: 1;

  &.status-success {
    background: #eef8dd;
    color: #86b44a;
  }

  &.status-warning {
    background: #f9ebe5;
    color: #ca695a;
  }

  &.status-processing {
    background: #eaf4df;
    color: #5f844d;
  }
}

.record-header-right {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8upx;
}

.record-label-tag,
.record-pin-tag {
  font-size: 20upx;
  padding: 6upx 12upx;
  border-radius: 999upx;
  line-height: 1;
}

.record-label-tag {
  border: 1upx solid #8eb650;
  color: #4f7b29;
  background: #f1f8e2;
}

.record-pin-tag {
  border: 1upx solid #5c7d4b;
  color: #3a5536;
  background: #eaf2de;
}

.record-content {
  margin-top: 14upx;
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.record-info {
  font-size: 22upx;
  color: #5a695b;
  display: flex;
  align-items: center;
  gap: 16upx;
  flex-wrap: wrap;
}

.info-label {
  color: #78887a;
}

.info-value {
  color: #1d2a1f;
  font-weight: 800;
  font-size: 34upx;
}

.info-gender {
  color: #4f684f;
  font-weight: 600;
}

.info-gender-conf {
  color: #90a090;
  font-size: 20upx;
  font-weight: 500;
}

.record-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
}

.record-time {
  font-size: 22upx;
  color: #8b9a8d;
}

.label-entry-btn {
  min-height: 62upx;
  padding: 0 22upx;
  border-radius: 999upx;
  background: rgba(238, 246, 223, 0.9);
  border: 1upx solid rgba(126, 164, 76, 0.75);
  color: #43623c;
  font-size: 22upx;
  font-weight: 700;
  line-height: 62upx;
  white-space: nowrap;
}

.record-tail {
  display: flex;
  align-items: center;
}

.record-checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
}

.record-checkbox-inner {
  transform: scale(0.9);
}

.empty-state {
  min-height: 460upx;
  border-radius: 36upx;
  border: 1upx dashed rgba(171, 197, 131, 0.9);
  background: rgba(246, 251, 233, 0.62);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40upx;
}

.empty-text {
  margin-top: 18upx;
  font-size: 24upx;
  color: #7a8a79;
}

.empty-btn {
  margin-top: 24upx;
  width: 260upx;
}

.loading-state {
  text-align: center;
  padding: 28upx;
  color: #819181;
  font-size: 24upx;
}

.load-more {
  display: flex;
  justify-content: center;
  padding-bottom: 12upx;
}

.load-more-btn {
  min-width: 220upx;
}

.bottom-action-bar {
  position: fixed;
  left: 28upx;
  right: 28upx;
  bottom: calc(env(safe-area-inset-bottom) + 26upx);
  z-index: 20;
  border-radius: 999upx;
  padding: 10upx;
  background: rgba(248, 252, 239, 0.92);
  border: 1upx solid rgba(202, 220, 149, 0.9);
  box-shadow: 0 14upx 28upx rgba(39, 55, 35, 0.16);
  display: flex;
  gap: 12upx;
}

.bottom-action-item {
  flex: 1;
  text-align: center;
  min-height: 74upx;
  border-radius: 999upx;
  background: linear-gradient(135deg, #5f844f 0%, #8cb45e 100%);
  color: #ffffff;
  font-size: 26upx;
  font-weight: 800;
  line-height: 74upx;
}

.bottom-action-item.danger {
  background: linear-gradient(135deg, #bc5d4f 0%, #d97966 100%);
}

.bottom-action-item.disabled {
  opacity: 0.45;
}
</style>



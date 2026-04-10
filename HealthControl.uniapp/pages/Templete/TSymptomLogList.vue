<template>
    <view>
        <uni-nav-bar dark :fixed="true" shadow background-color="var(--primary-color)" status-bar left-icon="left"
            left-text="返回" @clickLeft="goBack" title="症状日志列表" />


    </view>
</template>

<script setup>

import { useCommonStore } from '@/store';
import { Post } from '@/utils/http';
import { onLoad, onReady, onShow } from "@dcloudio/uni-app";
import { computed, reactive, ref } from 'vue';

// 获取store
const commonStore = useCommonStore();
const Token = computed(() => commonStore.Token)
const UserInfo = computed(() => commonStore.UserInfo)
const RoleType = computed(() => commonStore.RoleType)
const UserId = computed(() => commonStore.UserId)



const TestInfoList = ref([]);

const where = reactive({

});

// 生命周期钩子
onLoad(async (option) => {
});
onShow(async () => {
    GetTSymptomLogListApi();
});

onReady(async () => {


});


// 方法
const goBack = () => {
    uni.navigateBack();
};


const GetTSymptomLogListApi = async () => {
    let {
        Data: {
            Items
        }
    } = await Post('/TSymptomLog/List', where);
    TSymptomLogList.value = Items;
}
// 删除
const ShowDeleteModal = async (Id) => {
    uni.showModal({
        title: '提示',
        content: '确认删除该测试信息吗？',
        success: async (res) => {
            if (res.confirm) {
                const { Success } = await Post(`/TSymptomLog/Delete`, { Id: Id });
                if (Success) {
                    await GetTSymptomLogListApi();
                    uni.showToast({
                        title: '删除成功',
                        icon: 'success'
                    });
                }
            }
        }
    })
};

</script>

<style scoped lang="scss"></style>
<template>
    <view>
        <uni-nav-bar dark :fixed="true" shadow background-color="var(--primary-color)" status-bar left-icon="left"
            left-text="返回" @clickLeft="goBack" title="个人喉部健康档案信息" />

        <uni-section title="请填写个人喉部健康档案信息">
            <view class="form-container">

                <uni-forms ref="editModalForm" :model="formData" :rules="editModalFormRules" label-width='100'
                    label-position='top'>
                    <uni-forms-item label="关联用户表主键t_user." required name="UserId">
                        <sigle-select url="/User/List" columnName="Name" columnValue="Id" v-model="formData.UserId">
                        </sigle-select>
                    </uni-forms-item>
                    <uni-forms-item label="近期主要不适症状" required name="RecentSymptom">
                        <uni-easyinput v-model="formData.RecentSymptom" placeholder="请输入近期主要不适症状" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="症状持续时间：如1周/1月" required name="SymptomDuration">
                        <uni-easyinput v-model="formData.SymptomDuration" placeholder="请输入症状持续时间：如1周/1月" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="日常护嗓习惯" required name="DailyVoiceCare">
                        <uni-easyinput v-model="formData.DailyVoiceCare" placeholder="请输入日常护嗓习惯" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="末次自查时间" required name="LastScreenTime">
                        <uni-datetime-picker type="datetime" return-type="string" v-model="formData.LastScreenTime"
                            primaryColor="var(--primary-color)" />
                    </uni-forms-item>
                    <uni-forms-item label="健康趋势标签：如好转/恶化/稳定" required name="HealthTrendTag">
                        <uni-easyinput v-model="formData.HealthTrendTag" placeholder="请输入健康趋势标签：如好转/恶化/稳定" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="关联最新检测" required name="LatestDetectId">
                        <sigle-select url="/TAudioScreenRecord/List" columnName="AudioUrl" columnValue="Id" v-model="formData.LatestDetectId">
                        </sigle-select>
                    </uni-forms-item>
                    <uni-forms-item label="风险评估等级：0=低，1=中，2=高" required name="RiskAssessmentLevel">
                        <uni-data-checkbox v-model="formData.RiskAssessmentLevel" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="软删除标记：0=未删除，1=已删除" required name="IsDelete">
                        <uni-data-checkbox v-model="formData.IsDelete" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                </uni-forms>
                <button class="btn-primary" @click="createOrEditAsync">提交</button>
            </view>

        </uni-section>


    </view>
</template>

<script setup>

import { useCommonStore } from '@/store';
import { Post } from "@/utils/http";
import { onLoad, onReady, onShow } from "@dcloudio/uni-app";
import { computed, nextTick, reactive, ref } from 'vue';

// 获取store
const commonStore = useCommonStore();
const Token = computed(() => commonStore.Token)
const UserInfo = computed(() => commonStore.UserInfo)
const RoleType = computed(() => commonStore.RoleType)
const UserId = computed(() => commonStore.UserId)


// 表单数据和规则
const formData = reactive({
});
const editModalForm = ref(null);

const Id=ref(null);
const editModalFormRules = {
	UserId:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	RecentSymptom:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	SymptomDuration:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	DailyVoiceCare:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	LastScreenTime:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	HealthTrendTag:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	LatestDetectId:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	RiskAssessmentLevel:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	IsDelete:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
};




// 生命周期钩子
onLoad(async (option) => {
  	 Id.value=option.Id;
    ShowEditModal(Id.value);


});

onShow(async () => {
    // 页面显示时的逻辑
});

onReady(async () => {


});

// 显示编辑对话框
const ShowEditModal = async (Id) => {

    const { Data } = await Post(`/TPersonalLaryngealHealthRecord/Get`, { Id: Id });
    Object.assign(formData, Data);


  

  
    nextTick(() => {
        // 设置自定义表单校验规则，必须在节点渲染完毕后执行
        editModalForm.value.setRules(editModalFormRules);
    });
};

// 方法
const goBack = () => {
    uni.navigateBack();
};

//地址选择回调
const AddressSelected = (data) => {
    formData.Address = data.fullAddress;

};

// 创建或修改方法
const createOrEditAsync = async () => {
    editModalForm.value.validate().then(res => {
        uni.showModal({
            title: "提示",
            content: "你确定要操作吗?",
            showCancel: true,
            cancelText: "取消",
            confirmText: "确定",
            success: async (res) => {
                if (res.confirm) {                                 
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                    let { Data, Success } = await Post("/TPersonalLaryngealHealthRecord/CreateOrEdit", formData);
                    if (Success) {
                        uni.showToast({
                            title: "操作成功",
                            icon: "success"
                        });

                        uni.navigateBack();
                    }
                }
            }
        });

    }).catch(err => {
        uni.showToast({
            title: "请检测输入项是否正确",
            icon: "none"
        });

    });


};
</script>

<style scoped lang="scss"></style>
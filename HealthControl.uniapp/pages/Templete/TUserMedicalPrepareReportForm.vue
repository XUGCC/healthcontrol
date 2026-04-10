<template>
    <view>
        <uni-nav-bar dark :fixed="true" shadow background-color="var(--primary-color)" status-bar left-icon="left"
            left-text="返回" @clickLeft="goBack" title="用户就诊准备报告信息" />

        <uni-section title="请填写用户就诊准备报告信息">
            <view class="form-container">

                <uni-forms ref="editModalForm" :model="formData" :rules="editModalFormRules" label-width='100'
                    label-position='top'>
                    <uni-forms-item label="关联用户表主键t_user." required name="UserId">
                        <sigle-select url="/User/List" columnName="Name" columnValue="Id" v-model="formData.UserId">
                        </sigle-select>
                    </uni-forms-item>
                    <uni-forms-item label="关联健康档案表主键t_personal_laryngeal_health_record." required name="HealthRecordId">
                        <sigle-select url="/TPersonalLaryngealHealthRecord/List" columnName="RecentSymptom" columnValue="Id" v-model="formData.HealthRecordId">
                        </sigle-select>
                    </uni-forms-item>
                    <uni-forms-item label="关联检测" required name="DetectIdList">
                        <uni-easyinput v-model="formData.DetectIdList" placeholder="请输入关联检测" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="文档模板类型：0=简版，1=详版" required name="TemplateType">
                        <uni-data-checkbox v-model="formData.TemplateType" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="文档内容范围：如筛查记录+症状日志+饮食记录" required name="ContentScope">
                        <uni-easyinput v-model="formData.ContentScope" placeholder="请输入文档内容范围：如筛查记录+症状日志+饮食记录" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="标准化文档URL" required name="StandardDocUrl">
                        <upload-files v-model="formData.StandardDocUrl"></upload-files>
                    </uni-forms-item>
                    <uni-forms-item label="生成状态：0=待生成，1=生成中，2=已完成，3=生成失败" required name="GenerateStatus">
                        <uni-data-checkbox v-model="formData.GenerateStatus" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="生成失败原因" required name="GenerateFailReason">
                        <uni-easyinput v-model="formData.GenerateFailReason" placeholder="请输入生成失败原因" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="累计查看次数" required name="ViewCount">
                        <uni-number-box :min="0" :max="99999" v-model="formData.ViewCount" />
                    </uni-forms-item>
                    <uni-forms-item label="累计下载次数" required name="DownloadCount">
                        <uni-number-box :min="0" :max="99999" v-model="formData.DownloadCount" />
                    </uni-forms-item>
                    <uni-forms-item label="累计分享次数" required name="ShareCount">
                        <uni-number-box :min="0" :max="99999" v-model="formData.ShareCount" />
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
	HealthRecordId:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	DetectIdList:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	TemplateType:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	ContentScope:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	StandardDocUrl:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	GenerateStatus:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	GenerateFailReason:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	ViewCount:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	DownloadCount:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	ShareCount:{
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

    const { Data } = await Post(`/TUserMedicalPrepareReport/Get`, { Id: Id });
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
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                    let { Data, Success } = await Post("/TUserMedicalPrepareReport/CreateOrEdit", formData);
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
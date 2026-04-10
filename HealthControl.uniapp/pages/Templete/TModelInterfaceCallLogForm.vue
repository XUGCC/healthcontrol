<template>
    <view>
        <uni-nav-bar dark :fixed="true" shadow background-color="var(--primary-color)" status-bar left-icon="left"
            left-text="返回" @clickLeft="goBack" title="模型接口调用日志信息" />

        <uni-section title="请填写模型接口调用日志信息">
            <view class="form-container">

                <uni-forms ref="editModalForm" :model="formData" :rules="editModalFormRules" label-width='100'
                    label-position='top'>
                    <uni-forms-item label="结果更新时间" required name="UpdateTime">
                        <uni-datetime-picker type="datetime" return-type="string" v-model="formData.UpdateTime"
                            primaryColor="var(--primary-color)" />
                    </uni-forms-item>
                    <uni-forms-item label="关联音频自查记录表主键" required name="DetectId">
                        <sigle-select url="/TAudioScreenRecord/List" columnName="AudioUrl" columnValue="Id" v-model="formData.DetectId">
                        </sigle-select>
                    </uni-forms-item>
                    <uni-forms-item label="调用环节" required name="CallLink">
                        <uni-data-checkbox v-model="formData.CallLink" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="模型接口类型" required name="ModelInterfaceType">
                        <uni-data-checkbox v-model="formData.ModelInterfaceType" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="输入图谱URL" required name="InputSpectrumUrl">
                        <upload-files v-model="formData.InputSpectrumUrl"></upload-files>
                    </uni-forms-item>
                    <uni-forms-item label="调用参数" required name="InputParams">
                        <uni-easyinput v-model="formData.InputParams" placeholder="请输入调用参数" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="调用耗时" required name="CallCost">
                        <uni-number-box :min="0" :max="99999" v-model="formData.CallCost" />
                    </uni-forms-item>
                    <uni-forms-item label="返回结果摘要" required name="ResultSummary">
                        <uni-easyinput v-model="formData.ResultSummary" placeholder="请输入返回结果摘要" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="返回完整结果" required name="ResultComplete">
                        <uni-easyinput type="textarea" v-model="formData.ResultComplete" placeholder="请输入返回完整结果" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="调用状态：0=失败，1=成功" required name="CallStatus">
                        <uni-data-checkbox v-model="formData.CallStatus" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="失败错误码" required name="FailErrorCode">
                        <uni-easyinput v-model="formData.FailErrorCode" placeholder="请输入失败错误码" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="失败详细信息" required name="FailDetailInfo">
                        <uni-easyinput v-model="formData.FailDetailInfo" placeholder="请输入失败详细信息" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="软删除标记" required name="IsDelete">
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
	UpdateTime:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	DetectId:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	CallLink:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	ModelInterfaceType:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	InputSpectrumUrl:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	InputParams:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	CallCost:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	ResultSummary:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	ResultComplete:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	CallStatus:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	FailErrorCode:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	FailDetailInfo:{
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

    const { Data } = await Post(`/TModelInterfaceCallLog/Get`, { Id: Id });
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
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                    let { Data, Success } = await Post("/TModelInterfaceCallLog/CreateOrEdit", formData);
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
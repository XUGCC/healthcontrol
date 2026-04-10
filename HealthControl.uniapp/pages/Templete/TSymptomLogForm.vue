<template>
    <view>
        <uni-nav-bar dark :fixed="true" shadow background-color="var(--primary-color)" status-bar left-icon="left"
            left-text="返回" @clickLeft="goBack" title="症状日志信息" />

        <uni-section title="请填写症状日志信息">
            <view class="form-container">

                <uni-forms ref="editModalForm" :model="formData" :rules="editModalFormRules" label-width='100'
                    label-position='top'>
                    <uni-forms-item label="关联用户表主键t_user." required name="UserId">
                        <sigle-select url="/User/List" columnName="Name" columnValue="Id" v-model="formData.UserId">
                        </sigle-select>
                    </uni-forms-item>
                    <uni-forms-item label="关联音频自查记录表主键" required name="DetectId">
                        <sigle-select url="/TAudioScreenRecord/List" columnName="AudioUrl" columnValue="Id" v-model="formData.DetectId">
                        </sigle-select>
                    </uni-forms-item>
                    <uni-forms-item label="症状类型" required name="SymptomType">
                        <uni-data-checkbox v-model="formData.SymptomType" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="症状轻重" required name="SymptomLevel">
                        <uni-data-checkbox v-model="formData.SymptomLevel" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="症状持续时长" required name="SymptomDuration">
                        <uni-easyinput v-model="formData.SymptomDuration" placeholder="请输入症状持续时长" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="症状首次出现时间" required name="SymptomOccurTime">
                        <uni-datetime-picker type="datetime" return-type="string" v-model="formData.SymptomOccurTime"
                            primaryColor="var(--primary-color)" />
                    </uni-forms-item>
                    <uni-forms-item label="症状备注" required name="Remark">
                        <uni-easyinput v-model="formData.Remark" placeholder="请输入症状备注" primaryColor="var(--primary-color)" />
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
	UserId:{
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
	SymptomType:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	SymptomLevel:{
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
	SymptomOccurTime:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	Remark:{
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

    const { Data } = await Post(`/TSymptomLog/Get`, { Id: Id });
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
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                    let { Data, Success } = await Post("/TSymptomLog/CreateOrEdit", formData);
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
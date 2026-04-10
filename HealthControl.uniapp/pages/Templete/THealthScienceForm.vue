<template>
    <view>
        <uni-nav-bar dark :fixed="true" shadow background-color="var(--primary-color)" status-bar left-icon="left"
            left-text="返回" @clickLeft="goBack" title="健康科普信息" />

        <uni-section title="请填写健康科普信息">
            <view class="form-container">

                <uni-forms ref="editModalForm" :model="formData" :rules="editModalFormRules" label-width='100'
                    label-position='top'>
                    <uni-forms-item label="更新时间" required name="UpdateTime">
                        <uni-datetime-picker type="datetime" return-type="string" v-model="formData.UpdateTime"
                            primaryColor="var(--primary-color)" />
                    </uni-forms-item>
                    <uni-forms-item label="创建人ID" required name="CreatorId">
                        <sigle-select url="/User/List" columnName="Name" columnValue="Id" v-model="formData.CreatorId">
                        </sigle-select>
                    </uni-forms-item>
                    <uni-forms-item label="分类ID" required name="CategoryId">
                        <sigle-select url="/THealthScienceCategory/List" columnName="CategoryName" columnValue="Id" v-model="formData.CategoryId">
                        </sigle-select>
                    </uni-forms-item>
                    <uni-forms-item label="科普标题" required name="Title">
                        <uni-easyinput v-model="formData.Title" placeholder="请输入科普标题" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="封面图URL" required name="CoverUrl">
                        <upload-images v-model="formData.CoverUrl">
                        </upload-images>
                    </uni-forms-item>
                    <uni-forms-item label="科普内容" required name="ScienceContent">
                        <uni-easyinput type="textarea" v-model="formData.ScienceContent" placeholder="请输入科普内容" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="阅读量" required name="ReadCount">
                        <uni-number-box :min="0" :max="99999" v-model="formData.ReadCount" />
                    </uni-forms-item>
                    <uni-forms-item label="知识类型" required name="KnowledgeType">
                        <uni-number-box :min="0" :max="99999" v-model="formData.KnowledgeType" />
                    </uni-forms-item>
                    <uni-forms-item label="展示状态" required name="ShowStatus">
                        <uni-number-box :min="0" :max="99999" v-model="formData.ShowStatus" />
                    </uni-forms-item>
                    <uni-forms-item label="软删除标记" required name="IsDelete">
                        <uni-number-box :min="0" :max="99999" v-model="formData.IsDelete" />
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
	CreatorId:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	CategoryId:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	Title:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	CoverUrl:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	ScienceContent:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	ReadCount:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	KnowledgeType:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	ShowStatus:{
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

    const { Data } = await Post(`/THealthScience/Get`, { Id: Id });
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
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                    let { Data, Success } = await Post("/THealthScience/CreateOrEdit", formData);
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
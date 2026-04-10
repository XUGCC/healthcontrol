<template>
    <view>
        <uni-nav-bar dark :fixed="true" shadow background-color="var(--primary-color)" status-bar left-icon="left"
            left-text="返回" @clickLeft="goBack" title="健康科普分类信息" />

        <uni-section title="请填写健康科普分类信息">
            <view class="form-container">

                <uni-forms ref="editModalForm" :model="formData" :rules="editModalFormRules" label-width='100'
                    label-position='top'>
                    <uni-forms-item label="分类名称：如声带保护/疾病科普" required name="CategoryName">
                        <uni-easyinput v-model="formData.CategoryName" placeholder="请输入分类名称：如声带保护/疾病科普" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="分类描述" required name="CategoryDesc">
                        <uni-easyinput v-model="formData.CategoryDesc" placeholder="请输入分类描述" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="排序值：升序，值越小越靠前" required name="SortNum">
                        <uni-number-box :min="0" :max="99999" v-model="formData.SortNum" />
                    </uni-forms-item>
                    <uni-forms-item label="展示状态：0=隐藏，1=展示" required name="ShowStatus">
                        <uni-data-checkbox v-model="formData.ShowStatus" selectedColor="var(--primary-color)"
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
	CategoryName:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	CategoryDesc:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	SortNum:{
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

    const { Data } = await Post(`/THealthScienceCategory/Get`, { Id: Id });
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
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                    let { Data, Success } = await Post("/THealthScienceCategory/CreateOrEdit", formData);
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
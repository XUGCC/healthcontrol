<template>
    <view>
        <uni-nav-bar dark :fixed="true" shadow background-color="var(--primary-color)" status-bar left-icon="left"
            left-text="返回" @clickLeft="goBack" title="耳鼻喉医院医生信息" />

        <uni-section title="请填写耳鼻喉医院医生信息">
            <view class="form-container">

                <uni-forms ref="editModalForm" :model="formData" :rules="editModalFormRules" label-width='100'
                    label-position='top'>
                    <uni-forms-item label="医院名称" required name="HospitalName">
                        <uni-easyinput v-model="formData.HospitalName" placeholder="请输入医院名称" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="医生姓名" required name="DoctorName">
                        <uni-easyinput v-model="formData.DoctorName" placeholder="请输入医生姓名" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="科室类型：0=耳鼻喉科，1=咽喉科，2=头颈外科" required name="DepartmentType">
                        <uni-data-checkbox v-model="formData.DepartmentType" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="所在地区：如北京市海淀区" required name="Region">
                        <uni-easyinput v-model="formData.Region" placeholder="请输入所在地区：如北京市海淀区" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="详细地址" required name="Address">
                        <select-address v-model="formData.Address" @selected="AddressSelected"></select-address>
                    </uni-forms-item>
                    <uni-forms-item label="联系电话" required name="ContactPhone">
                        <uni-easyinput v-model="formData.ContactPhone" placeholder="请输入联系电话" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="预约链接" required name="AppointmentUrl">
                        <upload-files v-model="formData.AppointmentUrl"></upload-files>
                    </uni-forms-item>
                    <uni-forms-item label="展示状态：0=隐藏，1=展示" required name="ShowStatus">
                        <uni-data-checkbox v-model="formData.ShowStatus" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="软删除标记：0=未删除，1=已删除" required name="IsDelete">
                        <uni-data-checkbox v-model="formData.IsDelete" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="创建人ID" required name="CreatorId">
                        <sigle-select url="/User/List" columnName="Name" columnValue="Id" v-model="formData.CreatorId">
                        </sigle-select>
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
	HospitalName:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	DoctorName:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	DepartmentType:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	Region:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	Address:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	ContactPhone:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
      {
			pattern:/^1[34578]\d{9}$/,
			errorMessage: '请输入正确的手机号'
		},    
        	]
    },
	AppointmentUrl:{
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
	CreatorId:{
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

    const { Data } = await Post(`/TOtolaryngologyHospitalDoctor/Get`, { Id: Id });
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
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                    let { Data, Success } = await Post("/TOtolaryngologyHospitalDoctor/CreateOrEdit", formData);
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
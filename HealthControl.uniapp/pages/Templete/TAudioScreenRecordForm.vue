<template>
    <view>
        <uni-nav-bar dark :fixed="true" shadow background-color="var(--primary-color)" status-bar left-icon="left"
            left-text="返回" @clickLeft="goBack" title="音频自查记录信息" />

        <uni-section title="请填写音频自查记录信息">
            <view class="form-container">

                <uni-forms ref="editModalForm" :model="formData" :rules="editModalFormRules" label-width='100'
                    label-position='top'>
                    <uni-forms-item label="结果更新时间" required name="UpdateTime">
                        <uni-datetime-picker type="datetime" return-type="string" v-model="formData.UpdateTime"
                            primaryColor="var(--primary-color)" />
                    </uni-forms-item>
                    <uni-forms-item label="关联用户表主键t_user." required name="UserId">
                        <uni-number-box :min="0" :max="99999" v-model="formData.UserId" />
                    </uni-forms-item>
                    <uni-forms-item label="音频文件URL" required name="AudioUrl">
                        <upload-files v-model="formData.AudioUrl"></upload-files>
                    </uni-forms-item>
                    <uni-forms-item label="音频格式：mp3wav等" required name="AudioFormat">
                        <uni-easyinput v-model="formData.AudioFormat" placeholder="请输入音频格式：mp3wav等" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="音频时长" required name="AudioDuration">
                        <uni-number-box :min="0" :max="99999" v-model="formData.AudioDuration" />
                    </uni-forms-item>
                    <uni-forms-item label="音频采样率" required name="AudioSamplingRate">
                        <uni-number-box :min="0" :max="99999" v-model="formData.AudioSamplingRate" />
                    </uni-forms-item>
                    <uni-forms-item label="音频文件大小" required name="AudioFileSize">
                        <uni-number-box :min="0" :max="99999" v-model="formData.AudioFileSize" />
                    </uni-forms-item>
                    <uni-forms-item label="发音引导类型" required name="PronunciationGuideType">
                        <uni-data-checkbox v-model="formData.PronunciationGuideType" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="MFCC图谱URL" required name="MfccSpectrumUrl">
                        <upload-files v-model="formData.MfccSpectrumUrl"></upload-files>
                    </uni-forms-item>
                    <uni-forms-item label="MFCC图谱分辨率：如256*256" required name="MfccSpectrumResolution">
                        <uni-easyinput v-model="formData.MfccSpectrumResolution" placeholder="请输入MFCC图谱分辨率：如256*256" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="mel图谱URL" required name="MelSpectrumUrl">
                        <upload-files v-model="formData.MelSpectrumUrl"></upload-files>
                    </uni-forms-item>
                    <uni-forms-item label="mel图谱分辨率：如256*256" required name="MelSpectrumResolution">
                        <uni-easyinput v-model="formData.MelSpectrumResolution" placeholder="请输入mel图谱分辨率：如256*256" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="孪生Densenet模型版本" required name="DensenetModelVersion">
                        <uni-easyinput v-model="formData.DensenetModelVersion" placeholder="请输入孪生Densenet模型版本" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="初筛结果：0=良性，1=恶性" required name="PrimaryScreenResult">
                        <uni-data-checkbox v-model="formData.PrimaryScreenResult" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="初筛置信度" required name="PrimaryScreenConfidence">
                        <uni-number-box :min="0" :max="99999" v-model="formData.PrimaryScreenConfidence" />
                    </uni-forms-item>
                    <uni-forms-item label="基频jitter" required name="Jitter">
                        <uni-number-box :min="0" :max="99999" v-model="formData.Jitter" />
                    </uni-forms-item>
                    <uni-forms-item label="基频shimmer" required name="Shimmer">
                        <uni-number-box :min="0" :max="99999" v-model="formData.Shimmer" />
                    </uni-forms-item>
                    <uni-forms-item label="谐噪比HNR" required name="Hnr">
                        <uni-number-box :min="0" :max="99999" v-model="formData.Hnr" />
                    </uni-forms-item>
                    <uni-forms-item label="最长发声时间MPT" required name="Mpt">
                        <uni-number-box :min="0" :max="99999" v-model="formData.Mpt" />
                    </uni-forms-item>
                    <uni-forms-item label="Deepseek接口版本" required name="DeepseekApiVersion">
                        <uni-easyinput v-model="formData.DeepseekApiVersion" placeholder="请输入Deepseek接口版本" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="Deepseek详细报告" required name="DeepseekDetailReport">
                        <uni-easyinput type="textarea" v-model="formData.DeepseekDetailReport" placeholder="请输入Deepseek详细报告" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="Deepseek医学解释摘要" required name="DeepseekMedicalSummary">
                        <uni-easyinput type="textarea" v-model="formData.DeepseekMedicalSummary" placeholder="请输入Deepseek医学解释摘要" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="检测总状态" required name="DetectTotalStatus">
                        <uni-data-checkbox v-model="formData.DetectTotalStatus" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="检测子状态" required name="DetectSubStatus">
                        <uni-data-checkbox v-model="formData.DetectSubStatus" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="图谱生成失败原因" required name="SpectrumFailReason">
                        <uni-easyinput v-model="formData.SpectrumFailReason" placeholder="请输入图谱生成失败原因" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="模型调用失败原因" required name="ModelFailReason">
                        <uni-easyinput v-model="formData.ModelFailReason" placeholder="请输入模型调用失败原因" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="整体失败原因" required name="TotalFailReason">
                        <uni-easyinput v-model="formData.TotalFailReason" placeholder="请输入整体失败原因" primaryColor="var(--primary-color)" />
  	                 </uni-forms-item>
                    <uni-forms-item label="离线状态：0=在线，1=离线" required name="OfflineStatus">
                        <uni-data-checkbox v-model="formData.OfflineStatus" selectedColor="var(--primary-color)"
                            :localdata="[{ text: '是', value: true }, { text: '否', value: false }]" />
                    </uni-forms-item>
                    <uni-forms-item label="关联喉镜照片表主键t_laryngoscope_photo." required name="LaryngoscopePhotoId">
                        <sigle-select url="/TLaryngoscopePhoto/List" columnName="LaryngoscopePhotoUrl" columnValue="Id" v-model="formData.LaryngoscopePhotoId">
                        </sigle-select>
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
	UpdateTime:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	UserId:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	AudioUrl:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	AudioFormat:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	AudioDuration:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	AudioSamplingRate:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	AudioFileSize:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	PronunciationGuideType:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	MfccSpectrumUrl:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	MfccSpectrumResolution:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	MelSpectrumUrl:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	MelSpectrumResolution:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	DensenetModelVersion:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	PrimaryScreenResult:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	PrimaryScreenConfidence:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	Jitter:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	Shimmer:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	Hnr:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	Mpt:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	DeepseekApiVersion:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	DeepseekDetailReport:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	DeepseekMedicalSummary:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	DetectTotalStatus:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	DetectSubStatus:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	SpectrumFailReason:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	ModelFailReason:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	TotalFailReason:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	OfflineStatus:{
      rules:[
      {
			required: true,
			errorMessage: '该项为必填项'
		},  
        
        	]
    },
	LaryngoscopePhotoId:{
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

    const { Data } = await Post(`/TAudioScreenRecord/Get`, { Id: Id });
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
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                  
                    
                    let { Data, Success } = await Post("/TAudioScreenRecord/CreateOrEdit", formData);
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
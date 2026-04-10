<template>
    <div>
        <!-- 搜索表单卡片 -->
        <el-card class="box-card">
            <div slot="header" class="clearfix">
                <el-row>
                    <el-button type="primary" size="default" @click="SearchClick">
                        <el-icon>
                            <Search />
                        </el-icon>查询
                    </el-button>
                    <el-button type="warning" size="default" @click="ResetClick">
                        <el-icon>
                            <Refresh />
                        </el-icon>清空条件
                    </el-button>
                </el-row>
            </div>
            <div class="margin-top-sm">
                <el-form :inline="true" :model="searchForm" size="default">
                    <el-form-item label="MFCC图谱分辨率：如256*256" prop="MfccSpectrumResolution">
                        <el-input v-model.trim="searchForm.MfccSpectrumResolution"  placeholder="请输入MFCC图谱分辨率：如256*256"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="mel图谱分辨率：如256*256" prop="MelSpectrumResolution">
                        <el-input v-model.trim="searchForm.MelSpectrumResolution"  placeholder="请输入mel图谱分辨率：如256*256"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="孪生Densenet模型版本" prop="DensenetModelVersion">
                        <el-input v-model.trim="searchForm.DensenetModelVersion"  placeholder="请输入孪生Densenet模型版本"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="Deepseek接口版本" prop="DeepseekApiVersion">
                        <el-input v-model.trim="searchForm.DeepseekApiVersion"  placeholder="请输入Deepseek接口版本"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="图谱生成失败原因" prop="SpectrumFailReason">
                        <el-input v-model.trim="searchForm.SpectrumFailReason"  placeholder="请输入图谱生成失败原因"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="模型调用失败原因" prop="ModelFailReason">
                        <el-input v-model.trim="searchForm.ModelFailReason"  placeholder="请输入模型调用失败原因"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="整体失败原因" prop="TotalFailReason">
                        <el-input v-model.trim="searchForm.TotalFailReason"  placeholder="请输入整体失败原因"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="结果更新时间" class="search-input">
                        <el-date-picker v-model="searchForm.UpdateTimeRange" type="datetimerange" start-placeholder="开始结果更新时间"
                            end-placeholder="结束结果更新时间" value-format="YYYY-MM-DD HH:mm:ss" >
                        </el-date-picker>                                       
                    </el-form-item>                
 							 <el-form-item label="发音引导类型" class="search-input">
                 		 <el-select v-model="searchForm.PronunciationGuideType" class="search-input" :clearable="true" placeholder="请选择发音引导类型" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="初筛结果：0=良性，1=恶性" class="search-input">
                 		 <el-select v-model="searchForm.PrimaryScreenResult" class="search-input" :clearable="true" placeholder="请选择初筛结果：0=良性，1=恶性" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="检测总状态" class="search-input">
                 		 <el-select v-model="searchForm.DetectTotalStatus" class="search-input" :clearable="true" placeholder="请选择检测总状态" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="检测子状态" class="search-input">
                 		 <el-select v-model="searchForm.DetectSubStatus" class="search-input" :clearable="true" placeholder="请选择检测子状态" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="离线状态：0=在线，1=离线" class="search-input">
                 		 <el-select v-model="searchForm.OfflineStatus" class="search-input" :clearable="true" placeholder="请选择离线状态：0=在线，1=离线" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="软删除标记：0=未删除，1=已删除" class="search-input">
                 		 <el-select v-model="searchForm.IsDelete" class="search-input" :clearable="true" placeholder="请选择软删除标记：0=未删除，1=已删除" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
                </el-form>
            </div>
        </el-card>
<!-- 编辑对话框 -->
        <el-dialog :title="formData.Id ? '修改音频自查记录' : '添加音频自查记录'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
                size="default">             
              <el-row :gutter="10" class="edit-from-body">

                    <el-col :span="24">
                        <el-form-item label="结果更新时间" prop="UpdateTime">
                            <el-date-picker v-model="formData.UpdateTime" align="right" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss">
                            </el-date-picker>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="关联用户表主键t_user." prop="UserId">
                            <el-input-number  v-model="formData.UserId"  placeholder="请输入关联用户表主键t_user."     :clearable="true"  :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="音频文件URL" prop="AudioUrl">
                             <UploadFiles :limit="8"  v-model="formData.AudioUrl"></UploadFiles>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="音频格式：mp3wav等" prop="AudioFormat">
                            <el-input type="text" v-model="formData.AudioFormat"  placeholder="请输入音频格式：mp3wav等"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="音频时长" prop="AudioDuration">
                            <el-input-number  v-model="formData.AudioDuration"  placeholder="请输入音频时长"     :clearable="true"  :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="音频采样率" prop="AudioSamplingRate">
                            <el-input-number  v-model="formData.AudioSamplingRate"  placeholder="请输入音频采样率"     :clearable="true"  :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="音频文件大小" prop="AudioFileSize">
                            <el-input-number  v-model="formData.AudioFileSize"  placeholder="请输入音频文件大小"     :clearable="true"  :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="发音引导类型" prop="PronunciationGuideType">
                       		  <el-switch v-model="formData.PronunciationGuideType"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="MFCC图谱URL" prop="MfccSpectrumUrl">
                             <UploadFiles :limit="8"  v-model="formData.MfccSpectrumUrl"></UploadFiles>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="MFCC图谱分辨率：如256*256" prop="MfccSpectrumResolution">
                            <el-input type="text" v-model="formData.MfccSpectrumResolution"  placeholder="请输入MFCC图谱分辨率：如256*256"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="mel图谱URL" prop="MelSpectrumUrl">
                             <UploadFiles :limit="8"  v-model="formData.MelSpectrumUrl"></UploadFiles>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="mel图谱分辨率：如256*256" prop="MelSpectrumResolution">
                            <el-input type="text" v-model="formData.MelSpectrumResolution"  placeholder="请输入mel图谱分辨率：如256*256"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="孪生Densenet模型版本" prop="DensenetModelVersion">
                            <el-input type="text" v-model="formData.DensenetModelVersion"  placeholder="请输入孪生Densenet模型版本"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="初筛结果：0=良性，1=恶性" prop="PrimaryScreenResult">
                       		  <el-switch v-model="formData.PrimaryScreenResult"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="初筛置信度" prop="PrimaryScreenConfidence">
                            <el-input-number  v-model="formData.PrimaryScreenConfidence"  placeholder="请输入初筛置信度"     :clearable="true"  :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="基频jitter" prop="Jitter">
                            <el-input-number  v-model="formData.Jitter"  placeholder="请输入基频jitter"     :clearable="true"  :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="基频shimmer" prop="Shimmer">
                            <el-input-number  v-model="formData.Shimmer"  placeholder="请输入基频shimmer"     :clearable="true"  :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="谐噪比HNR" prop="Hnr">
                            <el-input-number  v-model="formData.Hnr"  placeholder="请输入谐噪比HNR"     :clearable="true"  :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="最长发声时间MPT" prop="Mpt">
                            <el-input-number  v-model="formData.Mpt"  placeholder="请输入最长发声时间MPT"     :clearable="true"  :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="Deepseek接口版本" prop="DeepseekApiVersion">
                            <el-input type="text" v-model="formData.DeepseekApiVersion"  placeholder="请输入Deepseek接口版本"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="Deepseek详细报告" prop="DeepseekDetailReport">
                            <el-input type="textarea" :rows="5" v-model.trim="formData.DeepseekDetailReport"  placeholder="请输入Deepseek详细报告"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="Deepseek医学解释摘要" prop="DeepseekMedicalSummary">
                            <el-input type="textarea" :rows="5" v-model.trim="formData.DeepseekMedicalSummary"  placeholder="请输入Deepseek医学解释摘要"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="检测总状态" prop="DetectTotalStatus">
                       		  <el-switch v-model="formData.DetectTotalStatus"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="检测子状态" prop="DetectSubStatus">
                       		  <el-switch v-model="formData.DetectSubStatus"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="图谱生成失败原因" prop="SpectrumFailReason">
                            <el-input type="text" v-model="formData.SpectrumFailReason"  placeholder="请输入图谱生成失败原因"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="模型调用失败原因" prop="ModelFailReason">
                            <el-input type="text" v-model="formData.ModelFailReason"  placeholder="请输入模型调用失败原因"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="整体失败原因" prop="TotalFailReason">
                            <el-input type="text" v-model="formData.TotalFailReason"  placeholder="请输入整体失败原因"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="离线状态：0=在线，1=离线" prop="OfflineStatus">
                       		  <el-switch v-model="formData.OfflineStatus"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="关联喉镜照片表主键t_laryngoscope_photo." prop="LaryngoscopePhotoId">
                          <SigleSelect url="/TLaryngoscopePhoto/List" columnName="LaryngoscopePhotoUrl" columnValue="Id"  v-model="formData.LaryngoscopePhotoId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="软删除标记：0=未删除，1=已删除" prop="IsDelete">
                       		  <el-switch v-model="formData.IsDelete"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row type="flex" justify="end" align="bottom">
                    <el-form-item>
                        <el-button size="default" type="primary" plain @click="CreateOrEditForm()">确 定</el-button>
                        <el-button size="default" @click="editorShow = false">取 消</el-button>
                    </el-form-item>
                </el-row>
            </el-form>
        </el-dialog>




        <!-- 数据表格 -->
        <PaginationTable ref="PaginationTableId" url="/TAudioScreenRecord/List" :column="columnList" :where="where">
            <template v-slot:header>
                <el-button type="primary" size="default" @click="ShowEditModal()">
                    <el-icon>
                        <Edit />
                    </el-icon>新 增
                </el-button>
                <el-button type="danger" size="default" @click="BatchDelete">
                    <el-icon>
                        <Delete />
                    </el-icon>批量删除
                </el-button>
                <ExportButton exportUrl="/TAudioScreenRecord/Export" :where="searchForm"></ExportButton>        
            </template>
            <template v-slot:Operate="scope">
              <el-button type="primary" size="default" class="margin-top-xs" @click="ShowEditModal(scope.row.Id)">
                    <el-icon>
                        <Edit />
                    </el-icon>修 改
                </el-button>
                <el-button type="danger" size="default" class="margin-top-xs" @click="ShowDeleteModal(scope.row.Id)">
                    <el-icon>
                        <Delete />
                    </el-icon>删 除
                </el-button>
            </template>
        </PaginationTable>
        
              
              
    </div>
</template>

<script setup>
import { Post,PostSingleUpdate } from '@/api/http';
import { ColumnType } from '@/components/Tables/columnTypes';
import { useCommonStore } from "@/store";

import { Delete, Edit, Refresh, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {onMounted,onBeforeMount,nextTick, watch,computed, reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();
// 计算属性
const Token = computed(() => commonStore.Token)
const UserInfo = computed(() => commonStore.UserInfo)
const RoleType = computed(() => commonStore.RoleType)
const UserId = computed(() => commonStore.UserId)
  
//表格条件
const where = reactive({});
// 搜索表单数据
const searchForm = reactive({});

// 编辑表单数据
const formData = reactive({});

// 编辑对话框显示状态
const editorShow = ref(false);

// 表单引用
const editModalForm = ref(null);

// 表格列配置
const columnList = ref([
  {
    key: "Id",
    hidden: true,

  },
  {
    key: "UpdateTime",
    title: "结果更新时间",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "UserId",
    title: "关联用户表主键t_user.",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "AudioUrl",
    title: "音频文件URL",
    width: "140px",
    type:ColumnType.AUDIO,
  },
  {
    key: "AudioFormat",
    title: "音频格式：mp3wav等",
    width: "140px",
    type:ColumnType.AUDIO,
  },
  {
    key: "AudioDuration",
    title: "音频时长",
    width: "140px",
    type:ColumnType.AUDIO,
  },
  {
    key: "AudioSamplingRate",
    title: "音频采样率",
    width: "140px",
    type:ColumnType.AUDIO,
  },
  {
    key: "AudioFileSize",
    title: "音频文件大小",
    width: "140px",
    type:ColumnType.AUDIO,
  },
  {
    key: "PronunciationGuideType",
    title: "发音引导类型",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "MfccSpectrumUrl",
    title: "MFCC图谱URL",
    width: "140px",
    type:ColumnType.FILESLINK,
  },
  {
    key: "MfccSpectrumResolution",
    title: "MFCC图谱分辨率：如256*256",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "MelSpectrumUrl",
    title: "mel图谱URL",
    width: "140px",
    type:ColumnType.FILESLINK,
  },
  {
    key: "MelSpectrumResolution",
    title: "mel图谱分辨率：如256*256",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "DensenetModelVersion",
    title: "孪生Densenet模型版本",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "PrimaryScreenResult",
    title: "初筛结果：0=良性，1=恶性",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "PrimaryScreenConfidence",
    title: "初筛置信度",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "Jitter",
    title: "基频jitter",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "Shimmer",
    title: "基频shimmer",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "Hnr",
    title: "谐噪比HNR",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "Mpt",
    title: "最长发声时间MPT",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "DeepseekApiVersion",
    title: "Deepseek接口版本",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "DeepseekDetailReport",
    title: "Deepseek详细报告",
    width: "140px",
    type:ColumnType.RICHTEXT,
  },
  {
    key: "DeepseekMedicalSummary",
    title: "Deepseek医学解释摘要",
    width: "140px",
    type:ColumnType.RICHTEXT,
  },
  {
    key: "DetectTotalStatus",
    title: "检测总状态",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "DetectSubStatus",
    title: "检测子状态",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "SpectrumFailReason",
    title: "图谱生成失败原因",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "ModelFailReason",
    title: "模型调用失败原因",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "TotalFailReason",
    title: "整体失败原因",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "OfflineStatus",
    title: "离线状态：0=在线，1=离线",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "LaryngoscopePhotoId",
    hidden: true, 
  },
  {
    key: "LaryngoscopePhotoDto.LaryngoscopePhotoUrl",
    title: "喉镜照片URL",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "IsDelete",
    title: "软删除标记：0=未删除，1=已删除",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    title: "操作",
    width:"300px",
    key: "Operate",
    type: ColumnType.USERDEFINED,
  },
            ]);

// 表单验证规则
const editModalFormRules = reactive({
"UpdateTime":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"UserId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"AudioUrl":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"AudioFormat":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"AudioDuration":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"AudioSamplingRate":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"AudioFileSize":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"PronunciationGuideType":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"MfccSpectrumUrl":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"MfccSpectrumResolution":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"MelSpectrumUrl":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"MelSpectrumResolution":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"DensenetModelVersion":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"PrimaryScreenResult":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"PrimaryScreenConfidence":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"Jitter":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"Shimmer":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"Hnr":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"Mpt":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"DeepseekApiVersion":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"DeepseekDetailReport":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"DeepseekMedicalSummary":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"DetectTotalStatus":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"DetectSubStatus":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"SpectrumFailReason":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ModelFailReason":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"TotalFailReason":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"OfflineStatus":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"LaryngoscopePhotoId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"IsDelete":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
 });


// 表格引用
const PaginationTableId = ref(null);

// 显示编辑对话框
const ShowEditModal = async (Id) => {

    const { Data } = await Post(`/TAudioScreenRecord/Get`, { Id: Id });
    
    Object.assign(formData, Data);

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/TAudioScreenRecord/CreateOrEdit`, formData);

            if (Success) {
                editorShow.value = false;
                PaginationTableId.value.Reload(searchForm);
                ElMessage.success('操作成功');
            }
        }
    });
};

// 搜索点击
const SearchClick = () => {
    PaginationTableId.value.Reload(searchForm);
};

// 重置搜索条件
const ResetClick = () => {
    Object.keys(searchForm).forEach(key => {
        searchForm[key] = undefined;
    });
    PaginationTableId.value.Reload(searchForm);
};

// 显示删除确认框
const ShowDeleteModal = async (Id) => {

    try {
        await ElMessageBox.confirm('确认删除该信息吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        const { Success } = await Post(`/TAudioScreenRecord/Delete`, { Id: Id });
        if (Success) {
            PaginationTableId.value.Reload(searchForm);
            ElMessage.success('删除成功');
        }
    }
    catch (error) {
        ElMessage.warning('用户放弃了操作');
    }
};

// 批量删除
const BatchDelete = async () => {
    const ids = PaginationTableId.value.GetSelectionRow().map(x => x.Id);
    if (ids.length > 0) {
        try {
            await ElMessageBox.confirm('确认删除所选的行数据吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            })


            const { Success } = await Post(`/TAudioScreenRecord/BatchDelete`, { Ids: ids });
            if (Success) {
                PaginationTableId.value.Reload(searchForm);
                ElMessage.success('删除成功');
            }
        }
        catch (error) {
            ElMessage.warning('用户放弃了操作');
        }
    }
    else {
        ElMessage.warning('用户放弃了操作');
    }
};
onBeforeMount(() => {
   
});
</script>

<style scoped>

</style>
<template>
    <div>
        <div id="aiEditor" :style="{ height: height }"></div>
    </div>
</template>

<script setup>
import { PostUpload } from "@/api/http";
import { AiEditor } from "aieditor";
import "aieditor/dist/style.css";
import { nextTick, onMounted, ref, watch } from 'vue';

// 定义props
const props = defineProps({
    modelValue: {
        type: String,
        default: ''
    },
    height: {
        type: String,
        default: '300px'
    }
})

// 定义emit
const emit = defineEmits(['update:modelValue'])

// 定义响应式数据
const uploadUrl = import.meta.env.VITE_API_BASE_URL + "/File/BatchUpload"
const fileList = ref([])
let editor = null

// 初始化编辑器
const initEditor = () => {
    editor = new AiEditor({
        element: "#aiEditor",
        placeholder: "点击输入内容...",
        theme: "light",
        content: props.modelValue,
        contentIsMarkdown: false,
        contentRetention: false,
        contentRetentionKey: 'ai-editor-content',
        draggable: true,
        pasteAsText: false,
        image: {
            uploadUrl: uploadUrl,
            uploader: async (file, uploadUrl, headers, formName) => {
                const formData = new FormData()
                formData.append('file', file)

                let { Success, Data } = await PostUpload(uploadUrl, formData)

                return {
                    "errorCode": 0,
                    "data": {
                        "src": Data[0].Url,
                        "alt": Data[0].FileName,
                        "align": "center",
                        "width": "100%",
                        "height": "auto",
                        "class": "image-class",
                        "loading": true,
                        "data-src": Data[0].Url
                    }
                }
            },
        },
        onChange: (aiEditor) => {
            emit('update:modelValue', aiEditor.getHtml())
        },
        ai: {
            models: {
                spark: {
                    appId: "2e9be894",
                    apiKey: "69b882dc87fb511d0b32198636617d57",
                    apiSecret: "NWE5OTJmZmM1MjAxYjViYTQ4OTdjMzg0",
                    version: "v1.1"
                }
            }
        }
    })
}

// 组件挂载后初始化编辑器
onMounted(() => {
    nextTick(() => {
        initEditor()
    })
})

// 监听modelValue变化
watch(() => props.modelValue, (newValue) => {
    if (editor && newValue !== editor.getHtml()) {
        editor.setHtml(newValue)
    }
})
</script>

<style scoped>
/* 编辑器容器样式 */
#aiEditor {
    border: 1px solid #dcdfe6;
    border-radius: 4px;
}
</style>
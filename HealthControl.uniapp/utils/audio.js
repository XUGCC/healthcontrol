/**
 * 音频上传工具函数
 */

import { GetLoginToken } from './cache'

let baseUrl = import.meta.env.VITE_API_BASE_URL

/**
 * 上传音频文件
 * @param {String} filePath 音频文件路径
 * @param {Function} onProgress 上传进度回调函数
 * @returns {Promise} 返回上传结果
 */
export function UploadAudio(filePath, onProgress) {
    return new Promise((resolve, reject) => {
        // 获取文件信息
        uni.getFileInfo({
            filePath: filePath,
            success: (fileInfo) => {
                // 检查文件大小（最大10MB）
                if (fileInfo.size > 10 * 1024 * 1024) {
                    resolve({
                        success: false,
                        msg: '文件大小不能超过10MB',
                        data: null
                    })
                    return
                }
                
                // 获取token
                const token = GetLoginToken()
                const header = {}
                if (token) {
                    header.Authorization = `Bearer ${token}`
                }
                
                // 开始上传
                const uploadTask = uni.uploadFile({
                    url: baseUrl + '/File/BatchUpload',
                    filePath: filePath,
                    name: 'file',
                    header: header,
                    success: (res) => {
                        try {
                            const data = JSON.parse(res.data)
                            
                            if (data.Success && data.Data && data.Data.length > 0) {
                                const fileResult = data.Data[0]
                                
                                // 获取音频信息
                                const audioContext = uni.createInnerAudioContext()
                                audioContext.src = filePath
                                
                                audioContext.onCanplay(() => {
                                    const duration = Math.floor(audioContext.duration)
                                    const sampleRate = 44100 // 默认采样率
                                    
                                    audioContext.destroy()
                                    
                                    resolve({
                                        success: true,
                                        msg: '上传成功',
                                        data: {
                                            url: fileResult.Url || fileResult.url,
                                            name: fileResult.Name || fileResult.name,
                                            format: getFileFormat(filePath),
                                            size: fileInfo.size,
                                            duration: duration,
                                            sampleRate: sampleRate
                                        }
                                    })
                                })
                                
                                audioContext.onError(() => {
                                    audioContext.destroy()
                                    resolve({
                                        success: true,
                                        msg: '上传成功',
                                        data: {
                                            url: fileResult.Url || fileResult.url,
                                            name: fileResult.Name || fileResult.name,
                                            format: getFileFormat(filePath),
                                            size: fileInfo.size,
                                            duration: 0,
                                            sampleRate: 44100
                                        }
                                    })
                                })
                            } else {
                                resolve({
                                    success: false,
                                    msg: data.Msg || '上传失败',
                                    data: null
                                })
                            }
                        } catch (error) {
                            console.error('解析上传结果失败:', error)
                            resolve({
                                success: false,
                                msg: '解析上传结果失败',
                                data: null
                            })
                        }
                    },
                    fail: (err) => {
                        console.error('上传失败:', err)
                        resolve({
                            success: false,
                            msg: err.errMsg || '上传失败',
                            data: null
                        })
                    }
                })
                
                // 监听上传进度
                if (onProgress && typeof onProgress === 'function') {
                    uploadTask.onProgressUpdate((res) => {
                        const progress = res.progress || 0
                        onProgress(progress)
                    })
                }
            },
            fail: (err) => {
                console.error('获取文件信息失败:', err)
                resolve({
                    success: false,
                    msg: '获取文件信息失败',
                    data: null
                })
            }
        })
    })
}

/**
 * 根据文件路径获取文件格式
 * @param {String} filePath 文件路径
 * @returns {String} 文件格式
 */
function getFileFormat(filePath) {
    const ext = filePath.split('.').pop().toLowerCase()
    const formatMap = {
        'mp3': 'mp3',
        'wav': 'wav',
        'm4a': 'm4a',
        'flac': 'flac',
        'aac': 'aac'
    }
    return formatMap[ext] || 'mp3'
}

/**
 * 获取音频时长
 * @param {String} filePath 音频文件路径
 * @returns {Promise<Number>} 音频时长（秒）
 */
export function getAudioDuration(filePath) {
    return new Promise((resolve, reject) => {
        const audioContext = uni.createInnerAudioContext()
        audioContext.src = filePath
        
        audioContext.onCanplay(() => {
            const duration = Math.floor(audioContext.duration)
            audioContext.destroy()
            resolve(duration)
        })
        
        audioContext.onError((err) => {
            audioContext.destroy()
            reject(err)
        })
    })
}

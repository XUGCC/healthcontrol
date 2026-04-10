/**
 * 小程序 API 配置
 * 真机调试时，请使用内网穿透工具（如 ngrok、natapp）将本地服务映射到公网 HTTPS 域名
 * 
 * 使用说明：
 * 1. 开发者工具调试：保持 tunnel 为空，使用 local 地址
 * 2. 真机调试：配置 tunnel 为内网穿透地址（必须是 HTTPS）
 * 3. 正式发布：配置 production 为生产环境地址
 */

// 开发环境配置
const config = {
  // 本地开发地址（仅用于微信开发者工具，真机无法访问）
  local: 'http://localhost:7245',
  
  // 内网穿透地址（真机调试时使用，需要替换为你自己的内网穿透域名）
  // 示例：'https://abc123.ngrok-free.app' 或 'https://abc123.natapp1.cc'
  // 获取方式：参考「真机调试配置指南.md」
  tunnel: '',
  
  // 生产环境地址（正式发布时使用）
  production: ''
}

/**
 * 获取当前环境的 API 基础地址
 * 优先级：环境变量 > 内网穿透地址 > 本地地址
 */
function getBaseUrl() {
  // 1. 优先使用环境变量（如果已配置）
  if (import.meta.env && import.meta.env.VITE_API_BASE_URL) {
    const envUrl = import.meta.env.VITE_API_BASE_URL
    if (envUrl) {
      return envUrl
    }
  }
  
  // 2. 判断是否为生产环境（仅微信小程序）
  // #ifdef MP-WEIXIN
  try {
    const accountInfo = uni.getAccountInfoSync()
    if (accountInfo && accountInfo.miniProgram) {
      const envVersion = accountInfo.miniProgram.envVersion
      
      // 生产环境：使用 production 配置
      if (envVersion === 'release' && config.production) {
        return config.production
      }
      
      // 开发版/体验版：优先使用内网穿透地址（真机调试必需）
      if (config.tunnel) {
        return config.tunnel
      }
    }
  } catch (e) {
    // 如果获取失败，继续使用默认逻辑
    console.warn('[API配置] 无法获取小程序环境信息:', e)
  }
  // #endif
  
  // 3. 默认使用本地地址（仅开发者工具可用）
  return config.local
}

// 导出配置
export const API_BASE_URL = getBaseUrl()

// 开发提示（仅开发环境）
// #ifdef MP-WEIXIN
console.log('[API配置] 当前API地址:', API_BASE_URL)

if (API_BASE_URL.includes('localhost') || API_BASE_URL.includes('127.0.0.1')) {
  console.warn('[API配置] ⚠️ 警告：当前使用本地地址，真机无法访问！')
  console.warn('[API配置] 请配置内网穿透地址：编辑 utils/config.js，设置 tunnel 字段')
  console.warn('[API配置] 详细步骤请参考：真机调试配置指南.md')
}
// #endif

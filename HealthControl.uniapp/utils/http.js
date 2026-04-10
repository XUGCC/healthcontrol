import { GetLoginToken } from "./cache";
import { API_BASE_URL } from "./config";
//请求后端接口的根路径
let baseUrl = API_BASE_URL;

/**
 * 发送一个post请求给后端
 * @param {Object} url 请求的路径
 * @param {Object} data 请求的数据
 */
export async function Post(url, data) {
  let response = {
    Code: "",
    Data: "",
    Msg: "",
    Success: true,
  };
  let header = {};
  // 直接从本地存储获取token
  const token = GetLoginToken();
  if (token) {
    header = {
      Authorization: `Bearer ${token}`,
    };
  }
  await Request({
    url: baseUrl + url,
    data: data,
    method: "POST",
    header: header,
    dataType: "json",
  })
    .then((res) => {
      response.Success = res.Success;
      response.Msg = res.Msg;
      response.Code = res.Code;
      response.Data = res.Data;
    })
    .catch((err) => {
      response.Success = false;
      response.Msg = err;
    });

  return response;
}

/**
 * 静默POST：不弹 toast（用于“触发检测”等不影响主流程的请求）
 */
export async function PostSilent(url, data) {
  let response = {
    Code: "",
    Data: "",
    Msg: "",
    Success: true,
  };
  let header = {};
  const token = GetLoginToken();
  if (token) {
    header = {
      Authorization: `Bearer ${token}`,
    };
  }
  await RequestSilent({
    url: baseUrl + url,
    data: data,
    method: "POST",
    header: header,
    dataType: "json",
  })
    .then((res) => {
      response.Success = res.Success;
      response.Msg = res.Msg;
      response.Code = res.Code;
      response.Data = res.Data;
    })
    .catch((err) => {
      response.Success = false;
      response.Msg = err;
    });

  return response;
}

/**
 * 更新某个表的字段请求
 * @param {*} getUrl 单个查询后端地址
 * @param {*} saveUrl 保存或者修改后端地址
 * @param {*} id  需要修改的数据id
 * @param {*} content 修改的内容提示
 * @param {*} obj  修改的数据
 * @returns
 */
export async function PostSigleUpdate(getUrl, saveUrl, id, content, obj) {
  let confirm = await uni.showModal({
    title: "提示",
    content: content,
  });

  if (confirm) {
    let { Data } = await PostAsync(getUrl, {
      Id: id,
    });
    Data = {
      ...Data,
      ...obj,
    };
    return await PostAsync(saveUrl, Data);
  }

  return {
    Success: false,
    Msg: "用户取消操作",
  };
}

/**
 * 发送一个post请求给后端 并且返回上一个页面
 * @param {Object} url 请求的路径
 * @param {Object} data 请求的数据
 */
export async function PostReturnPage(url, body) {
  let response = await PostAsync(url, body);
  let { success, data, message } = response;
  if (success) {
    uni.showToast({
      icon: "none",
      title: "操作成功",
      duration: 1500,
    });
    setTimeout(() => {
      uni.navigateBack({
        delta: 1,
      });
    }, 1500);
  } else {
    uni.showToast({
      icon: "error",
      title: message,
      duration: 1500,
    });
  }
  return response;
}

export var Request = function (option) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: option.url,
      data: option.data,
      method: option.method,
      header: option.header,
      dataType: option.dataType,
      success: async (res) => {
        if (res.statusCode == 404) {
          uni.showToast({
            icon: "none",
            duration: 3000,
            title: "请求的资源不存在404!",
          });
          reject("请求的资源不存在404!");
        } else if (res.statusCode == 500) {
          uni.showToast({
            icon: "none",
            duration: 3000,
            title: "服务后端出小差了!",
          });
          reject("服务后端出小差了!");
        } else if (res.statusCode == 200) {
          let {
            data: { Code, Msg, Success },
          } = res;

          if (Code == "401") {
            //如果没有授权 直接丢弃当前Token
            await store.dispatch("TokenLose", {});
            reject("你没有权限访问");
          } else if (Code == "500") {
            uni.showToast({
              icon: "none",
              duration: 3000,
              title: Msg,
            });
            reject(Msg);
          }

          resolve(res.data);
        }
      },
      fail: (err) => {
        console.log(err);
        reject(err);
      },
    });
  });
};

// 静默请求：不进行 toast 提示
export var RequestSilent = function (option) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: option.url,
      data: option.data,
      method: option.method,
      header: option.header,
      dataType: option.dataType,
      success: async (res) => {
        if (res.statusCode == 404) {
          reject("请求的资源不存在404!");
        } else if (res.statusCode == 500) {
          reject("服务后端出小差了!");
        } else if (res.statusCode == 200) {
          let {
            data: { Code, Msg },
          } = res;

          if (Code == "401") {
            reject("你没有权限访问");
          } else if (Code == "500") {
            reject(Msg);
          }

          resolve(res.data);
        }
      },
      fail: (err) => {
        reject(err);
      },
    });
  });
};

export var Upload = function (filePath) {
  return new Promise((resolve, reject) => {
    const uploadTask = uni.uploadFile({
      url: baseUrl + "/File/BatchUpload",
      filePath: filePath,
      name: "file",
      success: (uploadFileRes) => {
        resolve(JSON.parse(uploadFileRes.data));
      },
    });
  });
};

/**
 * 从相机或则拍摄图片
 */
export var UploadImageByCamera = function (count = 1) {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count: count,
      sizeType: ["original", "compressed"],
      sourceType: ["album", "camera"],
      success: async (res) => {
        let { tempFilePaths } = res;
        //循环上传图片
        let fileList = [];
        for (let i = 0; i < tempFilePaths.length; i++) {
          let response = await Upload(tempFilePaths[i]);
          fileList.push(response.Data[0]);
        }
        resolve(fileList);
      },
    });
  });
};

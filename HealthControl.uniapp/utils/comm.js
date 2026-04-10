export function CheckToken() {
  if (!store.getters.Token) {
    uni.navigateTo({
      url: "/pages/Front/Login",
    });
    return false;
  }
  return true;
}
/**
 * 获取路径中的文件名称
 */
export function GetFileNameByPath(path) {
  if (!path) {
    return "";
  }
  var pos1 = path.lastIndexOf("/");
  var pos2 = path.lastIndexOf("\\");
  var pos = Math.max(pos1, pos2);
  if (pos < 0) {
    return path;
  } else {
    return path.substring(pos + 1);
  }
}
/**
 * 获取路径中的文件格式
 */
export function GetFileTypeByPath(path) {
  if (!path) {
    return "";
  }
  var pos = path.lastIndexOf(".");
  if (pos < 0) {
    return "";
  } else {
    return path.substring(pos + 1);
  }
}
/**
 * 切割字段返回一个集合
 * @param {*} value 需要切割字段
 * @param {*} cutting 切割的符号
 */
export function ConvertArray(value = "", cutting = ",") {
  if (!value) {
    return [];
  }
  return value.split(cutting);
}

/**
 * 根据路径获取文件的详细信息
 */
export function FullConvertUrlArray(value = "", cutting = ",") {
  var arr = ConvertArray(value, cutting);

  return arr.map((x) => {
    return {
      url: x,
      name: GetFileNameByPath(x),
      type: GetFileTypeByPath(x),
    };
  });
}
/**
 * 获取对象中指定key的值
 */
export function GetObjectValue(obj, name) {
  if (!name) {
    return undefined;
  }
  if (!obj) {
    return undefined;
  }
  if (name.indexOf(".") != -1) {
    var array = name.split(".");

    var value = obj;
    array.forEach((item) => {
      value = value[`${item}`];
    });
    return value;
  } else {
    return obj[`${name}`];
  }
}
/**
 * 根据某个字段进行去重
 */
export function UniqueBy(arr, key) {
  return arr.reduce((acc, cur) => {
    const existing = acc.find((item) => item[key] === cur[key]);
    if (!existing) {
      acc.push(cur);
    }
    return acc;
  }, []);
}

export function GetFormatFullDate(date) {
  let year = date.getFullYear();
  let month = date.getMonth() + 1;
  let day = date.getDate();
  let hour = date.getHours();
  let minutes = date.getMinutes();
  let seconds = date.getSeconds();
  month = month < 10 ? "0" + month : month;
  day = day < 10 ? "0" + day : day;
  hour = hour < 10 ? "0" + hour : hour;
  minutes = minutes < 10 ? "0" + minutes : minutes;
  seconds = seconds < 10 ? "0" + seconds : seconds;
  let currentDate =
    year + "-" + month + "-" + day + " " + hour + ":" + minutes + ":" + seconds;
  return currentDate;
}

export function GetFormatHMSDate(date) {
  let year = date.getFullYear();
  let month = date.getMonth() + 1;
  let day = date.getDate();
  let hour = date.getHours();
  let minutes = date.getMinutes();
  let seconds = date.getSeconds();
  month = month < 10 ? "0" + month : month;
  day = day < 10 ? "0" + day : day;
  hour = hour < 10 ? "0" + hour : hour;
  minutes = minutes < 10 ? "0" + minutes : minutes;
  seconds = seconds < 10 ? "0" + seconds : seconds;
  let currentDate = hour + ":" + minutes + ":" + seconds;
  return currentDate;
}

export function GetFormatShortDate(date) {
  let year = date.getFullYear();
  let month = date.getMonth() + 1;
  let day = date.getDate();
  let hour = date.getHours();
  let minutes = date.getMinutes();
  let seconds = date.getSeconds();
  month = month < 10 ? "0" + month : month;
  day = day < 10 ? "0" + day : day;
  hour = hour < 10 ? "0" + hour : hour;
  minutes = minutes < 10 ? "0" + minutes : minutes;
  seconds = seconds < 10 ? "0" + seconds : seconds;
  let currentDate = year + "-" + month + "-" + day;

  return currentDate;
}

export function ShowLoading(title = "加载中...") {
  return new Promise((resolve, reject) => {
    uni.showLoading({
      title: title,
      success: resolve(),
      fail: reject(),
    });
  });
}

export function ShowModal(content = "你确定要操作吗") {
  return new Promise((resolve, reject) => {
    uni.showModal({
      title: "提示",
      content: content,
      success: (res) => {
        console.log(res);
        resolve(res.confirm || false);
      },
      fail: (res) => {
        resolve(false);
      },
    });
  });
}

/**
 * 选择图片
 */
export function ChooseImageAsync(count = 1) {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count: count, //默认9
      sizeType: ["original", "compressed"], //可以指定是原图还是压缩图，默认二者都有
      sourceType: ["album", "camera"], //从相册选择
      success: (res) => {
        resolve(res);
      },
    });
  });
}
/**
 * 选择视频
 */
export function ChooseVideoAsync(count = 1) {
  return new Promise((resolve, reject) => {
    uni.chooseVideo({
      sourceType: ["album", "camera"], //从相册选择
      success: (res) => {
        resolve(res);
      },
    });
  });
}
/**
 * 得到登录的code
 */
export function GetLoginCode(count = 1) {
  // #ifdef MP
  return new Promise((resolve, reject) => {
    console.log("aa");
    uni.login({
      provider: "weixin",
      onlyAuthorize: true, // 微信登录仅请求授权认证
      success: function (event) {
        console.log("得到登录的code", event.code);
        resolve(event.code);
      },
      fail: function (err) {
        console.log("获取微信的登录状态code失败", err);
        reject(code);
      },
    });
  });
  // #endif

  // #ifdef H5
  return new Promise((resolve, reject) => {
    console.log("执行");
    resolve(null);
  });
  // #endif
}

export function ReturnPage(page = 1, ms = 1000) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      let pages = getCurrentPages();
      if (pages.length == 1) {
        uni.redirectTo({
          url: "/pages/Front/Index",
        });
      } else {
        uni.navigateBack({
          delta: page,
          success: (res) => {
            resolve(res);
          },
        });
      }
    }, ms);
  });
}

export function GetTimeDifference(date) {
  const now = new Date();
  const diffInMilliseconds = now - date;
  const diffInSeconds = Math.floor(diffInMilliseconds / 1000);
  const diffInMinutes = Math.floor(diffInSeconds / 60);
  const diffInHours = Math.floor(diffInMinutes / 60);
  const diffInDays = Math.floor(diffInHours / 24);
  const diffInMonths = Math.floor(diffInDays / 30);

  if (diffInMonths > 0) {
    return `${diffInMonths}个月前`;
  } else if (diffInDays > 0) {
    return `${diffInDays}天前`;
  } else if (diffInHours > 0) {
    return `${diffInHours}小时前`;
  } else if (diffInMinutes > 0) {
    return `${diffInMinutes}分钟前`;
  } else {
    return "刚刚发表";
  }
}

/**
 * 获取当前经纬度
 */
export function GetCurrentLocation() {
  return new Promise((resolve, reject) => {
    uni.getLocation({
      type: "gcj02", //返回可以用于uni.openLocation的经纬度
      success: function (res) {
        resolve(res);
        console.log(res, "res");
      },
      fail: function (f) {
        reject(f);
      },
    });
  });
}
/**
 * 得到地址并写入缓存
 */
export async function SyncCurrentLocation() {
  console.log("开始获取当前地址信息");
  let location = null;
  await GetCurrentLocation()
    .then(async (res) => {
      if (res.latitude && res.longitude) {
        location = {
          latitude: res.latitude,
          longitude: res.longitude,
        };
        await store.dispatch("SyncLocation", location);
      }
    })
    .catch((f) => {
      uni.showToast({
        icon: "none",
        content: "获取地址失败",
      });
    });
  return location;
}

/**
 * 表单验证
 * @param {Object} formRef
 */
export function formValid(formRef) {
  return new Promise((resolve, reject) => {
    formRef
      .validate()
      .then((res) => {
        //如果验证失败
        if (Array.isArray(res) == true) {
          resolve({
            Success: false,
            Error: res,
          });
        } else {
          resolve({
            Success: true,
            Data: res,
          });
        }
      })
      .catch((err) => {
        resolve({
          Success: false,
          Error: err,
        });
      });
  });
}
/**
 * 二维码扫描
 */
export function ScanCode(page = 1, ms = 1000) {
  return new Promise((resolve, reject) => {
    uni.scanCode({
      success(res) {
        if (res.result) {
          // 扫描成功，处理二维码内容
          console.log("扫描结果：", res.result);

          resolve({
            success: true,
            result: res.result,
          });
        } else {
          // 扫描失败
          uni.showToast({
            title: "扫描失败",
            icon: "none",
          });
          reject({
            success: false,
          });
        }
      },
      fail(f) {
        reject({
          success: false,
        });
      },
    });
  });
}

/**
 * 隐藏手机号
 * @param {string} phoneNumber 手机号
 * @returns {string} 隐藏后的手机号
 */
export function HidePhoneNumber(phoneNumber) {
  if (!phoneNumber || phoneNumber.length != 11) {
    return "*";
  }
  const prefix = phoneNumber.slice(0, 3);
  const suffix = phoneNumber.slice(-4);
  const hiddenPart = "*".repeat(4);
  return prefix + hiddenPart + suffix;
}

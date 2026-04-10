import { ElMessageBox } from "element-plus";
import service from "../utils/axios";

// 通用的post请求
export async function Post(url, data) {
  let response = {
    Code: "",
    Data: "",
    Msg: "",
    Success: true,
  };

  try {
    const res = await service({
      url: url,
      method: "post",
      data: data || {},
    });

    if (res.Data === undefined && res.Data !== 0) {
      res.Data = {};
    }
    response = res;
  } catch (ex) {
    response.Success = false;
    response.Msg = ex;
    response.Data = {};
  }

  return response;
}

// 带确认框的删除请求
export async function PostDelete(url, data) {
  try {
    await ElMessageBox.confirm("你确定要删除吗?", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    return await Post(url, data);
  } catch {
    return { Success: false, Msg: "用户取消操作" };
  }
}

// 更新单个字段的请求
export async function PostSingleUpdate(getUrl, saveUrl, id, content, obj) {
  try {
    await ElMessageBox.confirm(content, "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    const { Data } = await Post(getUrl, { Id: id });
    const updatedData = { ...Data, ...obj };
    return await Post(saveUrl, updatedData);
  } catch {
    return { Success: false, Msg: "用户取消操作" };
  }
}

// 文件上传请求
export async function PostUpload(url, body) {
  let response = {
    Code: "",
    Data: "",
    Msg: "",
    Success: true,
  };

  try {
    const res = await service({
      url: url,
      method: "post",
      headers: {
        "Content-Type": "multipart/form-data",
      },
      data: body || {},
    });
    response = res;
  } catch (ex) {
    response.Success = false;
    response.Msg = ex;
    response.Data = {};
  }

  return response;
}

export default {
  Post,
  PostDelete,
  PostSingleUpdate,
  PostUpload,
};

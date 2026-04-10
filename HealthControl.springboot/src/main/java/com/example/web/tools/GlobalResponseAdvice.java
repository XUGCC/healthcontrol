package com.example.web.tools;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.example.web.tools.dto.ResponseData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
/**
 * 全局响应处理切面
 */
@ControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {



    @Value("${server.port:7245}")
    private String serverPort;

    @Value("${server.ip:http://localhost:7245}")
    private String serverIp;

    public GlobalResponseAdvice() {
    }

    /**
     * 是否开启支持
     *
     * @param returnType    返回的类型
     * @param converterType
     * @return
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 对部分需要直接返回原始内容的接口不做统一封装
        // 1）报告 HTML 预览：/Front/Medical/PrepareReportHtml
        // 2）报告 PDF 导出：/Front/Medical/PrepareReportPdf
        //
        // 说明：这些接口的返回结果会被小程序 web-view 或文件下载能力直接消费，
        // 如果再包一层 ResponseData 外壳，会导致：
        // - HTML 预览页展示成 {"Code":500,"Success":false,...} 这样的 JSON 文本；
        // - 下载到的 PDF 文件内容为 JSON，而不是标准 PDF，表现为“乱码”。
        Class<?> controllerClass = returnType.getContainingClass();
        if (controllerClass != null
                && "FrontMedicalController".equals(controllerClass.getSimpleName())
                && returnType.getMethod() != null) {
            String methodName = returnType.getMethod().getName();
            if ("PrepareReportHtml".equals(methodName) || "PrepareReportPdf".equals(methodName)) {
                return false;
            }
        }
        return true;
    }
 /**
     * 对写入body之前进行拦截拦截处理
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // 对 text/html 这类富文本内容，保持原样返回，避免被包成 JSON 结构
        if (selectedContentType != null && MediaType.TEXT_HTML.includes(selectedContentType)) {
            return body;
        }

        Object result;
        if (body == null) {
            result = ResponseData.OfSuccess();
        } else if (body instanceof ResponseData<?>) {
            result = body;
        } else {
            result = ResponseData.GetResponseDataInstance(body, "成功", true);
        }

        // 处理响应内容中的URL替换（使用反射遍历）
        if (result instanceof ResponseData<?>) {
            try {
                ResponseData<?> responseData = (ResponseData<?>) result;
                Object data = responseData.getData();
                if (data != null) {
                    replaceUrlInObject(data, new HashSet<>());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 递归遍历对象，替换字符串字段中的URL
     */
    private void replaceUrlInObject(Object obj, Set<Object> visited) {
        if (obj == null || visited.contains(obj)) {
            return;
        }
        
        // 避免循环引用
        if (!isSimpleType(obj.getClass())) {
            visited.add(obj);
        }

        String oldUrl = "http://localhost:" + serverPort + "/";

        // 处理集合类型
        if (obj instanceof Collection<?>) {
            for (Object item : (Collection<?>) obj) {
                replaceUrlInObject(item, visited);
            }
            return;
        }

        // 处理Map类型
        if (obj instanceof Map<?, ?>) {
            for (Object value : ((Map<?, ?>) obj).values()) {
                replaceUrlInObject(value, visited);
            }
            return;
        }

        // 处理数组类型
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[]) {
                for (Object item : (Object[]) obj) {
                    replaceUrlInObject(item, visited);
                }
            }
            return;
        }

        // 跳过基本类型和常见不可变类型
        if (isSimpleType(obj.getClass())) {
            return;
        }

        // 遍历对象的所有字段
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(obj);

                    if (fieldValue == null) {
                        continue;
                    }

                    // 如果是String类型，直接替换
                    if (fieldValue instanceof String) {
                        String strValue = (String) fieldValue;
                        if (strValue.contains(oldUrl)) {
                            String newValue = strValue.replace(oldUrl, serverIp + "/");
                            field.set(obj, newValue);
                        }
                    } else {
                        // 递归处理其他对象
                        replaceUrlInObject(fieldValue, visited);
                    }
                } catch (Exception e) {
                    // 忽略无法访问的字段
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * 判断是否为简单类型（不需要递归遍历）
     */
    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == String.class
                || Number.class.isAssignableFrom(clazz)
                || clazz == Boolean.class
                || clazz == Character.class
                || clazz.isEnum();
    }

}
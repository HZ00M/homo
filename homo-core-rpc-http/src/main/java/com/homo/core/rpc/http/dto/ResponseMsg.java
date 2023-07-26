package com.homo.core.rpc.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class ResponseMsg implements Serializable {
    /**
     * 转发消息的结果，200代表成功，其他表示失败
     */
    private Integer code;
    /**
     * 失败原因，code不等于1时msg字段的值有意义
     */
    private String codeDesc;
    /**
     * 消息id
     */
    private String msgId;
    /**
     * 业务返回的结构
     */
    private String msgContent;
}

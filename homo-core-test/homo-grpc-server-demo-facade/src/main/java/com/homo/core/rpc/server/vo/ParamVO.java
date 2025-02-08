package com.homo.core.rpc.server.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ParamVO implements Serializable {
    String strParam = "ok";
    Integer integerParam = 100;
    byte[] bytesParam;
}

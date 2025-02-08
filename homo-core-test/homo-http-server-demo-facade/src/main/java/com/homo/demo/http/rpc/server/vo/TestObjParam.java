package com.homo.demo.http.rpc.server.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestObjParam implements Serializable {
    String strParam = "ok";
    Integer integerParam = 100;
    byte[] bytesParam;
}

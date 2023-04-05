package com.homo.core.facade.tread.tread.enums;

/**
 * 校验结果枚举  业务层可根据返回结果做相应处理逻辑
 * 控制逻辑： 先扣除消耗资源，再增加可获取资源，即sub()函数优先与add()函数，
 * 同类型函数调用按声明顺序依次执行,中途校验失败或出现异常中断执行，已扣除的资源不做返还。
 */
public enum ExecRet {
    ok,//操作成功

    subCheckFail,//扣除资源检查失败: 框架会有默认检查函数,（如int和long类型会默认检查扣除值 扣除值>=0 & 扣除值<=现有值，也可注册业务自己的检查函数
    subError,//扣除资源异常: 扣除阶段SetMethod方法调用异常
    subGetError,//扣除资源Get方法调用异常
    subSetError,//扣除资源Set方法调用异常
    subCheckError,//扣除资源检查异常: 扣除资源校验阶段调用异常

    addCheckFail,//增加资源增加失败 框架会有默认检查函数,（如int和long类型会默认检查增加值 增加值>0），也可注册业务自己的检查函数
    addError,//增加资源异常：增加阶段SetMethod方法调用异常
    addGetError,//增加资源Get方法调用异常
    addSetError,//增加资源Set方法调用异常
    addCheckError,//增加资源检查异常：增加资源校验阶段调用异常

    createObjError,//创建对象异常
    getObjError,//获取对象异常
    setObjError,//设置对象异常

    sysError,   //系统异常：框架异常
}

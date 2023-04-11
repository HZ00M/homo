package com.homo.core.tread.processor;

/**
 * 校验结果枚举  业务层可根据返回结果做相应处理逻辑
 * 控制逻辑： 先扣除消耗资源，再增加可获取资源，即sub()函数优先与add()函数，
 * 同类型函数调用按声明顺序依次执行,中途校验失败或出现异常中断执行，已扣除的资源不做返还。
 */
public enum OpRet {
    sysError,           //功能异常
    afterGetCheckFail,  //afterGetCheck方法检查失败
    beforeSetCheckFail, //beforeSetCheck方法检查失败
    checkFail,          //ResourceCheckMethod检查失败
    checkError,         //ResourceCheckMethod调用异常
    getError,           //ResourceGetMethod调用异常
    setError,           //ResourceSetMethod调用异常
    ok,                 //执行成功
}

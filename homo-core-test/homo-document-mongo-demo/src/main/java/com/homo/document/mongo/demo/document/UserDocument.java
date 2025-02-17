package com.homo.document.mongo.demo.document;

import com.homo.core.facade.document.Document;
import io.homo.proto.document.demo.UserInfoPb;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import java.io.Serializable;

@Slf4j
@BsonDiscriminator
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collectionName = "user", indexes = {"value.nickName", "value.bindGameList.gameName", "value.level", "value.registerTime", "value.tagList"})
public class UserDocument implements Serializable {
    private String userId;
    private String phone;
    private String nickName;
    private Integer age;
    private Integer sex;                                                //性别  0男 1女
    private String sign;
    private String birthday;
    private String avatar;
    private String identityId;                                          //身份证
    private String identityIdName;                                      //身份证姓名
    private Integer level;                                              //会员等级
    private Double totalCharge;                                         //总充值金额
    private Integer groupValue;                                         //成长值
    private String imei;                                                //imei
    private String platform;                                            //系统类型 ios or android
    private String deviceType;                                          //手机型号
    private String token;                                               //登陆token

    private Long registerTime;                                          //注册时间
    private Long inMemberTime;                                          //成为会员时间
    private Long lastLoginTime;                                         //最后一次登陆时间
    private Long lastChargeTime;                                        //最后一次充值时间

    private Long deadlineDay; // 成长值最后期限

    public static UserInfoPb covertUserInfoToProto(UserDocument retUserDocument) {
        return UserInfoPb.newBuilder()
                .setUserId(retUserDocument.getUserId())
                .setAge(retUserDocument.getAge())
                .build();
    }

    public static UserDocument createUser(String userId) {
        UserDocument userDocument = new UserDocument();
        userDocument.setUserId(userId);
        userDocument.setAge(100);
        userDocument.setNickName("guest");
        userDocument.setSex(0);
        userDocument.setRegisterTime(System.currentTimeMillis());
        userDocument.setLastLoginTime(System.currentTimeMillis());
        userDocument.setLastChargeTime(System.currentTimeMillis());
        userDocument.setInMemberTime(System.currentTimeMillis());
        userDocument.setTotalCharge(0.0);
        userDocument.setGroupValue(0);

        return userDocument;
    }

    //省略逻辑代码
}

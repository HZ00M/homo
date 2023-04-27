package com.homo.core.entity.server.test;

import com.homo.core.entity.facade.IServerEntity;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.entity.test.TestEntityRequest;
import io.homo.proto.entity.test.TestEntityResponse;


public class ServerEntity implements IServerEntity {

    @Override
    public Homo<TestEntityResponse> login(TestEntityRequest request) {
        TestEntityResponse success = TestEntityResponse.newBuilder().setCode(1).build();
        return Homo.result(success);
    }
}

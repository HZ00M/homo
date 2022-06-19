package com.homo.core.facade.storege.landing;

import com.homo.core.common.pojo.DataObject;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Map;

public interface DataLandingProcess {

    Map<String, List<DataObject>> processBatch(List<String> dataList);

    Tuple2<String, DataObject> processOne(String dirtyField,String option);
}

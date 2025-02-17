package com.homo.core.storage.test;

import com.homo.core.facade.storege.SaveObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TestSaveObj implements SaveObject{
    public String ownerId;
    public String logicType;

}

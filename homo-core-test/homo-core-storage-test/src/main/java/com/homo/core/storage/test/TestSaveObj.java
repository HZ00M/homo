package com.homo.core.storage.test;

import com.homo.core.facade.storege.SaveObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

;


@Log4j2
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TestSaveObj implements SaveObject{
    public String ownerId;
    public String logicType;

}

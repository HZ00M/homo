package com.homo.core.facade.storege.dirty;

import java.util.Map;

public interface Dirty {
    String key();

    Map<String,String> dirtyMap();

}

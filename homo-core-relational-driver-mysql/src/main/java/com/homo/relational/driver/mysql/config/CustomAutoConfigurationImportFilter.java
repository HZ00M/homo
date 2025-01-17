package com.homo.relational.driver.mysql.config;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CustomAutoConfigurationImportFilter implements AutoConfigurationImportFilter {

    private static final Set<String> EXCLUDED_CLASSES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration"
            ))
    );
    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] matches = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            matches[i] = !EXCLUDED_CLASSES.contains(autoConfigurationClasses[i]);
        }
        return matches;
    }
}

package com.homo.core.utils.log;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.homo.core.utils.apollo.ConfigDriver;
import com.homo.core.utils.module.SupportModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.Set;

@Slf4j
public class HomoLogHandler implements SupportModule {
    @Value("${logging.config.namespace:application}")
    private String loggingNamespace;
    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private LoggingSystem loggingSystem;
    private Config loggingConfig;
    private static final String LOGGING_LEVEL_PREFIX = "logging.level.";

    @Override
    public void moduleInit() {
        loggingConfig = ConfigService.getConfig(loggingNamespace);
        loggingConfig.addChangeListener(new ConfigChangeListener() {
            @Override
            public void onChange(ConfigChangeEvent changeEvent) {
                for (String key : changeEvent.changedKeys()) {
                    if (key.startsWith(LOGGING_LEVEL_PREFIX)) {
                        ConfigChange change = changeEvent.getChange(key);
                        final String newValue = change.getNewValue();
                        switch (change.getChangeType()) {
                            case ADDED:
                                setLoggingLevel(key, newValue);
                                break;
                            case DELETED:
                                setLoggingLevel(key, null);
                                break;
                            case MODIFIED:
                                setLoggingLevel(key, newValue);
                                break;
                            default:
                        }
                    }
            }
        }});
        refreshLoggingLevel();
        log.info("HomoLogHandler init finish");
    }

    public void refreshLoggingLevel() {
        Set<String> propertyNames = loggingConfig.getPropertyNames();
        for (String propertyName : propertyNames) {
            if (propertyName.startsWith(LOGGING_LEVEL_PREFIX)) {
                String loggerName = propertyName.substring(LOGGING_LEVEL_PREFIX.length());
                String level = loggingConfig.getProperty(propertyName, null);
                if (level != null) {
                    setLoggingLevel(loggerName, level);
                }
            }
        }
    }

    private String setLoggingLevel(String key, String value) {
        log.info("setLoggingLevel key {} value {}", key, value);
        try {
            if (value == null) {
                value = LogLevel.INFO.name();
            }
            String loggerName = key.replaceFirst(LOGGING_LEVEL_PREFIX, "");
            if (loggerName.equals("root")) {
                loggerName = null;
            }
            //null可用于删除记录器的任何自定义级别，并使用默认配置
            loggingSystem.setLogLevel(loggerName, LogLevel.valueOf(value.toUpperCase()));
            return loggerName;
        } catch (Exception e) {
            log.error("setLoggingLevel error key {} value {}", key, value, e);
            return "";
        }
    }
}

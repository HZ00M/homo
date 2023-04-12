package com.homo.core.utils.concurrent.schedule;

public interface TaskFun extends Task{
     void run(Object... objects);
}

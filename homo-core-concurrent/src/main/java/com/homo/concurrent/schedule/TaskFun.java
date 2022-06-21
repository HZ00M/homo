package com.homo.concurrent.schedule;

public interface TaskFun extends Task{
     void run(Object... objects);
}

package com.t13max.kdb.bean;

import kotlinx.coroutines.Job;

/**
 * @author t13max
 * @since 16:59 2025/7/7
 */
public interface IData {

    long getId();

    <V extends IData> V setJob(Job job);
}

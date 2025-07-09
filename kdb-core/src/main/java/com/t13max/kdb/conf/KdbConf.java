package com.t13max.kdb.conf;

import lombok.Data;

import java.util.List;

/**
 * @author t13max
 * @since 10:24 2025/7/8
 */
@Data
public class KdbConf {

    private List<TableConf> tableConfList;

    private List<BeanConf> beanConfList;
}

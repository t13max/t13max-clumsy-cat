package com.t13max.kdb.data;

import com.t13max.kdb.bean.IData;
import lombok.Data;

/**
 * @author t13max
 * @since 13:19 2025/8/15
 */
@Data
public class ExampleData implements IData {

    private long id;

    private String name;

    private long roomId;


}

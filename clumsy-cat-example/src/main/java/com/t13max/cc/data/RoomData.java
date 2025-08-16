package com.t13max.cc.data;

import com.t13max.cc.bean.AutoData;

/**
 * @author t13max
 * @since 13:19 2025/8/15
 */
public class RoomData extends AutoData {

    private long id;

    private String name;

    private long uid;


    public RoomData() {
    }

    public RoomData(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
}

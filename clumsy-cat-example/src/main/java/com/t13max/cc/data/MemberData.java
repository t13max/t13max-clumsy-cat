package com.t13max.cc.data;

import com.t13max.cc.bean.AutoData;

/**
 * @author t13max
 * @since 13:19 2025/8/15
 */
public class MemberData extends AutoData {

    private long id;

    private String name;

    private long roomId;


    public MemberData() {
    }

    public MemberData(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public MemberData(long id, String name, long roomId) {
        this.id = id;
        this.name = name;
        this.roomId = roomId;
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

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }
}

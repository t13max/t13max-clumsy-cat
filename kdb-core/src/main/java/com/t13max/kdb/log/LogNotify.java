package com.t13max.kdb.log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author t13max
 * @since 11:21 2025/7/8
 */
public class LogNotify {

    private final List<LogKey> path = new ArrayList<>();
    private Note note;
    private AddRemoveInfo addRemoveInfo;

    public LogNotify(LogKey logkey, Note note) {
        path.add(logkey);
        this.note = note;
    }

    public LogNotify(AddRemoveInfo addRemoveInfo) {
        this.addRemoveInfo = addRemoveInfo;
    }

    public boolean isLast() {
        return path.isEmpty();
    }

    public LogKey pop() {
        return path.remove(path.size() - 1);
    }

    public LogNotify push(LogKey logkey) {
        path.add(logkey);
        return this;
    }

    public boolean isAddRemove() {
        return (null != addRemoveInfo);
    }

    public AddRemoveInfo getAddRemoveInfo() {
        return addRemoveInfo;
    }

    public Note getNote() {
        return note;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = path.size() - 1; i >= 0; --i)
            sb.append('.').append(path.get(i).getVarName());
        sb.append('=').append(note);
        return sb.toString();
    }
}

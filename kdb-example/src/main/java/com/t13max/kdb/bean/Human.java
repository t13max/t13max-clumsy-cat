package com.t13max.kdb.bean;

import com.t13max.kdb.log.IVarLog;
import com.t13max.kdb.log.IntLog;
import com.t13max.kdb.log.LogKey;
import com.t13max.kdb.log.StringLog;
import com.t13max.kdb.utils.VarLogUtils;

/**
 * 玩家对象 自动生成
 *
 * @author t13max
 * @since 15:39 2025/7/15
 */
public class Human extends AutoData {

    private long id;

    private String name;

    private int level;

    private int exp;

    @Override
    public long getId() {
        return id;
    }

    public String getName() {
        verify();
        return name;
    }

    public void setName(String _name) {
        verify();
        if (this.name.equals(_name)) {
            return;
        }

        VarLogUtils.varChangeLog(job, new LogKey(this, "name") {
            @Override
            public IVarLog<String> create() {
                return new StringLog(this, name) {
                    @Override
                    public void rollback() {
                        name = saved;
                    }

                    @Override
                    public boolean checkNoChange(String s) {
                        return s.equals(name);
                    }
                };
            }
        });

        this.name = _name;
        update();
    }

    public int getLevel() {
        verify();
        return level;
    }

    public void setLevel(int _level) {
        verify();
        if (this.level == _level) {
            return;
        }

        VarLogUtils.varChangeLog(job, new LogKey(this, "level") {
            @Override
            public IVarLog<Integer> create() {
                return new IntLog(this, level) {
                    @Override
                    public void rollback() {
                        level = saved;
                    }

                    @Override
                    public boolean checkNoChange(Integer v) {
                        return level == v;
                    }
                };
            }
        });

        this.level = _level;
        update();
    }

    public int getExp() {
        verify();
        return exp;
    }

    public void setExp(int _exp) {
        verify();
        if (this.exp == _exp) {
            return;
        }
        VarLogUtils.varChangeLog(job, new LogKey(this, "exp") {
            @Override
            public IVarLog<Integer> create() {
                return new IntLog(this, exp) {
                    @Override
                    public void rollback() {
                        exp = saved;
                    }

                    @Override
                    public boolean checkNoChange(Integer v) {
                        return exp == v;
                    }
                };
            }
        });
        this.exp = _exp;
        update();
    }
}

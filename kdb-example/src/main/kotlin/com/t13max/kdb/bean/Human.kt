package com.t13max.kdb.bean

import com.t13max.kdb.log.IVarLog
import com.t13max.kdb.log.IntLog
import com.t13max.kdb.log.LogKey
import com.t13max.kdb.log.StringLog
import com.t13max.kdb.utils.VarLogUtils

/**
 *
 * @author t13max
 * @since 12:52 2025/8/11
 */
class Human {

    private val id: Long = 0

    private var name: String? = null

    private var level = 0

    private var exp = 0

    fun getId(): Long {
        return id
    }

    fun getName(): String {
        verify()
        return name!!
    }

    fun setName(_name: String) {
        verify()
        if (this.name == _name) {
            return
        }

        VarLogUtils.varChangeLog<IData?>(job, object : LogKey(this, "name") {
            override fun create(): IVarLog<String?> {
                return object : StringLog(this, name) {
                    override fun rollback() {
                        name = saved
                    }

                    override fun checkNoChange(s: String): Boolean {
                        return s == name
                    }
                }
            }
        })

        this.name = _name
        update()
    }

    fun getLevel(): Int {
        verify()
        return level
    }

    fun setLevel(_level: Int) {
        verify()
        if (this.level == _level) {
            return
        }

        VarLogUtils.varChangeLog<IData?>(job, object : LogKey(this, "level") {
            override fun create(): IVarLog<Int?> {
                return object : IntLog(this, level) {
                    override fun rollback() {
                        level = saved
                    }

                    override fun checkNoChange(v: Int): Boolean {
                        return level == v
                    }
                }
            }
        })

        this.level = _level
        update()
    }

    fun getExp(): Int {
        verify()
        return exp
    }

    fun setExp(_exp: Int) {
        verify()
        if (this.exp == _exp) {
            return
        }
        VarLogUtils.varChangeLog<IData?>(job, object : LogKey(this, "exp") {
            override fun create(): IVarLog<Int?> {
                return object : IntLog(this, exp) {
                    override fun rollback() {
                        exp = saved
                    }

                    override fun checkNoChange(v: Int): Boolean {
                        return exp == v
                    }
                }
            }
        })
        this.exp = _exp
        update()
    }
}
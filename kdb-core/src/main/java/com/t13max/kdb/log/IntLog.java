package com.t13max.kdb.log;


import com.t13max.kdb.utils.VarLogUtils;

public abstract class IntLog extends Note implements IVarLog<Integer> {

	protected LogKey logkey;

	protected int saved;

	protected IntLog(LogKey logkey, int saved) {
		this.logkey = logkey;
		this.saved = saved;
	}

	@Override
	public void commit() {
		VarLogUtils.logNotify(logkey.getXBean(), new LogNotify(logkey, this));
	}

	@Override
	public String toString() {
		return String.valueOf(this.saved);
	}
}

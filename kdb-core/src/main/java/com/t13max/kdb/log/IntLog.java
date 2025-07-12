package com.t13max.kdb.log;


public abstract class IntLog extends Note implements IVarLog<Integer> {

	protected LogKey logkey;

	protected int saved;

	protected IntLog(LogKey logkey, int saved) {
		this.logkey = logkey;
		this.saved = saved;
	}

	@Override
	public void commit() {

	}

	@Override
	public String toString() {
		return String.valueOf(this.saved);
	}
}

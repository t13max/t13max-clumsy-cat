package com.t13max.kdb.log;


public abstract class LongLog extends Note implements IVarLog<Long> {

	protected LogKey logkey;

	protected long saved;

	protected LongLog(LogKey logkey, long saved) {
		this.logkey = logkey;
		this.saved = saved;
	}

	@Override
	public void commit() {
		logkey.getBean().onChange();
	}
}

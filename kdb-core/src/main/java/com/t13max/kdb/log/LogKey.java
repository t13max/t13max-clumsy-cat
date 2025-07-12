package com.t13max.kdb.log;

import com.t13max.kdb.bean.Bean;

public class LogKey {

	private final Bean bean;
	private final String varName;

	public LogKey(Bean bean, String varName) {
		this.bean = bean;
		this.varName = varName;
	}

	protected IVarLog create() {
		return null;
	}

	public final Bean getBean() {
		return bean;
	}

	public final String getVarName() {
		return varName;
	}

}

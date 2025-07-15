package com.t13max.kdb.log;

import com.t13max.kdb.bean.IData;

public class LogKey {

	private final IData data;
	private final String varName;

	public LogKey(IData data, String varName) {
		this.data = data;
		this.varName = varName;
	}

	public IVarLog<?> create(){
		return null;
	}

	public final IData getData() {
		return data;
	}

	public final String getVarName() {
		return varName;
	}

}

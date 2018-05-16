package com.xxl.job.executor.util;

import com.ustcinfo.tpc.framework.core.util.ReadProperties;

import ch.qos.logback.core.PropertyDefinerBase;

public class LogbackContainerId extends PropertyDefinerBase{

	@Override
	public String getPropertyValue() {
		String path = ReadProperties.readProperties("application.properties", "dockerPath");
		return ReadProperties.readFile(path, "");
	}

}

package com.playground.osgi.integration.coverage.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.playground.osgi.integration.coverage.service.HelloService;

public class HelloServiceImpl implements HelloService {

	private static final Logger LOG = LoggerFactory
			.getLogger(HelloServiceImpl.class);

	public void sayHello() {
		LOG.info("Service says 'Hello'!");
	}

}

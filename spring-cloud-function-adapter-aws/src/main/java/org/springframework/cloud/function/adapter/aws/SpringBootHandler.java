/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.function.adapter.aws;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.ClassUtils;

/**
 * @author Dave Syer
 *
 */
public class SpringBootHandler extends AbstractFunctionInvokingEventHandler<Object> {

	private static Log logger = LogFactory.getLog(SpringBootHandler.class);
	
	public static void main(String[] args) {
		new SpringBootHandler();
	}

	public SpringBootHandler() {
		super(getStartClass());
	}

	public SpringBootHandler(Class<?> startClass) {
		super(startClass);
	}

	@Override
	protected Object convertEvent(Object event) {
		return event;
	}

	private static Class<?> getStartClass() {
		ClassLoader classLoader = SpringBootHandler.class.getClassLoader();
		if (System.getenv("MAIN_CLASS")!=null) {
			return ClassUtils.resolveClassName(System.getenv("MAIN_CLASS"), classLoader);
		}
		try {
			Class<?> result = getStartClass(
					classLoader.getResources("META-INF/MANIFEST.MF"));
			if (result == null) {
				result = getStartClass(classLoader.getResources("meta-inf/manifest.mf"));
			}
			logger.info("Main class: " + result);
			return result;
		}
		catch (Exception ex) {
			logger.error("Failed to find main class", ex);
			return null;
		}
	}

	private static Class<?> getStartClass(Enumeration<URL> manifestResources) {
		ArrayList<URL> list = Collections.list(manifestResources);
		logger.info("Searching manifests: " + list);
		for (URL url : list) {
			try {
				logger.info("Searching manifest: " + url);
				InputStream inputStream = url.openStream();
				try {
					Manifest manifest = new Manifest(inputStream);
					String startClass = manifest.getMainAttributes()
							.getValue("Start-Class");
					if (startClass != null) {
						return ClassUtils.forName(startClass,
								SpringBootHandler.class.getClassLoader());
					}
				}
				finally {
					inputStream.close();
				}
			}
			catch (Exception ex) {
			}
		}
		return null;
	}

}

/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.function.adapter.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.amazonaws.services.lambda.runtime.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.function.registry.FunctionCatalog;
import org.springframework.context.ConfigurableApplicationContext;

import reactor.core.publisher.Flux;

/**
 * @author Mark Fisher
 */
public abstract class AbstractFunctionInvokingEventHandler<E> {

	private static Log logger = LogFactory.getLog(SpringBootHandler.class);
	
	private final Class<?> configurationClass;

	private Function<Flux<?>, Flux<?>> function;

	private AtomicBoolean initialized = new AtomicBoolean();

	private ConfigurableApplicationContext context;

	public AbstractFunctionInvokingEventHandler(Class<?> configurationClass) {
		this.configurationClass = configurationClass;
	}

	public Object handleEvent(E event, Context context) {
		if (this.initialized.compareAndSet(false, true)) {
			initialize();
		}
		Object input = convertEvent(event);
		Flux<?> output = this.function.apply(extract(input));
		return result(output);
	}

	private Object result(Flux<?> output) {
		List<Object> result = new ArrayList<>();
		for (Object value : output.toIterable()) {
			result.add(value);
		}
		return result;
	}

	private Flux<?> extract(Object input) {
		if (input instanceof Collection) {
			return Flux.fromIterable((Iterable<?>) input);
		}
		return Flux.just(input);
	}

	protected abstract Object convertEvent(E event);

	@SuppressWarnings("unchecked")
	private void initialize() {
		logger.info("Initializing: " + configurationClass);
		SpringApplicationBuilder builder = new SpringApplicationBuilder(
				configurationClass);
		this.context = builder.web(false).run();
		if (this.context.getBeanNamesForType(FunctionCatalog.class).length == 1) {
			FunctionCatalog catalog = this.context.getBean(FunctionCatalog.class);
			this.function = catalog.lookupFunction("function");
		}
		else {
			this.function = this.context.getBean("function", Function.class);
		}
	}
}

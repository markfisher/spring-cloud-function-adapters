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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.amazonaws.services.lambda.runtime.Context;

import reactor.core.publisher.Flux;

/**
 * @author Mark Fisher
 */
public abstract class AbstractFunctionInvokingEventHandler<E> {

	private final Class<?> configurationClass;

	private Function<Flux<String>, Flux<String>> function;

	private AtomicBoolean initialized = new AtomicBoolean();

	private ConfigurableApplicationContext context;

	public AbstractFunctionInvokingEventHandler(Class<?> configurationClass) {
		this.configurationClass = configurationClass;
	}

	public void handleEvent(E event, Context context) {
		if (!this.initialized.compareAndSet(false, true)) {
			initialize();
		}
		String input = convertEvent(event);
		Flux<String> output = this.function.apply(Flux.just(input));
		output.subscribe(System.out::println);
	}

	protected abstract String convertEvent(E event);

	@SuppressWarnings("unchecked")
	private void initialize() {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(configurationClass);
		this.context = builder.web(false).run();
		this.function = this.context.getBean("function", Function.class);
	}
}

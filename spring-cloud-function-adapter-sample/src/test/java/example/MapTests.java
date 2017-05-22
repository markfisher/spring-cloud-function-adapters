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

package example;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import org.springframework.cloud.function.adapter.aws.SpringBootHandler;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dave Syer
 *
 */
public class MapTests {

	@Test
	public void test() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("one", "foo");
		map.put("two", "bar");
		Map<String, Object> result = new Config(new Properties()).function().apply(map);
		assertThat(result).containsValue("FOO");
	}

	@Test
	public void start() throws Exception {
		new SpringBootHandler(Config.class)
				.handleEvent(Collections.singletonMap("name", "foo"), null);
	}

}

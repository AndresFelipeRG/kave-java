/**
 * Copyright 2016 Simon Reuß
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package cc.kave.commons.pointsto.stores;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import cc.recommenders.names.ITypeName;
import cc.recommenders.usages.Usage;

public class NopUsageStore implements UsageStore {

	@Override
	public void store(Collection<Usage> usages, Path relativeInput) throws IOException {

	}

	@Override
	public Set<ITypeName> getAllTypes() {
		return Collections.emptySet();
	}

	@Override
	public List<Usage> load(ITypeName type) throws IOException {
		return Collections.emptyList();
	}

	@Override
	public List<Usage> load(ITypeName type, Predicate<Usage> filter) throws IOException {
		return Collections.emptyList();
	}

	@Override
	public void flush() throws IOException {

	}

	@Override
	public void close() throws IOException {

	}

}

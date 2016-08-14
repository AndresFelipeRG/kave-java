/**
 * Copyright 2014 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.kave.commons.model.events;

import java.time.ZonedDateTime;

import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

// TODO use correct types instead of just "String"
public abstract class IDEEvent implements IIDEEvent {

	public String IDESessionUUID;

	public String KaVEVersion;

	@Nullable
	public ZonedDateTime TriggeredAt;

	@Override
	public ZonedDateTime getTriggeredAt() {
		return TriggeredAt;
	}

	@Override
	public ZonedDateTime getTerminatedAt() {
		throw new RuntimeException("not implemented yet");
	}

	public Trigger TriggeredBy;

	@Nullable
	public String Duration;

	public String ActiveWindow;
	public String ActiveDocument;

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
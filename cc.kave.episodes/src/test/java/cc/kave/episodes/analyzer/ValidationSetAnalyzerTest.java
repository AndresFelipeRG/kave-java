/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.kave.episodes.analyzer;

import static cc.recommenders.testutils.LoggerUtils.assertLogContains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedList;
import java.util.zip.ZipException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;

import cc.kave.commons.model.episodes.Event;
import cc.kave.episodes.analyzer.ValidationSetAnalyzer;
import cc.kave.episodes.mining.reader.MappingParser;
import cc.kave.episodes.mining.reader.ValidationContextsParser;
import cc.kave.episodes.model.Episode;
import cc.recommenders.io.Logger;

public class ValidationSetAnalyzerTest {

	@Mock
	private MappingParser mappingParser;
	@Mock
	private ValidationContextsParser validationParser;
	
	private static final int REPOS = 2;
	
	private LinkedList<Event> events;
	
	private ValidationSetAnalyzer sut;
	
	@Before
	public void setup() throws ZipException, IOException {
		Logger.reset();
		Logger.setCapturing(true);
		
		MockitoAnnotations.initMocks(this);
		
		events = new LinkedList<Event>();
		
		sut = new ValidationSetAnalyzer(mappingParser, validationParser);
		
		when(mappingParser.parse(REPOS)).thenReturn(events);
		when(validationParser.parse(events)).thenReturn(Sets.newHashSet(createTarget("11"),
				createTarget("11", "12", "11>12"), createTarget("11", "13", "11>13"),
				createTarget("11", "12", "13", "11>12", "11>13", "12>13")));
	}
	
	@After
	public void teardown() {
		Logger.reset();
	}
	
	@Test
	public void logTest() throws ZipException, IOException {
		Logger.clearLog();
		
		sut.categorize(REPOS);
		
		verify(mappingParser).parse(REPOS);
		verify(validationParser).parse(events);
		
		assertLogContains(0, "Reading the events mapping file ...");
		assertLogContains(1, "Reading validation data ...");
		assertLogContains(2, "Categorizing targets ...");
		
		assertLogContains(3, "\n#Invocations\t#Targets\n");
		assertLogContains(4, "0\t1\n");
		assertLogContains(5, "1\t2\n");
		assertLogContains(6, "2\t1\n");
	}
	
	private Episode createTarget(String...strings) {
		Episode target = new Episode();
		target.addStringsOfFacts(strings);
		return target;
	}
}

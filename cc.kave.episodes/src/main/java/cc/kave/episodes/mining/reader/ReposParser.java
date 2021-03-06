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
package cc.kave.episodes.mining.reader;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.episodes.export.EventStreamGenerator;
import cc.kave.episodes.model.events.Event;
import cc.recommenders.io.Directory;
import cc.recommenders.io.Logger;
import cc.recommenders.io.ReadingArchive;

public class ReposParser {
	
	private Directory contextsDir;
	private File eventsFolder;
	private FileReader reader;
	
	@Inject
	public ReposParser(@Named("contexts") Directory directory, @Named("events") File folder, FileReader reader) {
		assertTrue(folder.exists(), "Events folder does not exist");
		assertTrue(folder.isDirectory(), "Events is not a folder, but a file");
		this.contextsDir = directory;
		this.eventsFolder = folder;
		this.reader = reader;
	}
	
	public List<Event> learningStream(int numberOfRepos) throws ZipException, IOException {
		EventStreamGenerator generator = new EventStreamGenerator();
		StringBuilder repositories = new StringBuilder();
		String repoName = "";
		int repoID = 0;

		for (String zip : findZips(contextsDir)) {
			Logger.log("Reading zip file %s", zip.toString());
			if ((repoName.equalsIgnoreCase("")) || (!zip.startsWith(repoName))) {
				repoID++;
				if (repoID > numberOfRepos) {
					break;
				}
				repoName = getRepoName(zip);
				repositories.append(repoName + "\n");
			} 
			ReadingArchive ra = contextsDir.getReadingArchive(zip);

			while (ra.hasNext()) {
				Context ctx = ra.getNext(Context.class);
				if (ctx == null) {
					continue;
				}
				generator.add(ctx);
			}
			ra.close();
		}
		FileUtils.writeStringToFile(new File(getReposPath(numberOfRepos)), repositories.toString());
		List<Event> allEvents = generator.getEventStream();
		return allEvents;
	}
	
	public List<Event> validationStream(int numbRepos) throws IOException {
		List<String> learningRepos = reader.readFile(new File(getReposPath(numbRepos)));
		EventStreamGenerator generator = new EventStreamGenerator();
		
		for (String zip : findZips(contextsDir)) {
			String repoName = getRepoName(zip);
			if (learningRepos.contains(repoName)) {
				continue;
			}
			Logger.log("Reading zip file %s", zip.toString());
			ReadingArchive ra = contextsDir.getReadingArchive(zip);

			while (ra.hasNext()) {
				Context ctx = ra.getNext(Context.class);
				if (ctx == null) {
					continue;
				}
				generator.add(ctx);
			}
			ra.close();
		}
		List<Event> allEvents = generator.getEventStream();
		return allEvents;
	}

	private String getRepoName(String zipName) {
		int index = zipName.indexOf("/", zipName.indexOf("/", zipName.indexOf("/") + 1) + 1);
		String startPrefix = zipName.substring(0, index);

		return startPrefix;
	}

	private Set<String> findZips(Directory contextsDir) {
		Set<String> zips = contextsDir.findFiles(new Predicate<String>() {

			@Override
			public boolean apply(String arg0) {
				return arg0.endsWith(".zip");
			}
		});
		return zips;
	}
	
	private String getReposPath(int numberOfRepos) {
		File pathName = new File(eventsFolder.getAbsolutePath() + "/" + numberOfRepos + "Repos");
		if (!pathName.isDirectory()) {
			pathName.mkdir();
		}
		String fileName = pathName.getAbsolutePath() + "/repositories.txt";
		return fileName;
	}
}

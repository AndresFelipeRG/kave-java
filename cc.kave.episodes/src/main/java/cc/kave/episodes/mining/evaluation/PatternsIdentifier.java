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
package cc.kave.episodes.mining.evaluation;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import cc.kave.commons.model.episodes.Event;
import cc.kave.commons.model.episodes.EventKind;
import cc.kave.commons.model.episodes.Fact;
import cc.kave.episodes.mining.patterns.MaximalEpisodes;
import cc.kave.episodes.mining.reader.MappingParser;
import cc.kave.episodes.mining.reader.ReposParser;
import cc.kave.episodes.mining.reader.StreamParser;
import cc.kave.episodes.model.EnclosingMethods;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.postprocessor.EpisodesPostprocessor;
import cc.recommenders.io.Logger;

public class PatternsIdentifier {

	private File patternsFolder;

	private StreamParser streamParser;
	private MappingParser mappingParser;
	private EpisodesPostprocessor episodeProcessor;
	private MaximalEpisodes maxEpisodes;

	private ReposParser repos;

	@Inject
	public PatternsIdentifier(@Named("patterns") File folder, StreamParser streamParser, EpisodesPostprocessor episodes,
			MappingParser mappingParser, MaximalEpisodes maxEpisodes, ReposParser repos) {
		assertTrue(folder.exists(), "Patterns folder does not exist");
		assertTrue(folder.isDirectory(), "Patterns folder is not a folder, but a file");
		this.patternsFolder = folder;
		this.streamParser = streamParser;
		this.mappingParser = mappingParser;
		this.episodeProcessor = episodes;
		this.maxEpisodes = maxEpisodes;
		this.repos = repos;
	}

	public void trainingCode(int numbRepos, int frequency, double entropy) throws Exception {
		List<List<Fact>> stream = streamParser.parseStream(numbRepos);
		List<Event> events = mappingParser.parse(numbRepos);
		Map<Integer, Set<Episode>> postpEpisodes = episodeProcessor.postprocess(numbRepos, frequency, entropy);
		Map<Integer, Set<Episode>> patterns = maxEpisodes.getMaximalEpisodes(postpEpisodes);

		// int largestMethod = 0;
		// for (List<Fact> method : stream) {
		// if (method.size() > largestMethod) {
		// largestMethod = method.size();
		// }
		// }
		// Logger.log("Size of the largest method is: %d", largestMethod);

		for (Map.Entry<Integer, Set<Episode>> entry : patterns.entrySet()) {
			if (entry.getKey() < 2) {
				continue;
			}
			// Episode debug = createDebuggingEpisode();
			for (Episode episode : entry.getValue()) {
				Set<Fact> episodeFacts = episode.getEvents();
				EnclosingMethods methodsOrderRelation = new EnclosingMethods(true);

				for (List<Fact> method : stream) {
					if (method.size() < 3) {
						continue;
					}
					if (method.containsAll(episodeFacts)) {
						methodsOrderRelation.addMethod(episode, method, events);
						// if (episode.equals(debug)) {
						// Logger.log("Method: %s\noccurrence: %d",
						// method.toString(),
						// methodsOrderRelation.getOccurrences());
						// }
					}
				}
				if (methodsOrderRelation.getOccurrences() < episode.getFrequency()) {
					Logger.log("Episode: %s", episode.toString());
					Logger.log("Frequency = %d, occurrence = %d", episode.getFrequency(),
							methodsOrderRelation.getOccurrences());
					throw new Exception("Episode is not found sufficient number of times on the training stream!");
				}
			}
			Logger.log("Processed %d-node patterns!", entry.getKey());
			break;
		}
	}

	private Episode createDebuggingEpisode() {
		Episode episode = new Episode();
		episode.addStringsOfFacts("50", "14", "50>14");
		episode.setFrequency(25);
		episode.setBidirectMeasure(1.0);
		return episode;
	}

	public void validationCode(int numbRepos, int frequency, double entropy) throws Exception {
		List<Event> trainEvents = mappingParser.parse(numbRepos);
		Map<Integer, Set<Episode>> patterns = episodeProcessor.postprocess(numbRepos, frequency, entropy);

		List<Event> stream = repos.validationStream(numbRepos);
		Logger.log("Length of training mapping: %d", trainEvents.size());
		Logger.log("Length of validation stream: %d", stream.size());
		Logger.log("Getting all events ...");
		List<Event> allEvents = getAllEvents(stream, trainEvents);
		Logger.log("Length of complete mapper: %d", allEvents.size());
		Logger.log("Converting the validation stream to a list of methods ...");
		List<List<Fact>> streamMethods = streamOfMethods(stream, allEvents);
		StringBuilder sb = new StringBuilder();
		int patternID = 0;

//		for (Map.Entry<Integer, Set<Episode>> entry : patterns.entrySet()) {
		for (int i = 5; i > 1; i--) {
//			if (entry.getKey() < 2) {
//			if (i < 2) {
//				continue;
//			}
//			sb.append("Patterns of size: " + entry.getKey() + "-events\n");
			sb.append("Patterns of size: " + i + "-events\n");
			sb.append("PatternID\tFrequency\toccurrencesAsSet\toccurrencesOrder\n");
//			for (Episode episode : entry.getValue()) {
			for (Episode episode : patterns.get(i)) {
				EnclosingMethods methodsNoOrderRelation = new EnclosingMethods(false);
				EnclosingMethods methodsOrderRelation = new EnclosingMethods(true);

				for (List<Fact> method : streamMethods) {
					if (method.containsAll(episode.getEvents())) {
						methodsNoOrderRelation.addMethod(episode, method, allEvents);
						methodsOrderRelation.addMethod(episode, method, allEvents);
					}
				}
				sb.append(patternID + "\t" + episode.getFrequency() + "\t" + methodsNoOrderRelation.getOccurrences()
						+ "\t" + methodsOrderRelation.getOccurrences() + "\n");
				Logger.log("%d\t%d\t%d\t%d\n", patternID, episode.getFrequency(),
						methodsNoOrderRelation.getOccurrences(), methodsOrderRelation.getOccurrences());
				patternID++;
			}
			sb.append("\n");
//			Logger.log("\nProcessed %d-node patterns!", entry.getKey());
			Logger.log("\nProcessed %d-node patterns!", i);
		}
		FileUtils.writeStringToFile(getValidationPath(numbRepos, frequency, entropy), sb.toString());
	}

	private List<Event> getAllEvents(List<Event> stream, List<Event> events) {
//		Map<Event, Integer> completeEvents = Maps.newLinkedHashMap();
		for (Event e : stream) {
			if (!events.contains(e)) {
				events.add(e);
			}
		}
		return events;
	}

	private List<List<Fact>> streamOfMethods(List<Event> stream, List<Event> events) {
		List<List<Fact>> result = new LinkedList<>();
		List<Fact> method = new LinkedList<Fact>();

		for (Event event : stream) {
			if (event.getKind() == EventKind.FIRST_DECLARATION) {
				if (!method.isEmpty()) {
					result.add(method);
					method = new LinkedList<Fact>();
				}
			}
			int index = events.indexOf(event);
			method.add(new Fact(index));
		}
		if (!method.isEmpty()) {
			result.add(method);
		}
		return result;
	}

	private File getValidationPath(int numbRepos, int freqThresh, double bidirectThresh) {
		File folderPath = new File(patternsFolder.getAbsolutePath() + "/Repos" + numbRepos + "/Freq" + freqThresh
				+ "/Bidirect" + bidirectThresh + "/");
		if (!folderPath.isDirectory()) {
			folderPath.mkdirs();
		}
		File fileName = new File(folderPath.getAbsolutePath() + "/patternsValidation.txt");
		return fileName;
	}
}

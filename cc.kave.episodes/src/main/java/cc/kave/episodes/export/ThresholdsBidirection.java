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
package cc.kave.episodes.export;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cc.kave.episodes.mining.reader.EpisodeParser;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.statistics.EpisodesStatistics;

public class ThresholdsBidirection {

	private File patternsFolder;
	private EpisodeParser parser;
	private EpisodesStatistics statistics;

	@Inject
	public ThresholdsBidirection(@Named("patterns") File folder, EpisodeParser parse, EpisodesStatistics stats) {
		assertTrue(folder.exists(), "Patterns folder does not exist");
		assertTrue(folder.isDirectory(), "Patterns is not a folder, but a file");
		this.patternsFolder = folder;
		this.parser = parse;
		this.statistics = stats;
	}

	public void writer(int numbRepos, int frequency) throws IOException {
		Map<Integer, Set<Episode>> episodes = parser.parse(numbRepos);
		StringBuilder freqsBuilder = new StringBuilder();
		StringBuilder bdsBuilder = new StringBuilder();
		
		for (Map.Entry<Integer, Set<Episode>> entry : episodes.entrySet()) {
			if (entry.getKey() == 1) {
				continue;
			}
			Map<Integer, Integer> frequences = statistics.freqsEpisodes(entry.getValue());
			String freqsLevel = getFreqsStringRep(entry.getKey(), frequences);
			freqsBuilder.append(freqsLevel);
			
			Map<Double, Integer> bds = statistics.bidirectEpisodes(entry.getValue(), frequency);
			String bdsLevel = getBdsStringRep(entry.getKey(), bds);
			bdsBuilder.append(bdsLevel);
		}
//		FileUtils.writeStringToFile(new File(getPath(numbRepos).freqsPath), freqsBuilder.toString());
		FileUtils.writeStringToFile(new File(getPath(numbRepos).bdsPath), bdsBuilder.toString());
	}

	private String getBdsStringRep(Integer epLevel, Map<Double, Integer> bds) {
		String data = "Bidirectional distribution for " + epLevel + "-node episodes:\n";
		data += "Bidirectional\tCounter\n";
		
		for (Map.Entry<Double, Integer> entry : bds.entrySet()) {
			data += entry.getKey() + "\t" + entry.getValue() + "\n";
		}
		data += "\n";
		return data;
	}

	private String getFreqsStringRep(Integer epLevel, Map<Integer, Integer> frequences) {
		String data = "Frequency distribution for " + epLevel + "-node episodes:\n";
		data += "Frequency\tCounter\n";
		
		for (Map.Entry<Integer, Integer> entry : frequences.entrySet()) {
			data += entry.getKey() + "\t" + entry.getValue() + "\n";
		}
		data += "\n";
		return data;
	}
	
	private FilePaths getPath(int numbRepos) {
		FilePaths paths = new FilePaths();
		paths.freqsPath = patternsFolder.getAbsolutePath() + "/freqs" + numbRepos + "Repos.txt";
		paths.bdsPath = patternsFolder.getAbsolutePath() + "/freq40bds" + numbRepos + "Repos.txt";

		return paths;
	}

	private class FilePaths {
		private String freqsPath = "";
		private String bdsPath = "";
	}
}

/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ervina Cergani - initial API and implementation
 */
package cc.kave.commons.mining.episodes;

import static cc.recommenders.assertions.Asserts.assertFalse;
import static cc.recommenders.assertions.Asserts.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cc.kave.commons.model.episodes.Episode;

public class EpisodeParser {

	private File rootFolder;

	@Inject
	public EpisodeParser(@Named("episode") File directory) {
		assertTrue(directory.exists(), "Frequent episode folder does not exist");
		assertTrue(directory.isDirectory(), "Frequent episode folder is not a folder, but a file");
		this.rootFolder = directory;
	}

	public Map<Integer, List<Episode>> parse() {

		File filePath = getFilePath();
		List<String> lines = readFile(filePath);

		Map<Integer, List<Episode>> episodeLearned = new HashMap<Integer, List<Episode>>();
		List<Episode> episodeList = new LinkedList<Episode>();

		String[] rowValues;
		int numNodes = 0;

		for (String line : lines) {
			if (line.contains(":")) {
				rowValues = line.split(":");
				Episode episode = readEpisode(numNodes, rowValues);
				episodeList.add(episode);
			} else {
				rowValues = line.split("\\s+");
				if (!episodeList.isEmpty()) {
					episodeLearned.put(numNodes, episodeList);
				}
				if (Integer.parseInt(rowValues[3]) > 0) {
					String[] nodeString = rowValues[0].split("-");
					numNodes = Integer.parseInt(nodeString[0]);
					episodeList = new LinkedList<Episode>();
				}
			}
		}
		episodeLearned.put(numNodes, episodeList);
		return episodeLearned;
	}

	private Episode readEpisode(int numberOfNodes, String[] rowValues) {
		Episode episode = new Episode();
		episode.setNumEvents(numberOfNodes);
		episode.setFrequency(Integer.parseInt(rowValues[1].trim()));
		String[] events = rowValues[0].split("\\s+");
		for (int idx = 0; idx < numberOfNodes; idx++) {
			episode.addFact(events[idx]);
		}
		if (rowValues[3].contains(",")) {
			String[] relations = rowValues[3].substring(2).split(",");
			for (String relation : relations) {
				episode.addFact(relation);
			}
		}
		return episode;
	}

	private List<String> readFile(File file) {
		assertTrue(file.exists(), "Episode file does not exist");
		assertFalse(file.isDirectory(), "Episodes file is not a file, but a directory");

		List<String> lines = new LinkedList<String>();

		try {
			lines = FileUtils.readLines(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return lines;
	}

	private File getFilePath() {
		String fileName = rootFolder.getAbsolutePath() + "/output.txt";
		File file = new File(fileName);
		return file;
	}
}

/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package cc.kave.episodes.io;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Named;

import org.apache.commons.io.FileUtils;

import cc.kave.commons.utils.json.JsonUtils;
import cc.kave.episodes.model.EventStream;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.kave.episodes.model.events.Fact;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

public class EventStreamIo {

	private File repoDir;

	public static final double TIMEOUT = 0.5;

	@Inject
	public EventStreamIo(@Named("repositories") File folder) {
		assertTrue(folder.exists(), "Repositories folder does not exist");
		assertTrue(folder.isDirectory(),
				"Repositories is not a folder, but a file");
		this.repoDir = folder;
	}

	public void write(EventStream stream, int foldNum) {
		try {
			FileUtils.writeStringToFile(new File(
					getTrainPath(foldNum).streamPath), stream.getStream());
			JsonUtils.toJson(stream.getMapping().keySet(), new File(
					getTrainPath(foldNum).mappingPath));
			JsonUtils.toJson(stream.getEnclMethods(), new File(
					getTrainPath(foldNum).methodsPath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String readStream(String path) throws IOException {
		String stream = FileUtils.readFileToString(new File(path));
		return stream;
	}

	public List<List<Fact>> parseStream(String path) {
		List<List<Fact>> stream = Lists.newLinkedList();
		List<Fact> method = Lists.newLinkedList();

		List<String> lines = readlines(path);

		double timer = 0.0;

		for (String line : lines) {
			String[] eventTime = line.split(",");
			int eventID = Integer.parseInt(eventTime[0]);
			double timestamp = Double.parseDouble(eventTime[1]);
			while ((timestamp - timer) >= TIMEOUT) {
				stream.add(method);
				method = new LinkedList<Fact>();
				timer += TIMEOUT;
			}
			timer = timestamp;
			method.add(new Fact(eventID));
		}
		stream.add(method);
		return stream;
	}

	public List<Event> readMethods(String path) {
		@SuppressWarnings("serial")
		Type type = new TypeToken<List<Event>>() {
		}.getType();
		List<Event> methods = JsonUtils.fromJson(new File(path), type);
		assertMethods(methods);
		return methods;
	}

	private void assertMethods(List<Event> methods) {
		for (Event ctx : methods) {
			assertTrue(ctx.getKind() == EventKind.METHOD_DECLARATION,
					"List of methods does not contain only element cotexts!");
		}

	}

	public List<Event> readMapping(String path) {
		@SuppressWarnings("serial")
		Type type = new TypeToken<List<Event>>() {
		}.getType();
		return JsonUtils.fromJson(new File(path), type);
	}

	private List<String> readlines(String path) {
		List<String> lines = new LinkedList<String>();

		try {
			lines = FileUtils.readLines(new File(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return lines;
	}

	private class TrainingPath {
		String streamPath = "";
		String mappingPath = "";
		String methodsPath = "";
	}

	private TrainingPath getTrainPath(int fold) {
		File path = new File(repoDir.getAbsolutePath() + "/TrainingData/fold"
				+ fold);
		if (!path.isDirectory()) {
			path.mkdirs();
		}
		TrainingPath trainPath = new TrainingPath();
		trainPath.streamPath = path.getAbsolutePath() + "/stream.txt";
		trainPath.mappingPath = path.getAbsolutePath() + "/mapping.txt";
		trainPath.methodsPath = path.getAbsolutePath() + "/methods.txt";

		return trainPath;
	}
}
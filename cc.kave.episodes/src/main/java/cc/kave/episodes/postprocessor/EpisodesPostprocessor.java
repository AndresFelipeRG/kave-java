/**
 * * Copyright 2016 Technische Universität Darmstadt
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
package cc.kave.episodes.postprocessor;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import cc.kave.episodes.mining.reader.EpisodeParser;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.events.Fact;
import cc.recommenders.io.Logger;

public class EpisodesPostprocessor {

	private EpisodeParser parser;
	
	@Inject
	public EpisodesPostprocessor(EpisodeParser parser) {
		this.parser = parser;
	}
	
	public Map<Integer, Set<Episode>> postprocess(int numbRepos, int freqThresh, double bidirectThresh) {
		Map<Integer, Set<Episode>> patterns = Maps.newLinkedHashMap();
		
		Map<Integer, Set<Episode>> episodes = parser.parse(numbRepos);
		Logger.log("Finished parsing the episodes!");
		
		for (Map.Entry<Integer, Set<Episode>> entry : episodes.entrySet()) {
			if (entry.getKey() == 1) {
				continue;
			}
			Logger.log("Postprocessing %d-node episodes!", entry.getKey());
			Map<Set<Fact>, Episode> filtered = Maps.newLinkedHashMap();
			
			for (Episode ep : entry.getValue()) {
				int freq = ep.getFrequency();
				double bidirect = ep.getBidirectMeasure();
				
				if ((freq >= freqThresh) && (bidirect >= bidirectThresh)) {
					
					if (filtered.containsKey(ep.getEvents())) {
						Set<Fact> events = ep.getEvents();
						Episode filterEp = filtered.get(events);
						
						Episode repEp = getRepresentative(filterEp, ep, freqThresh, bidirectThresh);
						
						if (repEp.equals(ep)) {
							filtered.put(events, repEp);
						}
					} else {
						filtered.put(ep.getEvents(), ep);
					}
				}
			}
			Set<Episode> repEpisodes = getfilteredEp(filtered);
			patterns.put(entry.getKey(), repEpisodes);
		}
		return patterns;
	}

	private Set<Episode> getfilteredEp(Map<Set<Fact>, Episode> filtered) {
		Set<Episode> episodes = Sets.newLinkedHashSet();
		
		for (Map.Entry<Set<Fact>, Episode> entry : filtered.entrySet()) {
			episodes.add(entry.getValue());
		}
		return episodes;
	}

	private Episode getRepresentative(Episode filterEp, Episode currEp, int freqThresh, double bidirectThresh) {
		int ffreq = filterEp.getFrequency();
		double fbidirect = filterEp.getBidirectMeasure();
		
		int cfreq = currEp.getFrequency();
		double cbidirect = currEp.getBidirectMeasure();
		
		if (ffreq > cfreq) {
			return filterEp;
		}
		if (ffreq < cfreq) {
			return currEp;
		}
		if (ffreq == cfreq) {
			if (fbidirect < cbidirect) {
				return filterEp;
			}
			if (fbidirect > cbidirect) {
				return currEp;
			}
		}
		return filterEp;
//		Logger.log("Episode 1: %s", filterEp.toString());
//		Logger.log("Episode 2: %s", currEp.toString());
//		throw new Exception("There are two episodes exactly the same!");
	}
}

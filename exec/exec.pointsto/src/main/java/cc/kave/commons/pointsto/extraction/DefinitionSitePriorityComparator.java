/**
 * Copyright 2015 Simon Reuß
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
package cc.kave.commons.pointsto.extraction;

import java.io.Serializable;
import java.util.Comparator;
import java.util.EnumMap;

import cc.kave.commons.pointsto.dummies.DummyDefinitionSite;
//import cc.recommenders.usages.DefinitionSiteKind;

public class DefinitionSitePriorityComparator implements Comparator<DummyDefinitionSite>, Serializable {

	private static final long serialVersionUID = 850239537351939837L;

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(DummyDefinitionSite o1, DummyDefinitionSite o2) {
		// TODO Auto-generated method stub
		return 0;
	}

//	private EnumMap<DefinitionSiteKind, Integer> priorites = new EnumMap<>(DefinitionSiteKind.class);
//
//	public DefinitionSitePriorityComparator() {
//		priorites.put(DefinitionSiteKind.UNKNOWN, 0);
//		priorites.put(DefinitionSiteKind.THIS, 1);
//		priorites.put(DefinitionSiteKind.FIELD, 2);
//		priorites.put(DefinitionSiteKind.PARAM, 3);
//		priorites.put(DefinitionSiteKind.CONSTANT, 4);
//		priorites.put(DefinitionSiteKind.RETURN, 5);
//		priorites.put(DefinitionSiteKind.NEW, 5);
//
//		if (priorites.size() != DefinitionSiteKind.values().length) {
//			throw new RuntimeException("Number of entries in the priority map does not match number of enum values");
//		}
//	}
//
//	@Override
//	public int compare(DummyDefinitionSite defSite1, DummyDefinitionSite defSite2) {
//		return priorites.get(defSite1.getKind()) - priorites.get(defSite2.getKind());
//	}

}
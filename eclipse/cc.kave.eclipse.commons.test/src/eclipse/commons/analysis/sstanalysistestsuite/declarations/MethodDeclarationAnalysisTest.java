/**
 * Copyright 2015 Waldemar Graf
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

package eclipse.commons.analysis.sstanalysistestsuite.declarations;

import org.junit.Test;

import cc.kave.commons.model.ssts.impl.declarations.MethodDeclaration;
import eclipse.commons.analysis.sstanalysistestsuite.BaseSSTAnalysisTest;
import eclipse.commons.analysis.sstanalysistestsuite.SSTAnalysisFixture;

public class MethodDeclarationAnalysisTest extends BaseSSTAnalysisTest {

	private final String projectName = "testproject";
	private final String packageName = "sstanalysistestsuite.declarations;";

	@Test
	public void privateIsInlined() {
		updateContext(projectName, packageName + "PrivateIsInlined.java");
		MethodDeclaration mPub = newMethodDeclaration(
				"[%void, rt.jar, 1.8] [sstanalysistestsuite.declarations.PrivateIsInlined, ?].PublicA()");
		MethodDeclaration mPriv = newMethodDeclaration(
				"[%void, rt.jar, 1.8] [sstanalysistestsuite.declarations.PrivateIsInlined, ?].PrivateA()");
		mPriv.setEntryPoint(false);

		assertAllMethods(mPub, mPriv);
	}
}

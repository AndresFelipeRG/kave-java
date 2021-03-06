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

package cc.kave.eclipse.namefactory.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ImportDeclaration;

import cc.kave.eclipse.namefactory.NodeFactory;

public class ImportVisitor extends ASTVisitor {
	
	private List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();

	@Override
	public boolean visit(ImportDeclaration node) {
		imports.add(node);
		NodeFactory.createNodeName(node);
		return super.visit(node);
	}

	public List<ImportDeclaration> getImports() {
		return imports;
	}

	public ImportDeclaration getImport(String name) {
		for (ImportDeclaration i : imports) {
			if(i.resolveBinding().getName().equals(name)){
				return i;
			}
		}
		return null;
	}
}

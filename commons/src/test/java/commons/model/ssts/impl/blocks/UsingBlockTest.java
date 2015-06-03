package commons.model.ssts.impl.blocks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.Test;

import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.impl.blocks.UsingBlock;
import cc.kave.commons.model.ssts.impl.references.VariableReference;
import cc.kave.commons.model.ssts.impl.statements.ContinueStatement;
import cc.kave.commons.model.ssts.impl.statements.GotoStatement;
import cc.kave.commons.model.ssts.impl.statements.ReturnStatement;

import com.google.common.collect.Lists;
import commons.model.ssts.impl.SSTBaseTest;
import commons.model.ssts.impl.SSTTestHelper;

public class UsingBlockTest extends SSTBaseTest {

	@Test
	public void testDefaultValues() {
		UsingBlock sut = new UsingBlock();

		assertThat(new VariableReference(), equalTo(sut.getReference()));
		assertThat(new ArrayList<IStatement>(), equalTo(sut.getBody()));
		assertThat(0, not(equalTo(sut.hashCode())));
		assertThat(1, not(equalTo(sut.hashCode())));
	}

	@Test
	public void testSettingValues() {
		UsingBlock sut = new UsingBlock();
		sut.setReference(this.someVarRef("a"));
		sut.getBody().add(new ReturnStatement());
		assertThat(this.someVarRef("a"), equalTo(sut.getReference()));
		assertThat(Lists.newArrayList(new ReturnStatement()), equalTo(sut.getBody()));
	}

	@Test
	public void testEqualityDefault() {
		UsingBlock a = new UsingBlock();
		UsingBlock b = new UsingBlock();

		assertThat(a, equalTo(b));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	public void testEqualitfyReallyTheSame() {
		UsingBlock a = new UsingBlock();
		UsingBlock b = new UsingBlock();
		a.setReference(this.someVarRef("a"));
		a.getBody().add(new ReturnStatement());
		b.setReference(this.someVarRef("a"));
		b.getBody().add(new ReturnStatement());

		assertThat(a, equalTo(b));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	public void testEqualityDifferentReference() {
		UsingBlock a = new UsingBlock();
		UsingBlock b = new UsingBlock();
		a.setReference(this.someVarRef("a"));
		b.setReference(this.someVarRef("b"));

		assertThat(a, not(equalTo(b)));
		assertThat(a.hashCode(), not(equalTo(b.hashCode())));
	}

	@Test
	public void testEqualityDifferentBody() {
		UsingBlock a = new UsingBlock();
		UsingBlock b = new UsingBlock();
		a.getBody().add(new ContinueStatement());
		b.getBody().add(new GotoStatement());

		assertThat(a, not(equalTo(b)));
		assertThat(a.hashCode(), not(equalTo(b.hashCode())));
	}

	@Test
	public void testVisitorIsImplemented() {
		UsingBlock sut = new UsingBlock();
		SSTTestHelper.accept(sut, 23).verify(sut);
	}

	@Test
	public void testVisitorWithReturnIsImplemented() {
		// TODO: Visitor Test
	}
}

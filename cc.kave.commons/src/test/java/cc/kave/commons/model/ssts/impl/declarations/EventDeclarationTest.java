package cc.kave.commons.model.ssts.impl.declarations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import cc.kave.commons.model.names.IEventName;
import cc.kave.commons.model.names.csharp.EventName;
import cc.kave.commons.model.ssts.impl.SSTTestHelper;

public class EventDeclarationTest {

	@Test
	public void testDefaultValues() {
		EventDeclaration sut = new EventDeclaration();

		assertThat(EventName.UNKNOWN_NAME, equalTo(sut.getName()));
		assertThat(0, not(equalTo(sut.hashCode())));
		assertThat(1, not(equalTo(sut.hashCode())));
	}

	@Test
	public void testSettingValues() {
		EventDeclaration sut = new EventDeclaration();
		sut.setName(someEvent());

		assertThat(someEvent(), equalTo(sut.getName()));
	}

	@Test
	public void testEqualityDefault() {
		EventDeclaration a = new EventDeclaration();
		EventDeclaration b = new EventDeclaration();

		assertThat(a, equalTo(b));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	public void testEqualityReallyTheSame() {
		EventDeclaration a = new EventDeclaration();
		EventDeclaration b = new EventDeclaration();
		a.setName(someEvent());
		b.setName(someEvent());

		assertThat(a, equalTo(b));
		assertThat(a.hashCode(), equalTo(b.hashCode()));
	}

	@Test
	public void testEqualityDifferentType() {
		EventDeclaration a = new EventDeclaration();
		EventDeclaration b = new EventDeclaration();
		a.setName(someEvent());

		assertThat(a, not(equalTo(b)));
		assertThat(a.hashCode(), not(equalTo(b.hashCode())));
	}

	@Test
	public void testVisitorIsImplemented() {
		EventDeclaration sut = new EventDeclaration();
		SSTTestHelper.accept(sut, 23).verify(sut);
	}

	@Test
	public void testWithReturnIsImplemented() {
		// TODO : Visitor Test
	}
	
	private IEventName someEvent() {
		return EventName.newEventName("[T1,P1] [T2,P2].Event");
	}
}

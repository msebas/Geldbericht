package org.mcservice.javafx.control.table;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ReflectedFieldTest {


	
	public class noGetter{
		@SuppressWarnings("unused")
		private int myMember=0;
		public void setMyMember(int myMember) {this.myMember = myMember;}
	}
	
	public class noSetter{
		private int myMember;
		public int getMyMember() {return myMember;}
	}
	
	public class protectedGetter{
		private int myMember;
		protected int getMyMember() {return myMember;}
	}
	
	public class protectedSetter{
		@SuppressWarnings("unused")
		private int myMember;
		protected void setMyMember(int myMember) {this.myMember = myMember;}
	}
	
	public class protectedIsGetter{
		private int myMember;
		protected int isMyMember() {return myMember;}
	}
	
	public class isGetter{
		private int myMember;
		public int isMyMember() {return myMember;}
	}
	
	public class strangeGetter{
		private int myMember;
		public String getMyMember() {return String.valueOf(myMember);}
		public String isMyMember() {return String.valueOf(myMember);}
	}
	
	@Test void checkNoGetterReadableWritable() {
		ReflectedField<Integer> tstObj=new ReflectedField<>("myMember", noGetter.class);
		assertFalse(tstObj.isReadable());
		assertTrue(tstObj.isWritable());
	}
	
	@Test void checkNoSetterReadableWritable() throws Exception {
		ReflectedField<Integer> tstObj=new ReflectedField<>(noSetter.class.getDeclaredField("myMember"), noSetter.class);
		assertTrue(tstObj.isReadable());
		assertFalse(tstObj.isWritable());
	}
	
	@Test void checkSet() {
		ReflectedField<Integer> tstObj1=new ReflectedField<>("myMember", noGetter.class);
		ReflectedField<Integer> tstObj2=new ReflectedField<>("myMember", noSetter.class);
		
		noGetter g = Mockito.mock(noGetter.class);
		noSetter s = Mockito.mock(noSetter.class);
				
		assertThrows(IllegalStateException.class, () -> tstObj2.set(s,0));
		
		doThrow(new IllegalStateException()).when(g).setMyMember(0);
		tstObj1.set(g,1);
		assertThrows(RuntimeException.class, () -> tstObj1.set(g,0));
		
	}
	
	@Test void checkGet() {
		ReflectedField<Integer> tstObj1=new ReflectedField<>("myMember", noGetter.class);
		ReflectedField<Integer> tstObj2=new ReflectedField<>("myMember", noSetter.class);
		
		noSetter s = Mockito.mock(noSetter.class);
				
		assertThrows(IllegalStateException.class, () -> tstObj1.get(s));
		
		assertEquals(0,tstObj2.get(s));
		doThrow(new IllegalStateException()).when(s).getMyMember();
		assertThrows(RuntimeException.class, () -> tstObj2.get(s));
		
	}
	
	@Test void checkGetters() throws Exception {
		ReflectedField<Integer> tstObj=new ReflectedField<>("myMember", noSetter.class);
		
		assertEquals(noSetter.class.getDeclaredField("myMember"),tstObj.getField());
		assertEquals("myMember",tstObj.toString());
		assertEquals("myMember",tstObj.getName());
		assertEquals(noSetter.class,tstObj.getFieldClass());
		assertEquals(Integer.TYPE,tstObj.getType());
		
	}
	
	@Test void checkFieldNotPresent() {
		ReflectedField<Integer> tstObj=new ReflectedField<>("noMember", noSetter.class);
		assertThrows(RuntimeException.class, () -> tstObj.isReadable());
	}
	
	@Test void checkProtectedGetter() {
		ReflectedField<Integer> tstObj=new ReflectedField<>("myMember", protectedGetter.class);
		assertFalse(tstObj.isReadable());
	}
	
	@Test void checkProtectedSetter() {
		ReflectedField<Integer> tstObj=new ReflectedField<>("myMember", protectedSetter.class);
		assertFalse(tstObj.isWritable());
	}
	
	@Test void checkProtectedIsGetter() {
		ReflectedField<Integer> tstObj=new ReflectedField<>("myMember", protectedIsGetter.class);
		assertFalse(tstObj.isReadable());
	}
	
	@Test void checkStrangeGetter() {
		ReflectedField<Integer> tstObj=new ReflectedField<>("myMember", strangeGetter.class);
		assertFalse(tstObj.isReadable());
	}
	
	@Test void checkIsGetter() {
		ReflectedField<Integer> tstObj=new ReflectedField<>("myMember", isGetter.class);
		assertTrue(tstObj.isReadable());
	}

}


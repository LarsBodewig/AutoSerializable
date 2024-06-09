package dev.bodewig.autoserializable;

public abstract class BeanWithNoArgsConstructor {

	public static class Explicit {
		public Explicit() {
		}
	}
	
	public static class Implicit {
	}
	
	public static class Inherited extends BeanWithNoArgsConstructor {
	}

	public BeanWithNoArgsConstructor() {
	}
}

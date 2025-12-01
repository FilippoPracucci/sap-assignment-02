package main.java.common.ddd;

public interface Aggregate<T> extends Entity<T>{
	
	T getId(); 
	
}

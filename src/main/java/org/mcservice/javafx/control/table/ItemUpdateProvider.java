package org.mcservice.javafx.control.table;

public interface ItemUpdateProvider {

	public void addListener(ItemUpdateListener listener); 

    public void removeListener(ItemUpdateListener listener);
	
}

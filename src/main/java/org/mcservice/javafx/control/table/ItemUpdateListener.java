package org.mcservice.javafx.control.table;

@FunctionalInterface
public interface ItemUpdateListener {
	
	/**
     * @param trueChange Tells if this call has changed anything 
     */
    void changed(boolean trueChange);

}

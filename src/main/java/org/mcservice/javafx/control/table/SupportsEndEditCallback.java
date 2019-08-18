package org.mcservice.javafx.control.table;

import java.util.function.Consumer;

public interface SupportsEndEditCallback {
	
	public void setEndEditCallback(Consumer<String> endEditCallback);
}

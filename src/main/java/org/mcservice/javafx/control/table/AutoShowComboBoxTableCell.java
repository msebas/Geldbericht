/*******************************************************************************
 * Copyright (C) 2019 Sebastian Müller <sebastian.mueller@mcservice.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.mcservice.javafx.control.table;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class AutoShowComboBoxTableCell<S, T> extends ComboBoxTableCell<S, T> {

	private ComboBox<T> box;
	
	private EventHandler<KeyEvent> filter=event -> {
		if (event.getCode() == KeyCode.ENTER) {
			if (isEditing() && null!=box && !box.isShowing()) {
				commitEdit(box.getSelectionModel().getSelectedItem());
			}
		} else if (event.getCode() == KeyCode.ESCAPE) {
			cancelEdit();
		}
	};

	/**
	 * Creates a default {@link ComboBoxTableCell} instance with the given items
	 * being used to populate the {@link ComboBox} when it is shown. This cell
	 * could only be added to a single TableView, otherwise event filters might 
	 * get into a non correct state.
	 *
	 * @param items The items to show in the ComboBox popup menu when selected by
	 *              the user.
	 */
	public AutoShowComboBoxTableCell(ObservableList<T> items) {
		super(null, items);
		tableViewProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				WeakEventHandler<KeyEvent> weakFilter=new WeakEventHandler<KeyEvent>(filter);
				getTableView().addEventFilter(KeyEvent.KEY_RELEASED, weakFilter);
				observable.removeListener(this);
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public void startEdit() {
		if (isEditing())
			return;

		super.startEdit();

		if (isEditing()) {
			Node node = this.getGraphic();
			if (node instanceof ComboBox) {
				@SuppressWarnings("unchecked")
				ComboBox<T> box = (ComboBox<T>) node;
				this.box=box;
				box.show();
			} else {
				throw new RuntimeException("Unable to get ComboBox from TableCell#getGraphic. "
						+ "Implementation details might have changed. "
						+ "Program does not work correctly.");
			}
		}
	}
	
}

/*******************************************************************************
 * Copyright (C) 2019 Sebastian Müller <sebastian.mueller@mcservice.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.mcservice.geldbericht;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.CompanyProperty;
import org.mcservice.geldbericht.database.DbAbstractionLayer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;;

public class CompanyManagerController {

	protected DbAbstractionLayer db=null;
	protected ObservableList<Company> companies=null;
	protected ObservableList<CompanyProperty> companyProperties=null;
	protected ZonedDateTime lastUpdate=ZonedDateTime.now();
	
	@FXML
	private Label changesLabel=null;
	@FXML
	private TableView<CompanyProperty> companyTableView=null;
	
	@SuppressWarnings("exports")
	public CompanyManagerController(ObservableList<Company> companies, DbAbstractionLayer db,
			ZonedDateTime lastUpdate) {
		this.companies=companies;
		this.companyProperties=FXCollections.observableArrayList();
		this.db=db;
		this.lastUpdate=lastUpdate;
	}
	
	@FXML
    public void initialize() {
		for (Company company : companies) {
			companyProperties.add(new CompanyProperty(company));
		}
		
		companyTableView.setItems(companyProperties);
		setTableEditable();
    }

	@FXML
    private void add() throws IOException {
        companyProperties.add(new CompanyProperty(new Company("","","")));
    }
	
	@FXML
    private void cancel() throws IOException {
        Stage stage = (Stage) companyTableView.getScene().getWindow();
        stage.close();
    }
	
	@FXML
    private void persist() throws IOException {
		ArrayList<Company> newCompanies=new ArrayList<Company>();
		for(int i=0;i<companyProperties.size();++i) {
			if (companyProperties.get(i).getUid()==null) {
				newCompanies.add(companyProperties.get(i).updateCompany());
			} else {
				companyProperties.get(i).updateCompany();
			}
		}
		
		for (Company company : companies) {
			if (company.getLastChange().isAfter(lastUpdate))
				db.updateCompany(company);
		}
		for (Company company : newCompanies) {
			companies.add(db.persistCompany(company));
		}
		
		this.cancel();
    }
	
	@FXML 
	private void editName(TableColumn.CellEditEvent<CompanyProperty, String> editEvent) {
		CompanyProperty actCompany=companyTableView.getSelectionModel().getSelectedItem();
		if (editEvent.getNewValue().strip().length()>0) {
			actCompany.setCompanyName(editEvent.getNewValue().strip());
		} else {
			actCompany.setCompanyName(null);
		}		
	}
	
	@FXML 
	private void editNumber(TableColumn.CellEditEvent<CompanyProperty, String> editEvent) {
		CompanyProperty actCompany=companyTableView.getSelectionModel().getSelectedItem();
		if (editEvent.getNewValue().strip().length()>0) {
			actCompany.setCompanyNumber(editEvent.getNewValue().strip());
		} else {
			actCompany.setCompanyNumber(null);
		}		
	}
	
	@FXML 
	private void editBookkeepingAppointment(TableColumn.CellEditEvent<CompanyProperty, String> editEvent) {
		CompanyProperty actCompany=companyTableView.getSelectionModel().getSelectedItem();
		if (editEvent.getNewValue().strip().length()>0) {
			actCompany.setCompanyBookkeepingAppointment(editEvent.getNewValue().strip());
		} else {
			actCompany.setCompanyBookkeepingAppointment(null);
		}		
	}
	
	@SuppressWarnings("unchecked")
	private void setTableEditable() {
		companyTableView.setEditable(true);
		for (TableColumn<CompanyProperty, ?> cell :this.companyTableView.getColumns()) {
			((TableColumn<CompanyProperty, String>) cell).setCellFactory(TextFieldTableCell.<CompanyProperty>forTableColumn());
			
		}
		// allows the individual cells to be selected
		companyTableView.getSelectionModel().cellSelectionEnabledProperty().set(true);
		// when character or numbers pressed it will start edit in editable
		// fields
		/* * /
		companyTableView.setOnKeyPressed(event -> {
			if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
				editFocusedCell();
			} else if (event.getCode() == KeyCode.RIGHT
					|| event.getCode() == KeyCode.TAB) {
				companyTableView.getSelectionModel().selectNext();
				event.consume();
			} else if (event.getCode() == KeyCode.LEFT) {
				companyTableView.getSelectionModel().selectPrevious(); //due to a bug
				// stopping it from working on
				// the first column in the last row of the table
				//selectPrevious();
				event.consume();
			}
		});
		/* */
	}

	@SuppressWarnings("unchecked")
	private void editFocusedCell() {
		final TablePosition<CompanyProperty, ?> focusedCell = companyTableView
				.focusModelProperty().get().focusedCellProperty().get();
		companyTableView.edit(focusedCell.getRow(), focusedCell.getTableColumn());
	}
    
}

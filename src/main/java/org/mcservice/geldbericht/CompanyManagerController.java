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
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.CompanyProperty;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mcservice.javafx.AnnotationBasedFormatter;
import org.mcservice.javafx.table.ValidatingTextFieldTableCell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
//import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;

public class CompanyManagerController {
	
	protected static class MemberVariable{
		public final Field field;
		public final Method setter;
		public final Method getter;
		
		public MemberVariable(Field field, Method setter, Method getter) {
			this.field=field;
			this.setter=setter;
			this.getter=getter;
		}
		
		public static MemberVariable fromName(String fieldName, Class<?> owningClass) throws NoSuchFieldException, SecurityException, NoSuchMethodException {
			String upperFieldName=fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
			Field field=owningClass.getDeclaredField(fieldName);
			return new MemberVariable(field,
					owningClass.getMethod("set"+upperFieldName,field.getType()),
					owningClass.getMethod("get"+upperFieldName));
		}
	}

	protected DbAbstractionLayer db=null;
	protected ObservableList<Company> companies=null;
	protected ObservableList<Company> companiesWithErrors=null;
	protected ZonedDateTime lastUpdate=null;
	
	protected static Validator companyValidator=Validation.buildDefaultValidatorFactory().getValidator();
	
	protected Map<String,MemberVariable> mapping;
	
	protected ObjectProperty<String> lastKeyCode = new SimpleObjectProperty<String>(this,"lastInputEditCell");
	
	@FXML
	private Label changesLabel=null;
	@FXML
	private Button persistButton=null;
	@FXML
	private TableView<Company> companyTableView=null;
	
	public CompanyManagerController(DbAbstractionLayer db) throws Exception{
		this.db=db;
		Class<Company> c=Company.class;
		mapping=Map.of(
			"tableColumnName",MemberVariable.fromName("companyName",c),
			"tableColumnNumber",MemberVariable.fromName("companyNumber",c),
			"tableColumnBookkeepingAppointment",MemberVariable.fromName("companyBookkeepingAppointment",c)
		);
	}
	
	@FXML
    public void initialize() {
		this.companies=FXCollections.observableArrayList(this.db.getCompanies());
		List<Company> act=new ArrayList<Company>();
		for (Company company : this.companies) {
			if(companyValidator.validate(company).size()>0) {
				act.add(company);
			}
		}
		this.companiesWithErrors=FXCollections.observableList(act);
		persistButton.setDisable(!this.companiesWithErrors.isEmpty());
		if(this.companiesWithErrors.size()>0) {
			changesLabel.setText(String.format("%d fehlerhafte Einträge",this.companiesWithErrors.size()));
		}
		this.lastUpdate=ZonedDateTime.now();
		companyTableView.setItems(companies);
		setTableEditable();
    }

	@FXML
    private void add() throws IOException {
        companies.add(new Company("","",""));
        companiesWithErrors.add(companies.get(companies.size()-1));
    }
	
	@FXML
    private void cancel() throws IOException {
        Stage stage = (Stage) companyTableView.getScene().getWindow();
        stage.close();
    }
	
	@FXML
    private void persist() throws IOException {
		db.manageCompanies(companies, lastUpdate);
		this.cancel();
    }
	
	@FXML 
	private void editField(TableColumn.CellEditEvent<CompanyProperty, String> editEvent) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		MemberVariable actMember=this.mapping.get(editEvent.getTableColumn().getId());
		Company actCompany=companyTableView.getSelectionModel().getSelectedItem();
		if (editEvent.getNewValue().strip().length()>0) {
			actMember.setter.invoke(actCompany, editEvent.getNewValue().strip());
		} else {
			actMember.setter.invoke(actCompany, new Object[] {null} );
		}
		
		if(companyValidator.validate(actCompany).size()>0) {
			if (!this.companiesWithErrors.contains(actCompany)) {
				this.companiesWithErrors.add(actCompany);
			}
			if(companyValidator.validateProperty(actCompany,actMember.field.getName()).isEmpty()) {
				companyTableView.getSelectionModel().selectNext();
			}
		} else {
			this.companiesWithErrors.remove(actCompany);
			companyTableView.getSelectionModel().selectNext();
		}
		
		if (this.companiesWithErrors.isEmpty()) {
			changesLabel.setText("Ungespeicherte Änderungen");
			persistButton.setDisable(false);
		} else {
			changesLabel.setText(String.format("%d fehlerhafte Einträge",this.companiesWithErrors.size()));
			persistButton.setDisable(true);
		}		
	}
	
	@SuppressWarnings("unchecked")
	private void setTableEditable() {
		companyTableView.setEditable(true);
		for (TableColumn<Company, ?> column :this.companyTableView.getColumns()) {
			MemberVariable actMember=this.mapping.get(column.getId());
			((TableColumn<Company, String>) column).setCellFactory(
				ValidatingTextFieldTableCell.forTableColumn(
						new AnnotationBasedFormatter<Company,String>(actMember.field,Company.class,""), 
					lastKeyCode));
		}
		companyTableView.getSelectionModel().cellSelectionEnabledProperty().set(true);
		
		companyTableView.setOnKeyTyped(event -> {
			String actChar=event.getCharacter();
						
			if (actChar!=null && actChar.matches("[\\w \\p{Punct}]")) {
				this.lastKeyCode.set(actChar);
				final TablePosition<Company, ?> focusedCell = 
						companyTableView.focusModelProperty().get().focusedCellProperty().get();
				companyTableView.edit(focusedCell.getRow(), focusedCell.getTableColumn());
				this.lastKeyCode.set(null);
			}
		});
	}
	
}

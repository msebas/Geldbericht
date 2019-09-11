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
package org.mcservice.geldbericht;

import org.mcservice.geldbericht.database.DbAbstractionLayer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PrimaryController {
	
	protected ControllerFactory controllerFactory=null;
	
	@FXML 
	protected VBox transactionTab;
	@FXML
	protected TransactionInputPaneController transactionTabController;	
	@FXML 
	protected VBox pdfGeneratorTab;
	@FXML
	protected PdfGeneratorPaneController pdfGeneratorTabController;
	@FXML
	protected Menu menuManager;
	@FXML
	protected MenuItem menuCompanyManager;
	@FXML
	protected MenuItem menuAccountManager;
	@FXML
	protected MenuItem menuVatTypeManager;
		
	public PrimaryController(DbAbstractionLayer db) {
		this.controllerFactory=new ControllerFactory(db);
	}
	
	@FXML
    public void initialize() {
		
    }

	@FXML
	protected void startCompanyManager() throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("CompanyManager.fxml"));
		CompanyManagerController controller = 
				(CompanyManagerController) controllerFactory.call(CompanyManagerController.class);
		fxmlLoader.setController(controller);
		controller.setUpdatedNotification(() -> updateControllerData());
		Scene scene = new Scene(fxmlLoader.load());
        
		// New window (Stage)
		Stage newWindow = new Stage();
		newWindow.setTitle("Betriebmanager");
		newWindow.setScene(scene);

		newWindow.show();
	}
	
	@FXML
	protected void startAccountManager() throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("AccountManager.fxml"));
		AccountManagerController controller = 
				(AccountManagerController) controllerFactory.call(AccountManagerController.class);
		fxmlLoader.setController(controller);
		controller.setUpdatedNotification(() -> updateControllerData());
		Scene scene = new Scene(fxmlLoader.load());
        
		// New window (Stage)
		Stage newWindow = new Stage();
		newWindow.setTitle("Kontenmanager");
		newWindow.setScene(scene);
		
		newWindow.show();
	}
	
	@FXML
	protected void startVatTypeManager() throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("VatTypeManager.fxml"));
		VatTypeManagerController controller = 
				(VatTypeManagerController) controllerFactory.call(VatTypeManagerController.class);
		controller.setUpdatedNotification(() -> updateControllerData());
		fxmlLoader.setController(controller);
		Scene scene = new Scene(fxmlLoader.load());
        
		// New window (Stage)
		Stage newWindow = new Stage();
		newWindow.setTitle("Steuermanager");
		newWindow.setScene(scene);
		
		newWindow.show();
	}
		
	protected void updateControllerData() {
		transactionTabController.updateData();
		pdfGeneratorTabController.updateData();
	}
}

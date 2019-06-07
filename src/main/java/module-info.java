module org.mcservice.Geldbericht {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.mcservice.geldbericht to javafx.fxml;
    exports org.mcservice.geldbericht;
}
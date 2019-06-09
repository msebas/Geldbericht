module org.mcservice.geldbericht {
    requires javafx.controls;
    requires javafx.fxml;
	requires java.sql;
	requires org.junit.jupiter.api;
	requires java.persistence;

    opens org.mcservice.geldbericht to javafx.fxml;
    exports org.mcservice.geldbericht;
}
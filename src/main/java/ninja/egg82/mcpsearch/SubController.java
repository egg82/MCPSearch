package ninja.egg82.mcpsearch;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableView;
import javafx.fxml.FXML;

public class SubController {
    // Search
    @FXML
    public JFXTextField searchText;

    // Data
    @FXML
    public JFXTreeTableView classesTable;
    @FXML
    public JFXTreeTableView methodsTable;
    @FXML
    public JFXTreeTableView fieldsTable;
}

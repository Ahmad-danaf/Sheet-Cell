package desktop;

import com.sheetcell.engine.Engine;
import com.sheetcell.engine.EngineImpl;
import desktop.body.BodyController;
import desktop.sheet.SheetController;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;


public class AppController{

    private Engine engine;
    @FXML private BorderPane body;
    @FXML private BodyController bodyController;
    @FXML private ScrollPane sheet;
    private SheetController sheetController;

    @FXML
    public void initialize() {
        engine = new EngineImpl();
        //bodyController.setMainController(this);
        //sheetController.setMainController(this);
    }

    public void setBodyController(BodyController bodyController) {
        this.bodyController = bodyController;
    }

    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
    }

    public void loadxmlbuttenpresses()
    {
       //function that body can call and then the then engine load the xml and then the sheet can be updated from sheet controller
    }


}

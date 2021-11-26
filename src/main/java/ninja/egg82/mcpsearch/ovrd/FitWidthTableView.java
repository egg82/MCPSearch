package ninja.egg82.mcpsearch.ovrd;

import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.scene.control.Skin;

// https://stackoverflow.com/questions/50583912/javafx-9-10-no-longer-possible-to-override-tableview-resizecolumntofitcontent
public class FitWidthTableView<S extends RecursiveTreeObject<S>> extends JFXTreeTableView<S> {
    public FitWidthTableView() { setSkin(new FitWidthTableViewSkin<>(this)); }

    public void resizeColumnsToFitContent() {
        Skin<?> skin = getSkin();
        if (skin instanceof FitWidthTableViewSkin<?> ctvs) {
            ctvs.resizeColumnsToFitContent();
        }
    }
}

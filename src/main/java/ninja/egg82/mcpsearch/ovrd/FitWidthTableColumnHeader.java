package ninja.egg82.mcpsearch.ovrd;

import com.jfoenix.skins.JFXTableColumnHeader;
import javafx.scene.control.TableColumnBase;

public class FitWidthTableColumnHeader extends JFXTableColumnHeader {
    public FitWidthTableColumnHeader(TableColumnBase col) { super(col); }

    @Override
    public void resizeColumnToFitContent(int rows) { super.resizeColumnToFitContent(-1); }
}

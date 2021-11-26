package ninja.egg82.mcpsearch.ovrd;

import com.jfoenix.skins.JFXTreeTableViewSkin;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;

// https://stackoverflow.com/questions/50583912/javafx-9-10-no-longer-possible-to-override-tableview-resizecolumntofitcontent
public class FitWidthTableViewSkin<S> extends JFXTreeTableViewSkin<S> {
    public FitWidthTableViewSkin(TreeTableView<S> tableView) {
        super(tableView);
    }

    @Override
    protected TableHeaderRow createTableHeaderRow() {
        return new TableHeaderRow(this) {

            @Override
            protected NestedTableColumnHeader createRootHeader() {
                return new NestedTableColumnHeader(null) {

                    @Override
                    protected TableColumnHeader createTableColumnHeader(TableColumnBase col) {
                        return new FitWidthTableColumnHeader(col);
                    }
                };
            }
        };
    }

    public void resizeColumnsToFitContent() {
        for (TableColumnHeader columnHeader : getTableHeaderRow().getRootHeader().getColumnHeaders()) {
            if (columnHeader instanceof FitWidthTableColumnHeader colHead) {
                colHead.resizeColumnToFitContent(-1);
            }
        }
    }
}

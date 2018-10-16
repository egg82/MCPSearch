package ninja.egg82.mcpsearch.utils.gui;

import com.sun.javafx.scene.control.skin.TreeTableViewSkin;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TreeTableGUIUtil {
    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TreeTableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TreeTableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static <T> void autoFitTable(TreeTableView<T> view) {
        for (TreeTableColumn<T, ?> column : view.getColumns()) {
            try {
                columnToFitMethod.invoke(view.getSkin(), column, -1);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}

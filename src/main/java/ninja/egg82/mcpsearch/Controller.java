package ninja.egg82.mcpsearch;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jfoenix.controls.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyEvent;
import ninja.egg82.mcpsearch.data.CSVData;
import ninja.egg82.mcpsearch.data.SRGData;
import ninja.egg82.mcpsearch.data.TreeClass;
import ninja.egg82.mcpsearch.data.TreeField;
import ninja.egg82.mcpsearch.data.TreeMethod;
import ninja.egg82.mcpsearch.model.MCPVersionModel;
import ninja.egg82.mcpsearch.ovrd.FitWidthTableView;
import ninja.egg82.mcpsearch.utils.*;
import ninja.egg82.mcpsearch.utils.gui.SearchGUIUtil;
import ninja.egg82.mcpsearch.utils.gui.VersionGUIUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controller {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Timer searchTimer = new Timer();

    private ExecutorService workPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactoryBuilder().setNameFormat("Work-%d").build());

    public void setup() {
        versionTypeCombo.managedProperty().bind(versionTypeCombo.visibleProperty());
        searchText.textProperty().addListener((obj, oldValue, newValue) -> {
            searchTimer.cancel();
            searchTimer = new Timer();

            if (newValue.isEmpty()) {
                search(newValue);
            } else {
                searchTimer.schedule(new TimerTask() {
                    public void run() {
                        Platform.runLater(() -> search(newValue));
                    }
                }, 500L);
            }
        });

        JFXTreeTableColumn<TreeClass, String> classObfuscatedColumn = new JFXTreeTableColumn<>("Obfuscated");
        classObfuscatedColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("ObfuscatedName"));
        JFXTreeTableColumn<TreeClass, String> classSrgColumn = new JFXTreeTableColumn<>("SRG");
        classSrgColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("SRGName"));
        classesTable.getColumns().setAll(classObfuscatedColumn, classSrgColumn);

        JFXTreeTableColumn<TreeField, String> fieldObfuscatedColumn = new JFXTreeTableColumn<>("Obfuscated");
        fieldObfuscatedColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("ObfuscatedName"));
        JFXTreeTableColumn<TreeField, String> fieldSrgColumn = new JFXTreeTableColumn<>("SRG");
        fieldSrgColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("SRGName"));
        JFXTreeTableColumn<TreeField, String> fieldMappedColumn = new JFXTreeTableColumn<>("Mapped");
        fieldMappedColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("MappedName"));
        JFXTreeTableColumn<TreeField, String> fieldDescriptionColumn = new JFXTreeTableColumn<>("Description");
        fieldDescriptionColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("Description"));
        fieldsTable.getColumns().setAll(fieldObfuscatedColumn, fieldSrgColumn, fieldMappedColumn, fieldDescriptionColumn);

        JFXTreeTableColumn<String[], String> methodObfuscatedColumn = new JFXTreeTableColumn<>("Obfuscated");
        methodObfuscatedColumn.setCellValueFactory(data -> new SimpleStringProperty((data == null) ? null : (data.getValue() == null) ? null : (data.getValue().getValue() == null) ? null : data.getValue().getValue()[0]));
        /*methodObfuscatedColumn.setCellFactory(cell -> new JFXTreeTableCell<String[], String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                System.out.println(item + ": " + empty);
            }
        });*/
        JFXTreeTableColumn<String[], String> methodSrgColumn = new JFXTreeTableColumn<>("SRG");
        methodSrgColumn.setCellValueFactory(data -> new SimpleStringProperty((data == null) ? null : (data.getValue() == null) ? null : (data.getValue().getValue() == null) ? null : data.getValue().getValue()[1]));
        //methodSrgColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("SRGString"));
        JFXTreeTableColumn<String[], String> methodMappedColumn = new JFXTreeTableColumn<>("Mapped");
        methodMappedColumn.setCellValueFactory(data -> new SimpleStringProperty((data == null) ? null : (data.getValue() == null) ? null : (data.getValue().getValue() == null) ? null : data.getValue().getValue()[2]));
        //methodMappedColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("MappedString"));
        JFXTreeTableColumn<String[], String> methodDescriptionColumn = new JFXTreeTableColumn<>("Description");
        methodDescriptionColumn.setCellValueFactory(data -> new SimpleStringProperty((data == null) ? null : (data.getValue() == null) ? null : (data.getValue().getValue() == null) ? null : data.getValue().getValue()[3]));
        //methodDescriptionColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("Description"));
        methodsTable.getColumns().setAll(methodObfuscatedColumn, methodSrgColumn, methodMappedColumn, methodDescriptionColumn);

        classesTable.getSelectionModel().selectedIndexProperty().addListener((obj, oldSelection, newSelection) -> {
            if (newSelection == null || newSelection.intValue() == 0) {
                return;
            }

            TreeItem<TreeClass> item = classesTable.getTreeItem(newSelection.intValue());
            if (item == null) {
                return;
            }

            populateFields(item.getValue().getFields());
            populateMethods(item.getValue().getMethods());
        });
    }

    public void stop() {
        if (!workPool.isShutdown()) {
            workPool.shutdown();
            try {
                if (!workPool.awaitTermination(8L, TimeUnit.SECONDS)) {
                    workPool.shutdownNow();
                }
            } catch (InterruptedException ignored) {
                workPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // Version
    @FXML
    public JFXComboBox<String> versionCombo;
    @FXML
    public JFXComboBox<String> versionTypeCombo;
    @FXML
    public JFXButton reloadButton;
    @FXML
    public JFXSpinner versionSpinner;
    @FXML
    public Label versionLabel;

    public Map<String, MCPVersionModel> versionsModel = null;

    private DateFormat fromDateFormat = new SimpleDateFormat("yyyyMMdd");
    private DateFormat toDateFormat = new SimpleDateFormat("EEE MMM dd yyyy");

    @FXML
    public void onVersionSelect(ActionEvent actionEvent) {
        if (versionCombo.getValue() == null) {
            return;
        }

        versionTypeCombo.getItems().clear();

        searchText.setText("");
        searchText.setDisable(true);
        exactMatchCheckbox.setDisable(true);
        filterFieldsCheckbox.setDisable(true);
        filterMethodsCheckbox.setDisable(true);

        MCPVersionModel revisions = versionsModel.get(versionCombo.getValue());
        if (revisions == null) {
            return;
        }

        for (Short i : revisions.getStable()) {
            versionTypeCombo.getItems().add("Stable: rev-" + i);
        }
        for (Long i : revisions.getSnapshot()) {
            try {
                versionTypeCombo.getItems().add("Snapshot: " + toDateFormat.format(fromDateFormat.parse(String.valueOf(i))));
            } catch (ParseException ex) {
                logger.error("Could not parse date.", ex);
                AlertUtil.show(Alert.AlertType.ERROR, "Date Parser Error", ex.getMessage());
                break;
            }
        }

        versionTypeCombo.setVisible(true);
    }

    @FXML
    public void onVersionTypeSelect(ActionEvent actionEvent) {
        if (versionTypeCombo.getValue() == null) {
            return;
        }

        versionCombo.setDisable(true);
        versionTypeCombo.setDisable(true);
        reloadButton.setDisable(true);
        versionSpinner.setVisible(true);
        classesTable.setRoot(null);
        fieldsTable.setRoot(null);
        methodsTable.setRoot(null);

        String version = versionCombo.getValue();
        boolean isStable;
        String revision = versionTypeCombo.getValue();

        if (revision.startsWith("Stable")) {
            isStable = true;
            revision = revision.substring(12);
        } else {
            isStable = false;
            try {
                revision = fromDateFormat.format(toDateFormat.parse(revision.substring(10)));
            } catch (ParseException ex) {
                logger.error("Could not parse date.", ex);
                AlertUtil.show(Alert.AlertType.ERROR, "Date Parser Error", ex.getMessage());
                return;
            }
        }

        String csvUrl;
        String srgUrl;

        if (isStable) {
            csvUrl = "https://github.com/ModCoderPack/MCPMappingsArchive/raw/master/mcp_stable/" + revision + "-" + version + "/mcp_stable-" + revision + "-" + version + ".zip";
        } else {
            csvUrl = "https://github.com/ModCoderPack/MCPMappingsArchive/raw/master/mcp_snapshot/" + revision + "-" + version + "/mcp_snapshot-" + revision + "-" + version + ".zip";
        }

        int srgVersion;
        int[] versionParsed = VersionGUIUtil.parseVersion(version);
        if (versionParsed[0] == 1 && versionParsed[1] <= 12) {
            srgUrl = "https://github.com/ModCoderPack/MCPMappingsArchive/raw/master/mcp/" + version + "/mcp-" + version + "-srg.zip";
            srgVersion = 1;
        } else {
            srgUrl = "https://raw.githubusercontent.com/MinecraftForge/MCPConfig/master/versions/release/" + version + "/joined.tsrg";
            srgVersion = 2;
        }

        File versionDirectory;
        File revisionDirectory;
        try {
            versionDirectory = FileUtil.getVersionDirectory(version);
            revisionDirectory = FileUtil.getRevisionDirectory(version, revision, isStable);
        } catch (URISyntaxException ex) {
            logger.error("Could not get file.", ex);
            AlertUtil.show(Alert.AlertType.ERROR, "File Error", ex.getMessage());
            return;
        }

        if (revisionDirectory.exists() && !revisionDirectory.isDirectory()) {
            if (!revisionDirectory.delete()) {
                AlertUtil.show(Alert.AlertType.ERROR, "File Error", "Could not create folder structure.");
                return;
            }
        }

        if (!revisionDirectory.exists() && !revisionDirectory.mkdirs()) {
            AlertUtil.show(Alert.AlertType.ERROR, "File Error", "Could not create folder structure.");
            return;
        }

        File csvZip = new File(revisionDirectory, "csv.zip");
        File srgFile;
        if (srgVersion == 1) {
            srgFile = new File(versionDirectory, "srg.zip");
        } else if (srgVersion == 2) {
            srgFile = new File(versionDirectory, "joined.tsrg");
        } else {
            AlertUtil.show(Alert.AlertType.ERROR, "SRG Version Error", "Could not get SRG file for version specified.");
            return;
        }

        if (!csvZip.exists() || !srgFile.exists()) {
            workPool.submit(() -> {
                try {
                    if (!csvZip.exists()) {
                        Platform.runLater(() -> versionLabel.setText("Downloading CSV data"));
                        FileUtil.downloadFile(csvUrl, csvZip);
                    }
                    if (!srgFile.exists()) {
                        Platform.runLater(() -> versionLabel.setText("Downloading SRG data"));
                        FileUtil.downloadFile(srgUrl, srgFile);
                    }
                } catch (IOException ex) {
                    logger.error("Could not download file.", ex);
                    AlertUtil.show(Alert.AlertType.ERROR, "File Download Error", ex.getMessage());
                    return;
                }

                if (!csvZip.exists() || !srgFile.exists()) {
                    AlertUtil.show(Alert.AlertType.ERROR, "File Cache Error", "Could not fetch required files.");
                    return;
                }

                Platform.runLater(() -> {
                    extract(srgFile, srgVersion, csvZip);
                });
            });
        } else {
            extract(srgFile, srgVersion, csvZip);
        }
    }
    @FXML
    public void onVersionReload(ActionEvent actionEvent) {
        versionCombo.setDisable(true);
        versionCombo.getItems().clear();
        versionTypeCombo.setVisible(false);
        versionTypeCombo.getItems().clear();
        reloadButton.setDisable(true);
        versionSpinner.setVisible(true);
        versionLabel.setText("Fetching version list");

        searchText.setText("");
        searchText.setDisable(true);
        exactMatchCheckbox.setDisable(true);
        filterFieldsCheckbox.setDisable(true);
        filterMethodsCheckbox.setDisable(true);

        workPool.submit(() -> {
            VersionGUIUtil.getVersions(this);

            Platform.runLater(() -> {
                versionCombo.setDisable(false);
                reloadButton.setDisable(false);
                versionSpinner.setVisible(false);
                versionLabel.setText("");
            });
        });
    }

    // Search
    @FXML
    public JFXTextField searchText;
    @FXML
    public JFXCheckBox exactMatchCheckbox;
    @FXML
    public JFXCheckBox filterMethodsCheckbox;
    @FXML
    public JFXCheckBox filterFieldsCheckbox;

    @FXML
    public void onExactMatchSelect(ActionEvent actionEvent) {
        search(searchText.getText());
    }

    @FXML
    public void onFilterMethodsSelect(ActionEvent actionEvent) {
        search(searchText.getText());
    }

    @FXML
    public void onFilterFieldsSelect(ActionEvent actionEvent) {
        search(searchText.getText());
    }

    @FXML
    public void onKeyTyped(KeyEvent keyEvent) {
        if (!searchText.isDisable()) {
            if (keyEvent.getCharacter().equals("\b")) {
                searchText.setText(searchText.getText().substring(0, searchText.getLength() - 1));
            } else {
                searchText.setText(searchText.getText() + keyEvent.getCharacter());
            }

            searchText.requestFocus();
            searchText.selectEnd();
        }
    }

    private void search(String search) {
        if (search.isEmpty()) {
            populateClasses(rootTrees, false);
            return;
        }

        populateClasses(SearchGUIUtil.search(rootTrees, search, filterMethodsCheckbox.isSelected(), filterFieldsCheckbox.isSelected(), exactMatchCheckbox.isSelected()), false);
    }

    // Data
    @FXML
    public FitWidthTableView classesTable;
    @FXML
    public FitWidthTableView methodsTable;
    @FXML
    public FitWidthTableView fieldsTable;

    private CSVData csvData;
    private SRGData srgData;
    private Map<String, TreeClass> rootTrees = new HashMap<>();

    private void extract(File srgFile, int srgVersion, File csvZip) {
        File srgJoinedFile;

        if (srgVersion == 1) {
            srgJoinedFile = new File(srgFile.getParent(), "joined.srg");
        } else {
            srgJoinedFile = srgFile;
        }

        File srgDataFile = new File(srgFile.getParent(), "data.json");

        if (srgVersion == 1 && !srgJoinedFile.exists()) {
            versionLabel.setText("Extracting SRG data");
            workPool.submit(() -> {
                try {
                    if (!srgJoinedFile.exists()) {
                        FileUtil.extractFile(srgFile, "joined.srg", srgJoinedFile);
                    }
                } catch (IOException ex) {
                    AlertUtil.show(Alert.AlertType.ERROR, "File Extraction Error", ex.getMessage());
                    return;
                }

                Platform.runLater(() -> {
                    versionLabel.setText("Loading SRG data");
                });
                if (!srgDataFile.exists()) {
                    srgData = new SRGData(srgJoinedFile, srgVersion);
                    try {
                        JSONUtil.write(srgData.serialize(), srgDataFile);
                    } catch (IOException ex) {
                        logger.error("Could not write file.", ex);
                        AlertUtil.show(Alert.AlertType.ERROR, "Error Writing Data", ex.getMessage());
                        return;
                    }
                } else {
                    try {
                        srgData = new SRGData(JSONUtil.readObject(srgDataFile));
                    } catch (IOException ex) {
                        logger.error("Could not write file.", ex);
                        AlertUtil.show(Alert.AlertType.ERROR, "Error Reading Data", ex.getMessage());
                        return;
                    } catch (org.json.simple.parser.ParseException ex) {
                        logger.error("Could not read file.", ex);
                        AlertUtil.show(Alert.AlertType.ERROR, "Error Reading Data", ex.getMessage());
                        return;
                    }
                }
                Platform.runLater(() -> extractCSV(csvZip));
            });
        } else {
            versionLabel.setText("Loading SRG data");
            workPool.submit(() -> {
                if (!srgDataFile.exists()) {
                    srgData = new SRGData(srgJoinedFile, srgVersion);
                    try {
                        JSONUtil.write(srgData.serialize(), srgDataFile);
                    } catch (IOException ex) {
                        logger.error("Could not write file.", ex);
                        AlertUtil.show(Alert.AlertType.ERROR, "Error Writing Data", ex.getMessage());
                        return;
                    }
                } else {
                    try {
                        srgData = new SRGData(JSONUtil.readObject(srgDataFile));
                    } catch (IOException ex) {
                        logger.error("Could not read file.", ex);
                        AlertUtil.show(Alert.AlertType.ERROR, "Error Reading Data", ex.getMessage());
                        return;
                    } catch (org.json.simple.parser.ParseException ex) {
                        logger.error("Could not read file.", ex);
                        AlertUtil.show(Alert.AlertType.ERROR, "Error Reading Data", ex.getMessage());
                        return;
                    }
                }
                Platform.runLater(() -> extractCSV(csvZip));
            });
        }
    }

    private void extractCSV(File csvFile) {
        File fieldsFile = new File(csvFile.getParent(), "fields.csv");
        File methodsFile = new File(csvFile.getParent(), "methods.csv");
        File paramsFile = new File(csvFile.getParent(), "params.csv");

        File csvDataFile = new File(csvFile.getParent(), "data.json");

        if (!fieldsFile.exists() || !methodsFile.exists() || !paramsFile.exists()) {
            versionLabel.setText("Extracting CSV data");
            workPool.submit(() -> {
                try {
                    if (!fieldsFile.exists()) {
                        FileUtil.extractFile(csvFile, "fields.csv", fieldsFile);
                    }
                    if (!methodsFile.exists()) {
                        FileUtil.extractFile(csvFile, "methods.csv", methodsFile);
                    }
                    if (!paramsFile.exists()) {
                        FileUtil.extractFile(csvFile, "params.csv", paramsFile);
                    }
                } catch (IOException ex) {
                    AlertUtil.show(Alert.AlertType.ERROR, "File Extraction Error", ex.getMessage());
                    return;
                }

                Platform.runLater(() -> {
                    versionLabel.setText("Loading CSV data");
                });
                if (!csvDataFile.exists()) {
                    csvData = new CSVData(fieldsFile,  methodsFile, paramsFile);
                    try {
                        JSONUtil.write(csvData.serialize(), csvDataFile);
                    } catch (IOException ex) {
                        logger.error("Could not write file.", ex);
                        AlertUtil.show(Alert.AlertType.ERROR, "Error Writing Data", ex.getMessage());
                        return;
                    }
                } else {
                    try {
                        csvData = new CSVData(JSONUtil.readObject(csvDataFile));
                    } catch (IOException ex) {
                        logger.error("Could not read file.", ex);
                        AlertUtil.show(Alert.AlertType.ERROR, "Error Reading Data", ex.getMessage());
                        return;
                    } catch (org.json.simple.parser.ParseException ex) {
                        logger.error("Could not read file.", ex);
                        AlertUtil.show(Alert.AlertType.ERROR, "Error Reading Data", ex.getMessage());
                        return;
                    }
                }
                Platform.runLater(() -> loadData(csvFile));
            });
        } else {
            versionLabel.setText("Loading CSV data");
            workPool.submit(() -> {
                if (!csvDataFile.exists()) {
                    csvData = new CSVData(fieldsFile,  methodsFile, paramsFile);
                    try {
                        JSONUtil.write(csvData.serialize(), csvDataFile);
                    } catch (IOException ex) {
                        logger.error("Could not write file.", ex);
                        AlertUtil.show(Alert.AlertType.ERROR, "Error Writing Data", ex.getMessage());
                        return;
                    }
                } else {
                    try {
                        csvData = new CSVData(JSONUtil.readObject(csvDataFile));
                    } catch (IOException ex) {
                        logger.error("Could not read file.", ex);
                        AlertUtil.show(Alert.AlertType.ERROR, "Error Reading Data", ex.getMessage());
                        return;
                    } catch (org.json.simple.parser.ParseException ex) {
                        logger.error("Could not read file.", ex);
                        AlertUtil.show(Alert.AlertType.ERROR, "Error Reading Data", ex.getMessage());
                        return;
                    }
                }
                Platform.runLater(() -> loadData(csvFile));
            });
        }
    }

    private void loadData(File csvFile) {
        workPool.submit(() -> {
            rootTrees.clear();

            File treeDataFile = new File(csvFile.getParent(), "tree.json");
            if (!treeDataFile.exists()) {
                Platform.runLater(() -> versionLabel.setText("Parsing data: 0%"));

                int current = 0;

                for (Map.Entry<String, String> kvp : srgData.classes.entrySet()) {
                    TreeClass clazz;

                    int dollar = kvp.getKey().indexOf('$');
                    if (dollar > -1) {
                        String[] subClasses = kvp.getKey().split("\\$");
                        //System.out.println(kvp.getKey() + ": " + Arrays.toString(subClasses));
                        String currentName = subClasses[0];
                        clazz = rootTrees.get(currentName);
                        if (clazz == null) {
                            //System.out.println(kvp.getKey() + ": " + currentName + ": " + Arrays.toString(subClasses));
                            clazz = new TreeClass(currentName, srgData.classes.get(currentName));
                            rootTrees.put(currentName, clazz);
                        }
                        for (int i = 1; i < subClasses.length; i++) {
                            currentName += "$" + subClasses[i];
                            boolean found = false;
                            for (TreeClass clazz2 : clazz.getClasses()) {
                                if (clazz2.getObfuscatedName().equals(currentName)) {
                                    //System.out.println(kvp.getKey() + ": " + currentName + ": " + Arrays.toString(subClasses));
                                    clazz = clazz2;
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                //System.out.println(kvp.getKey() + ": " + currentName + ": " + Arrays.toString(subClasses));
                                TreeClass clazz2 = new TreeClass(currentName, srgData.classes.get(currentName));
                                clazz.getClasses().add(clazz2);
                                clazz = clazz2;
                            }
                        }
                    } else {
                        clazz = new TreeClass(kvp.getKey(), kvp.getValue());
                        TreeClass oldClass = rootTrees.putIfAbsent(kvp.getKey(), clazz);
                        if (oldClass != null) {
                            clazz = oldClass;
                        }
                        //System.out.println(kvp.getKey());
                    }

                    current++;
                    if (current % 10 == 0) {
                        double currentThreaded = current;
                        Platform.runLater(() -> versionLabel.setText("Parsing data: " + (int) Math.floor(currentThreaded / (double) srgData.classes.size() * 100.0d) + "%"));
                    }

                    //System.out.println(clazz.getObfuscatedName());

                    for (Map.Entry<String, String> kvp2 : srgData.fields.entrySet()) {
                        if (!kvp2.getKey().substring(0, kvp2.getKey().lastIndexOf('/')).equals(kvp.getKey())) {
                            continue;
                        }

                        String[] mappedData = csvData.fields.get(kvp2.getValue());
                        //System.out.println(kvp2.getKey() + ": " + kvp2.getValue().substring(kvp2.getValue().lastIndexOf('/') + 1) + ": " + Arrays.toString(mappedData));
                        clazz.getFields().add(new TreeField(kvp2.getKey(), kvp2.getValue().substring(kvp2.getValue().lastIndexOf('/') + 1), (mappedData != null) ? mappedData[0] : null, (mappedData != null) ? mappedData[1] : null));
                    }
                    for (Map.Entry<String, String> kvp2 : srgData.methods.entrySet()) {
                        if (!kvp2.getKey().substring(0, kvp2.getKey().lastIndexOf('/')).equals(kvp.getKey())) {
                            continue;
                        }

                        String[] funcParts = kvp2.getValue().split("_");
                        int funcNum = (funcParts.length > 1) ? Integer.parseInt(funcParts[1]) : -1;

                        String[] mappedData = csvData.methods.get(kvp2.getValue());
                        //System.out.println(kvp2.getKey() + ": " + kvp2.getValue().substring(kvp2.getValue().lastIndexOf('/') + 1) + ": " + Arrays.toString(mappedData));
                        String[] params = srgData.params.get(kvp2.getValue());
                        String[] paramValues = new String[params.length];
                        String[] mappedParams = new String[params.length];
                        String output = params[params.length - 1];
                        params = remove(params);
                        boolean plusOne = !csvData.params.containsKey("p_" + funcNum + "_0_") && csvData.params.containsKey("p_" + funcNum + "_1_");
                        for (int i = 0; i < params.length; i++) {
                            String value = (funcNum > -1) ? "p_" + funcNum + "_" + (i + (plusOne ? 1 : 0)) + "_" : null;
                            paramValues[i] = value;
                            mappedParams[i] = (value != null) ? csvData.params.get(value) : null;
                        }
                        clazz.getMethods().add(new TreeMethod(kvp2.getKey(), kvp2.getValue().substring(kvp2.getValue().lastIndexOf('/') + 1), (mappedData != null) ? mappedData[0] : null, params, paramValues, mappedParams, output, (mappedData != null) ? mappedData[1] : null));
                    }
                }

                JSONArray out = new JSONArray();
                for (TreeClass clazz : rootTrees.values()) {
                    out.add(clazz.serialize());
                }

               try {
                    JSONUtil.write(out, treeDataFile);
                } catch (IOException ex) {
                   logger.error("Could not write file.", ex);
                    AlertUtil.show(Alert.AlertType.ERROR, "Error Writing Data", ex.getMessage());
                    return;
                }
            } else {
                Platform.runLater(() -> versionLabel.setText("Loading tree data"));

                JSONArray treeData;

                try {
                    treeData = JSONUtil.readArray(treeDataFile);
                } catch (IOException ex) {
                    logger.error("Could not read file.", ex);
                    AlertUtil.show(Alert.AlertType.ERROR, "Error Reading Data", ex.getMessage());
                    return;
                } catch (org.json.simple.parser.ParseException ex) {
                    logger.error("Could not read file.", ex);
                    AlertUtil.show(Alert.AlertType.ERROR, "Error Reading Data", ex.getMessage());
                    return;
                }

                for (Object o : treeData) {
                    TreeClass clazz = new TreeClass((JSONObject) o);
                    rootTrees.put(clazz.getObfuscatedName(), clazz);
                }
            }

            Platform.runLater(() -> {
                populateClasses(rootTrees, true);
            });
        });
    }

    private void populateClasses(Map<String, TreeClass> rootTrees, boolean fitColumns) {
        TreeClass selected = classesTable.getSelectionModel().getSelectedItem() != null ? ((TreeItem<TreeClass>) classesTable.getSelectionModel().getSelectedItem()).getValue() : null;

        TreeItem<TreeClass> classRoot = new TreeItem<>();
        for (TreeClass clazz : rootTrees.values()) {
            classRoot.getChildren().add(getNode(clazz));
        }
        classRoot.setExpanded(true);
        classesTable.setRoot(classRoot);
        int selectionIndex = (selected != null) ? getIndex(selected, classRoot) : -1;
        if (selectionIndex == -1) {
            fieldsTable.setRoot(null);
            methodsTable.setRoot(null);
        } else {
            classesTable.getSelectionModel().select(selectionIndex);
        }

        versionCombo.setDisable(false);
        versionTypeCombo.setDisable(false);
        reloadButton.setDisable(false);
        versionSpinner.setVisible(false);
        versionLabel.setText("");

        if (fitColumns) {
            searchText.setText("");
            searchText.setDisable(false);
            exactMatchCheckbox.setDisable(false);
            filterFieldsCheckbox.setDisable(false);
            filterMethodsCheckbox.setDisable(false);
        }
    }

    private int getIndex(TreeClass previous, TreeItem<TreeClass> root) {
        for (TreeItem<TreeClass> clazz : root.getChildren()) {
            if (clazz.getValue() != null && previous.getObfuscatedName().equals(clazz.getValue().getObfuscatedName())) {
                return classesTable.getRow(clazz);
            }

            int retVal = getIndex(previous, clazz);
            if (retVal > -1) {
                return retVal;
            }
        }

        return -1;
    }

    private void populateFields(List<TreeField> fields) {
        TreeItem<TreeField> fieldRoot = new TreeItem<>();
        for (TreeField field : fields) {
            fieldRoot.getChildren().add(new TreeItem<>(field));
        }
        fieldRoot.setExpanded(true);
        fieldsTable.setRoot(fieldRoot);
    }

    private void populateMethods(List<TreeMethod> methods) {
        TreeItem<String[]> methodRoot = new TreeItem<>();
        for (TreeMethod method : methods) {
            String[] methodColumns = new String[] {
                    method.getObfuscatedName(),
                    method.getSRGString(),
                    method.getMappedString(),
                    method.getDescription()
            };

            TreeItem<String[]> item = new TreeItem<>(methodColumns);
            for (int i = 0; i < method.getSRGInput().length; i++) {
                String[] itemColumns = new String[4];
                itemColumns[0] = "";
                itemColumns[1] = method.getSRGValues()[i] != null ? method.getSRGValues()[i] : "?";
                itemColumns[2] = (method.getMappedInput() != null && i < method.getMappedInput().length) ? method.getMappedInput()[i] : method.getSRGValues()[i];
                itemColumns[3] = "";
                item.getChildren().add(new TreeItem<>(itemColumns));
            }
            methodRoot.getChildren().add(item);
        }
        methodRoot.setExpanded(true);
        methodsTable.setRoot(methodRoot);
    }

    private TreeItem<TreeClass> getNode(TreeClass clazz) {
        TreeItem<TreeClass> retVal = new TreeItem<>(clazz);
        for (TreeClass clazz2 : clazz.getClasses()) {
            retVal.getChildren().add(getNode(clazz2));
        }
        retVal.setExpanded(true);
        return retVal;
    }

    private String[] remove(String[] input) {
        String[] retVal = new String[input.length - 1];
        System.arraycopy(input, 0, retVal, 0, input.length - 1);
        return retVal;
    }
}

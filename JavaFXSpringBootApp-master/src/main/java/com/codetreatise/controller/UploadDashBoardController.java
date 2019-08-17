package com.codetreatise.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import com.amazonaws.services.s3.AmazonS3;
import com.codetreatise.awss3service.S3Application;
import com.codetreatise.bean.OrderData;
import com.codetreatise.bean.PhotoFile;
import com.codetreatise.config.StageManager;
import com.codetreatise.repository.OrderRepository;
import com.codetreatise.utility.DataLoader;
import com.codetreatise.view.FxmlView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;

@Controller
public class UploadDashBoardController implements Initializable {
	
	@FXML
    private Button btnLogout;
	
	@FXML
    private Button newOrder;
	
	@FXML
    private Label userId;
    
	@FXML
    private Label uploadAlbum;
	
    @FXML
    private ComboBox<String> cbSize;
    
    @FXML
    private ComboBox<String> cbAlbumType;
    
    @FXML
    private ComboBox<String> cbOrderType;
    
    @FXML
    private ComboBox<String> cbCoverType;
    
    @FXML
    private RadioButton rbExpress;
    
    @FXML
    private RadioButton rbNormal;
    
    @FXML
    private TextArea textArea;
    
    @FXML
    private Button reset;
	
	@FXML
    private Button selectFiles;
	
	@FXML
    private Button startUpload;
	
	@FXML
    private Button cancelUpload;
	
	@FXML
	private ProgressBar progressBar;
	
	@FXML
	private TableView<PhotoFile> fileTable;

	@FXML
	private TableColumn<PhotoFile, Long> colId;

	@FXML
	private TableColumn<PhotoFile, String> colFileName;

	@FXML
	private TableColumn<PhotoFile, String> colSize;
	
	@FXML
	private TableColumn<PhotoFile, String> colStatus;
	
	@FXML
    private TableColumn<PhotoFile, Boolean> colEdit;
	
	@FXML
    private ImageView logoImage;
	
	@FXML
    private MenuItem deleteFiles;
	
	@FXML
    private ToggleGroup toggleGroup;
	
	@Lazy
    @Autowired
    private StageManager stageManager;
	
	@Autowired
	private S3Application s3Application;
	
	@Autowired
	private OrderRepository orderRepositry;
	
	static double ii = 0;
	
	private AmazonS3 amazonS3Conn;
	boolean flag = false;
	boolean inturruptFlag = true;
	private Thread one;
	private String finalStatus;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		DataLoader newDataLoader = new DataLoader();
		cbSize.setItems(newDataLoader.getSizeList());
		cbAlbumType.setItems(newDataLoader.getAlbumType());
		cbOrderType.setItems(newDataLoader.getOrderType());
		cbCoverType.setItems(newDataLoader.getCoverType());
		
		Image image = null;
		image = new Image(getClass().getResourceAsStream("/images/logo.jpg"));
		logoImage.setImage(image);
		logoImage.setPreserveRatio(true);
		logoImage.setSmooth(true);
		logoImage.setCache(true);
		fileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		setColumnProperties();
	}
    
    @FXML
    private void startUpload(ActionEvent event) throws InterruptedException {
    	OrderData orderData = new OrderData();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		orderData.setAlbumType(cbAlbumType.getValue());
		orderData.setOrderType(cbOrderType.getValue());
		orderData.setCoverType(cbCoverType.getValue());
		orderData.setCustomerId("");
		orderData.setDate(timestamp);
		orderData.setComments(textArea.getText());
		orderData.setDwnloadUrl("");
		orderData.setSize(cbSize.getValue());
		orderData.setSpeed("Normal");
		orderData.setStatus("Uploaded");
		String orderType = cbOrderType.getValue();
    	
		actionButtonsEnableDisableAfterUpload(true);
    	if (amazonS3Conn == null) {
    		amazonS3Conn = s3Application.createConnection();
    	}
    	double adder = (fileTable.getItems().size()*0.01);
    	one = new Thread () {
    		public void run () {
    			if (!fileTable.getItems().isEmpty()) {
    				// Crete a zip file and get a zip file
    				
    				// pass it to uploader
            		for (int i =0;i<fileTable.getItems().size();i++) {
		        			if ("Pending".equalsIgnoreCase(fileTable.getItems().get(i).getStatus())) {
		        			try {
		        					flag = s3Application.startUploadActivity(fileTable.getItems().get(i),amazonS3Conn);
		        				}
							 catch (InterruptedException e) {
								e.printStackTrace();
							}
		        			ii += adder;
		        			if (flag) {
		        				fileTable.getItems().get(i).setStatus("Completed");
		        			} else {
		        				fileTable.getItems().get(i).setStatus("Error");
		        			}
		        			System.out.println(ii);
		        			progressBar.setProgress(ii);
		        			fileTable.refresh();
		        		}
            		}
            		progressBar.setProgress(1.0);
    			}
    		}
    	};
    	one.start();
    	one.join();
		if (flag) {
			OrderData resp = orderRepositry.save(orderData);
			setFinalStatus("Success");
			Alert alert = null;
			alert = new Alert(AlertType.INFORMATION, "Data Uploaded sucessfully ! Your order id is "+resp.getId(), ButtonType.OK);
			alert.showAndWait();
			actionButtonsEnableDisableAfterUpload(false);
		} else {
			setFinalStatus("Error");
			Alert alert = null;
			alert = new Alert(AlertType.ERROR, "Error in data upload please try again !", ButtonType.OK);
			actionButtonsEnableDisableAfterUpload(false);
		}
    }
    
    @FXML
    private void cancelUpload(ActionEvent event) {
    	//TODO Delete the existing uploaded
    	one.interrupt();
    }
    
	
	
	@FXML
	private void exit(ActionEvent event) {
		Platform.exit();
    }
	
	@FXML
    private void selectFiles(ActionEvent event) throws IOException {
		ObservableList<PhotoFile> fileList = FXCollections.observableArrayList();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select JPEG files");
		fileChooser.setInitialDirectory(new File("C:\\"));
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("JPEG Files", "*.jpg"));
		 
		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
		for (int i=0; i<selectedFiles.size(); i++) {
			PhotoFile photofile = new PhotoFile();
			photofile.setFileName(selectedFiles.get(i).getName());
			photofile.setId(new Long(i));
			photofile.setSize(String.valueOf((selectedFiles.get(i).length()/1024)/1024)+" MB");
			photofile.setFullPath(selectedFiles.get(i).getAbsolutePath());
			photofile.setStatus("Pending");
			fileList.add(photofile);
		}
		
		fileTable.setItems(fileList);
		fileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		fileTable.refresh();
		actionButtonsEnableDisable(false);
	}
	
	@FXML
    private void deleteFiles(ActionEvent event) throws IOException {
		
	}
	
	/**
	 * Logout and go to the login page
	 */
    @FXML
    private void logout(ActionEvent event) throws IOException {
    	stageManager.switchScene(FxmlView.LOGIN);    
    	//TODO need to stop uploading process
    }
    
    /**
	 * It will create the new Order and delete the existing data 
	 */
    @FXML
    private void newOrder(ActionEvent event) throws IOException {
    	fileTable.setItems(null);
		fileTable.refresh();
		cbSize.getSelectionModel().clearSelection();
		cbAlbumType.getSelectionModel().clearSelection();
		cbOrderType.getSelectionModel().clearSelection();
		cbCoverType.getSelectionModel().clearSelection();
		selectFiles.setDisable(false);
		textArea.setText("");
		progressBar.setProgress(0);
		reset.setDisable(false);
    }
    
    @FXML
    void reset(ActionEvent event) {
    	fileTable.setItems(null);
		fileTable.refresh();
		actionButtonsEnableDisable(true);
    }
	
	/**
	 * For Setting data table column property values
	 */
	private void setColumnProperties(){
		colId.setCellValueFactory(new PropertyValueFactory<>("id"));
		colFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
		colSize.setCellValueFactory(new PropertyValueFactory<>("size"));
		colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
		//colEdit.setCellFactory(cellFactory);
	}
	
	/**
	 * This method will enable/disable Upload and Cancel Button as it required.
	 * @param flag
	 */
	private void actionButtonsEnableDisable(boolean flag) {
		startUpload.setDisable(flag);
		cancelUpload.setDisable(flag);
	}
	
	private void actionButtonsEnableDisableAfterUpload(boolean flag) {
		startUpload.setDisable(flag);
		cancelUpload.setDisable(!flag);
		selectFiles.setDisable(flag);
		reset.setDisable(flag);
		newOrder.setDisable(flag);
	}
	
	
	Callback<TableColumn<PhotoFile, Boolean>, TableCell<PhotoFile, Boolean>> cellFactory = 
			new Callback<TableColumn<PhotoFile, Boolean>, TableCell<PhotoFile, Boolean>>() {
		@Override
		public TableCell<PhotoFile, Boolean> call( final TableColumn<PhotoFile, Boolean> param)
		{
			final TableCell<PhotoFile, Boolean> cell = new TableCell<PhotoFile, Boolean>()
			{
				Image imgEdit = new Image(getClass().getResourceAsStream("/images/edit.png"));
				final Button btnEdit = new Button();
				
				@Override
				public void updateItem(Boolean check, boolean empty)
				{
					super.updateItem(check, empty);
					if(empty)
					{
						setGraphic(null);
						setText(null);
					}
					else{
						btnEdit.setOnAction(e -> {
							PhotoFile file = getTableView().getItems().get(getIndex());
							updatePhotoFile(file);
						});
						
						btnEdit.setStyle("-fx-background-color: transparent;");
						ImageView iv = new ImageView();
				        iv.setImage(imgEdit);
				        iv.setPreserveRatio(true);
				        iv.setSmooth(true);
				        iv.setCache(true);
						btnEdit.setGraphic(iv);
						
						setGraphic(btnEdit);
						setAlignment(Pos.CENTER);
						setText(null);
					}
				}

				private void updatePhotoFile(PhotoFile file) {
					
				}
			};
			return cell;
		}
	};

	public String getFinalStatus() {
		return finalStatus;
	}

	public void setFinalStatus(String finalStatus) {
		this.finalStatus = finalStatus;
	}

}

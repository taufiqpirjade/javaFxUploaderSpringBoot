package com.codetreatise.utility;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DataLoader {
	private ObservableList<String> sizeList = FXCollections.observableArrayList("8x12", "12x18");
	private ObservableList<String> orderType = FXCollections.observableArrayList("Karizma", "NT", "General");
	private ObservableList<String> albumType = FXCollections.observableArrayList("Matt", "Glossy", "3D");
	private ObservableList<String> coverType = FXCollections.observableArrayList("3D", "Embossed", "Noraml");
	
	public ObservableList<String> getOrderType() {
		return orderType;
	}

	public void setOrderType(ObservableList<String> orderType) {
		this.orderType = orderType;
	}

	public ObservableList<String> getAlbumType() {
		return albumType;
	}

	public void setAlbumType(ObservableList<String> albumType) {
		this.albumType = albumType;
	}

	public ObservableList<String> getCoverType() {
		return coverType;
	}

	public void setCoverType(ObservableList<String> coverType) {
		this.coverType = coverType;
	}

	public ObservableList<String> getSizeList() {
		return sizeList;
	}

	public void setSizeList(ObservableList<String> sizeList) {
		this.sizeList = sizeList;
	}
	
	

}

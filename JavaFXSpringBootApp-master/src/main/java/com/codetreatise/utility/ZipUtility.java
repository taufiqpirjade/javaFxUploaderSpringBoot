package com.codetreatise.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.codetreatise.bean.PhotoFile;

import javafx.scene.control.TableView;

public class ZipUtility {
	
	public void createZipFile(TableView<PhotoFile> photoFile) throws IOException {
		FileOutputStream fos = new FileOutputStream("C:\\Users\\admin\\Documents\\flukesoft\\sample.zip");
        ZipOutputStream zipOS = new ZipOutputStream(fos);
        for (int i =0;i<photoFile.getItems().size();i++) {
        	String[] stringArray = photoFile.getItems().get(i).getFullPath().split("\\");
        	writeToZipFile(photoFile.getItems().get(i).getFullPath(), zipOS,stringArray[stringArray.length-1]);
        }
	}
	
	public void writeToZipFile(String path, ZipOutputStream zipStream,String filename)
            throws FileNotFoundException, IOException {

        System.out.println("Writing file : '" + path + "' to zip file");

        File aFile = new File(path);
        FileInputStream fis = new FileInputStream(aFile);
        ZipEntry zipEntry = new ZipEntry(filename);
        zipStream.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipStream.write(bytes, 0, length);
        }

        zipStream.closeEntry();
        fis.close();
    }

}

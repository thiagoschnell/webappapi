package com.after_project.webappapi;
// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
public class FileManager {
    protected String getFileData(File file) throws Exception {
        return readFile(file);
    }
    protected byte[] getFileBytes(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] b = readBytes(fis);
        fis.close();
        return b;
    }
    protected File getFile(File fileObject, String filename){
        File file =  new File(fileObject, filename);
        file.getParentFile().mkdirs();
        return file;
    }
    protected void saveFile(File file, String data) throws Exception{
        saveFile(file,data.getBytes());
    }
    protected void saveFile(File file, byte[] bytes) throws Exception{
        FileOutputStream fos = new FileOutputStream(file,false);
        fos.write(bytes);
        fos.close();
    }
    private String readFile(File file) throws Exception{
        StringBuilder text = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            text.append(line);
            text.append('\n');
        }
        br.close();
        return text.toString();
    }
    protected String loadData(File file) throws Exception{
        FileInputStream fis = new FileInputStream(file);
        //fis = openFileInput("");
        StringBuffer fileContent = new StringBuffer("");
        byte[] buffer = new byte[1024];
        int n;
        while ((n = fis.read(buffer)) != -1)
        {
            fileContent.append(new String(buffer, 0, n));
        }
        fis.close();
        return fileContent.toString();
    }
    protected String readData(File file) throws Exception{
        StringBuffer datax = new StringBuffer("");
        FileInputStream fIn = new FileInputStream(file);
        //  FileInputStream fIn = openFileInput("");
        InputStreamReader isr = new InputStreamReader(fIn);
        BufferedReader buffreader = new BufferedReader(isr);
        String readString = buffreader.readLine();
        while (readString != null) {
            datax.append(readString);
            readString = buffreader.readLine();
        }
        isr.close() ;
        fIn.close();
        return datax.toString();
    }
    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
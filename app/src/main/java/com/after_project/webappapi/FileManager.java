package com.after_project.webappapi;
// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
public class FileManager implements AutoCloseable {
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
    protected File getDir(File fileObject, String dir){
        File file =  new File(fileObject, dir);
        file.mkdirs();
        return file;
    }
    protected void deleteFile(File file) throws Exception {
        if (file.exists()){
            if (file.isDirectory()) {
                for (File ct : file.listFiles()){
                    deleteFile(ct);
                }
            }
            if (!file.delete()) {
                throw new IOException("Could not delete the file " + file.getAbsolutePath());
            }
        }else{
            throw new FileNotFoundException("Could not find this file " + file.getAbsolutePath());
        }
    }
    protected InputStream getInputStream(byte[] bytes){
        return new ByteArrayInputStream(bytes);
    }
    protected String getChecksum(String algorithm, byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        InputStream i = new ByteArrayInputStream(bytes);
        md.update(readBytes(i));
        byte[] mdsum = md.digest();
        BigInteger bigInt = new BigInteger(1, mdsum);
        String output = bigInt.toString(16);
        output = String.format("%32s", output).replace(' ', '0');
        return output;
    }
    protected InputStream getInputStream(File file) throws Exception {
        return new FileInputStream(file);
    }
    protected void saveFile(File file, String data) throws Exception{
        saveFile(file,data.getBytes());
    }
    protected void saveFile(File file, byte[] bytes) throws Exception{
        FileOutputStream fos = new FileOutputStream(file,false);
        fos.write(bytes);
        fos.close();
    }
    protected void saveFile(File file, InputStream is) throws Exception{
        saveFile(file,readBytes(is));
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
    protected void unzip(byte[] data, File path) throws Exception{
        InputStream is = new ByteArrayInputStream(data);
        unzip(is,path);
    }
    protected void unzip(InputStream is, File path) throws Exception{
        ZipInputStream zis = null;
        FileOutputStream fos = null;
        try {
            zis = new ZipInputStream(is);
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                File entryFile;
                if (ze.isDirectory()) {
                    getDir(path,ze.getName());
                } else {
                    entryFile = getFile(path,ze.getName());
                    fos = new FileOutputStream(entryFile);
                    copyStream(zis, fos);
                    fos.close();
                    fos = null;
                }
                zis.closeEntry();
            }
        } finally {
            if (zis != null) {
                zis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
    private void writeBytes(byte[] data, OutputStream output) throws IOException {
        if (data != null) {
            output.write(data);
        }
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
    private int copyStream(InputStream input, OutputStream output) throws IOException {
        long count = copyBigStream(input, output);
        return count > 2147483647L ? -1 : (int)count;
    }
    private long copyStream(InputStream input, OutputStream output, int bufferSize) throws IOException {
        return copyBigStream(input, output, new byte[bufferSize]);
    }
    private long copyBigStream(InputStream input, OutputStream output) throws IOException {
        return copyStream(input, output, 4096);
    }
    private long copyBigStream(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count;
        int n;
        for(count = 0L; -1 != (n = input.read(buffer)); count += (long)n) {
            output.write(buffer, 0, n);
        }
        return count;
    }
    @Override
    public void close() throws Exception {
    }
}
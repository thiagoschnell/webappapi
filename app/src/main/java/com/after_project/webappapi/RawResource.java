package com.after_project.webappapi;
// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
import java.io.InputStream;
public class RawResource {
    private String rawData = null;
    RawResource(android.content.res.Resources resources, int resource_id) throws Exception {
        readBytes( resources.openRawResource(resource_id) );
    }
    RawResource(android.content.res.AssetManager assetManager, String AssetFilename) throws Exception {
        readBytes( assetManager.open(AssetFilename));
    }
    private void readBytes(InputStream file) throws Exception{
        byte[] b = new byte[file.available()];
        file.read(b);
        file.close();
        rawData = new String(b);
    }
    @Override
    public String toString() {
        return rawData;
    }
}
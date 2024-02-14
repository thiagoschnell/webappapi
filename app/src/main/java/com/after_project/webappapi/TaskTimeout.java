package com.after_project.webappapi;
// Copyright (c) Thiago Schnell.
// Licensed under the MIT License.
import android.os.AsyncTask;
public class TaskTimeout extends AsyncTask<Void,Void,String> {
    private long startTime = 0;
    private long timeout = 0;
    private long endTime = 0;
    private TaskTimeoutCallback taskTimeoutCallback = null;
    TaskTimeout(){
    }
    TaskTimeout(TaskTimeoutCallback taskTimeoutCallback){
        this.taskTimeoutCallback = taskTimeoutCallback;
    }
    private long getTickCount(){
        return System.currentTimeMillis();
    }
    protected long getTimeout() {
        return timeout;
    }
    protected void setTimeout(long timeout){
        if(timeout > 0){
            endTime= getTickCount();
            startTime = getTickCount();
            this.timeout = timeout;
        }else if(timeout==0){
            reset();
        }
    }
    protected void reset(){
        this.timeout = 0;
        this.endTime = 0;
        this.startTime = 0;
    }
    @Override
    protected String doInBackground(Void... voids) {
        while(true){
            while(endTime - startTime  < timeout){
                endTime= getTickCount();
                try {
                    Thread.sleep(100);//power savings - cpu usage
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if(timeout!=0){
                timeout = 0;
                if(this.taskTimeoutCallback!=null){
                    taskTimeoutCallback.onTimeoutReached();
                }
            }else if(timeout==0){
                try {
                    Thread.sleep(500);//idle time
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if(1==2)break;
        }
        return null;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
    protected interface TaskTimeoutCallback {
        void onTimeoutReached();
    }
}
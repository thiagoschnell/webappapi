package com.after_project.webappapi;
// Copyright (c) Thiago Schnell | https://github.com/thiagoschnell/webappapi/blob/main/LICENSE
// Licensed under the MIT License.
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
public class Thread2 extends Thread{
    Runnable r;
    ExecutorService es = Executors.newSingleThreadExecutor();
    Thread2(Runnable r){
        this.r = r;
    }
    @Override
    public void run() {
        exec();
    }
    @Override
    public synchronized void start() {
        exec();
    }
    void exec(){
        {
            try {
                es.execute(new Thread(() -> {
                    try {
                        r.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                es.shutdown();
                try {
                    es.awaitTermination(99999999, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

package com.after_project.webappapi;
// Copyright (c) Thiago Schnell | https://github.com/thiagoschnell/webappapi/blob/main/LICENSE
// Licensed under the MIT License.
public class Timer2 {
}
class FutureTimer{
    Thread2 t1;
    private FutureTimerInterface futureTimerInterface;
    FutureTimer(FutureTimerInterface futureTimerInterface){
        this.futureTimerInterface = futureTimerInterface;
    }
    private Integer interval = null;
    void setInterval(int interval){
        this.interval = interval;
    }
    void setNewInterval(int interval){
        stopTimer();
        this.interval = interval;
        startTimer();
    }
    void stopTimer(){
        if(t1!=null && t1.es!=null){
            t1.es.shutdownNow();
        }
    }
    void startTimer(){
        if(interval!=null) {
            t1 = new Thread2(() -> {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                futureTimerInterface.onComplete();
                interval = null;
            });
            t1.start();
        }
    }
    interface FutureTimerInterface{
        void onComplete();
    }
}
class PrimitiveTimer {
    private long startTime = 0;
    private long timeout = 0;
    private long getTickCount() {
        return System.currentTimeMillis();
    }
    protected long getTime(){
        return getTickCount() - startTime;
    }
    protected void startTimer(){
        startTime = getTickCount();
    }
    protected long getTimeout() {
        return timeout;
    }
    protected void setTimeout(long timeout){
        if(timeout > 0){
            this.timeout = timeout;
            //startTimer();
        }else if(timeout==0){
            this.timeout = 0;
        }else{
            throw new Error("INVALID TIMEOUT VALUE (" + timeout + ")");
        }
    }
    protected long getRemaingTime() {
        if(timeout>0) {
            return getTime();
        }else {
            return 0;
        }
    }
}
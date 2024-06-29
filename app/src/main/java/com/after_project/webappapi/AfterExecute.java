package com.after_project.webappapi;
// Copyright (c) Thiago Schnell | https://github.com/thiagoschnell/webappapi/blob/main/LICENSE
// Licensed under the MIT License.

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AfterExecute {

}
class LiveMethodObserver {
    final Handler mainHandle = new Handler(Looper.getMainLooper());
    final Handler uiHandle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            liveData1.setValue(msg.obj);
        }
    };
    int multitasksIndex = 0;
    List<PostExecute> list1 = new ArrayList<>();
    List<List<PostExecute>> multiTasks = new ArrayList(){{add(new ArrayList<>());}};
    Context context1;
    MutableLiveData liveData1 = null;
    LiveMethodObserver(Context context, MutableLiveData liveData ){
        this.context1 = context;
        this.liveData1 = liveData;
    }
    List<PostExecute> getTasks(){
        return multiTasks.get(multitasksIndex);
    }
    void multTtasksInc(){
        multiTasks.add(new ArrayList<>());
        multitasksIndex++;
    }
    void ThreadSleep(int millis){
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    void executeTasks(List<PostExecute> tasks, AfterExecuteImpl afterExecute){
        final int[] count = {0};
        for(PostExecute postExecute : tasks){
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            new Thread2(() -> {
                try {
                    postExecute.afterExecuteCallback = afterExecute.onAfterExecuteCallback;
                    executorService.execute(postExecute.task);
                    executorService.shutdown();
                    executorService.awaitTermination(99, TimeUnit.MINUTES);
                    afterExecute.onExecuteTask(postExecute, count[0]);
                    count[0]++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    synchronized void runTasks(Boolean sync, AfterExecuteImpl afterExecute){
        final int multiTasksCurrentIndex = new Integer(multitasksIndex);
        multTtasksInc();
        if(sync) {
            executeTasks(multiTasks.get(multiTasksCurrentIndex), afterExecute);
            afterExecute.onComplete();
        }else{
            new Thread(() -> {
                executeTasks(multiTasks.get(multiTasksCurrentIndex),afterExecute);
                afterExecute.onComplete();
            }).start();
        }
    }
    /**
     * Interfaces
     */
    interface OnAfterExecuteCallback{
        void onAfterExecute(Object object);
    }
    static class AfterExecuteAsync extends AfterExecuteImpl{
    }
    static class AfterExecuteSync extends AfterExecuteImpl{
    }
    static class AfterExecuteImpl implements AfterExecuteInterface{
        OnAfterExecuteCallback onAfterExecuteCallback;
        @Override
        public void onComplete() {
        }
        @Override
        public void onExecuteTask(PostExecute postExecute, int pos) {
        }
        void setOnAfterExecuteCallback(OnAfterExecuteCallback onAfterExecuteCallback) {
            this.onAfterExecuteCallback = onAfterExecuteCallback;
        }
    }
    private interface AfterExecuteInterface{
        void onComplete();
        void onExecuteTask(PostExecute postExecute,int pos);
    }
    //[start] PostExecute
    class PostExecute extends AbstractLiveDataObserver{
        Thread2 task = null;
        Integer id = null;
        LiveDataObject object;
        PostExecute(Context context, MutableLiveData liveData, List<PostExecute> list){
            this.liveData = liveData;
            setLiveDataObserver((LifecycleOwner) context);
            list.add(this);
            this.id = new Integer(list.lastIndexOf(this));
        }
        void setObject(LiveDataObject object) {
            this.object = object;
        }
        private void sendUI(LiveDataObject object) {
            Message msg = new Message();
            msg.obj = object;
            uiHandle.sendMessage(msg);
        }
        /**
         * uiblocking - same as runOnUiThread()
         * @param object
         */
        void runUI(LiveDataObject object){
            if(Looper.myLooper()!=null){
                mainHandle.post(()->{
                    liveData1.setValue(object);
                });
            }else {
                sendUI(object);
            }
        }
        /**
         * non-ui
         * @param object
         */
        void run(LiveDataObject object){
            if(this.afterExecuteCallback!=null) {
                this.afterExecuteCallback.onAfterExecute(object);
            }
        }
        void handle(Runnable runnable){
            mainHandle.post(runnable);
        }
        void handle(Thread thread){
            mainHandle.post(() -> {
                thread.run();
            });
        }
        void addTask(Thread2 r){
            getTasks().get(getTasks().size()-1).task = r;
        }
        @Override
        protected void setLiveDataObserver(LifecycleOwner lifecycleOwner) {
            //[start] observer
            this.observer = new Observer() {
                @Override
                public void onChanged(Object o) {
                    if(afterExecuteCallback!=null){
                        final LiveDataObject liveDataObject = (LiveDataObject)o;
                        if(liveDataObject.id == id) {
                            afterExecuteCallback.onAfterExecute(o);
                            liveData.removeObserver(observer);
                        }
                    }
                }
            };
            liveData.observe(lifecycleOwner, this.observer);
            //[end] observer
        }
    }
    //[end] PostExecute
}

abstract class AbstractLiveDataObserver{
    protected Observer observer;
    protected MutableLiveData<LiveDataObject> liveData = null;
    abstract protected void setLiveDataObserver(LifecycleOwner lifecycleOwner);
    protected LiveMethodObserver.OnAfterExecuteCallback afterExecuteCallback;
}

class LiveDataObject{
    int id;
    Object data;
    LiveDataObject(int id, Object data){
        this.id = id;
        this.data = data;
    }
}
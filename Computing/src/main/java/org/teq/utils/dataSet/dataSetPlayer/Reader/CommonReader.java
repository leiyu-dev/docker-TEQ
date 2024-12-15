package org.teq.utils.dataSet.dataSetPlayer.Reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CommonReader<T> implements Runnable, Serializable {
    protected String filePath;
    protected BufferedReader br;
    protected BlockingQueue<T> blockingQueue;
    protected int bQSize;
    protected StringLineObject<T> stringLineObject = null;

    public CommonReader(String filePath, int bQSize) {
        this.filePath = filePath;
        this.bQSize = bQSize;
        blockingQueue = new ArrayBlockingQueue<>(bQSize);
    }

    public CommonReader(String filePath, int bQSize, StringLineObject<T> stringLineObject) {
        this.filePath = filePath;
        this.bQSize = bQSize;
        blockingQueue = new LinkedBlockingQueue<>(bQSize);
        this.stringLineObject = stringLineObject;
    }

    @Override
    public void run() {
        T oneItem = null;
        try {
            while ((oneItem = getNextItem()) != null) {
                blockingQueue.put(oneItem); // 这是一个阻塞操作
            }
            closeFile();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 用来不断接受数据的入口
     * @return
     */
    public BlockingQueue<T> getBlockingQueue(){
        return this.blockingQueue;
    }
    /**
     * 一个迭代器方法，用来读取数据集中的下一行数据记录
     */
    public T getNextItem() throws Exception {
        if(br == null){
            br = new BufferedReader(new FileReader(filePath));
        }
        String line = br.readLine();
        if(line == null) return null;
        return stringLineObject.transform(line);
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setStringLineObject(StringLineObject<T> stringLineObject) {
        this.stringLineObject = stringLineObject;
    }

    public String getFilePath() {
        return filePath;
    }

    public void closeFile(){
        try {
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
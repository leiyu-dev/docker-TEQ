package org.teq.utils.dataSet.dataSetPlayer;

import org.teq.utils.dataSet.dataSetPlayer.Reader.CommonReader;

public class DataSetCommonPlayer<T>{

    public CommonDataSource<T> genPlayer(CommonReader<T> commonReader){
        return new CommonDataSource<>(commonReader);
    }
    public CommonDataSource<T> genPlayer(long bufferTimed, CommonReader<T> commonReader){
        return new CommonDataSource<>(bufferTimed, commonReader);
    }

}

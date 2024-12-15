package org.teq.utils.dataSet.dataSetPlayer.Reader;


import org.teq.utils.dataSet.commonInterface.Function;

import java.io.Serializable;
public interface StringLineObject<T> extends Serializable, Function {

    T transform(String line);
}


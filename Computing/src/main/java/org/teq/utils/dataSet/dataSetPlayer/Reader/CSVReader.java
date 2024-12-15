package org.teq.utils.dataSet.dataSetPlayer.Reader;

import java.io.Serializable;

public class CSVReader extends CommonReader<String[]> implements Serializable {

    public CSVReader(String filePath, int bQSize) {
        super(filePath, bQSize);
        this.stringLineObject = new StringLineObject<String[]>() {
            @Override
            public String[] transform(String line) {
                return line.split(",");
            }
        };
    }

}
package edLineEditor;

import java.util.ArrayList;

class previousVersionsOfText {
    private ArrayList<BufferPool> historyContents = new ArrayList<>();
    int versionNumber = 0;

    ArrayList<BufferPool> getHistoryContents(){
        return historyContents;
    }
}

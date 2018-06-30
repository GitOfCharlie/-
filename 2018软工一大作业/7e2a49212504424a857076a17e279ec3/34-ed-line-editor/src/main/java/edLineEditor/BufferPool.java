package edLineEditor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class BufferPool {
    ArrayList<String> content = new ArrayList<>();
    HashMap<String,String> hashMap = new HashMap<>();
    int lineNumber = 0;
    String fileName = null;
    boolean saved = true;

    ArrayList<String> getContent() {
        return content;
    }

    int getLineNumber() {
        return lineNumber;
    }

    String getFileName() {
        return fileName;
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
    }

    boolean isSaved(){
        return saved;
    }

    HashMap<String, String> getHashMap() {
        return hashMap;
    }

    void savePartOfContent(ArrayList<Integer> addressList, String file, boolean followTheContent, BufferPool bufferPool){//W，w保存指令
        try{
            FileWriter fileWriter = new FileWriter(file,followTheContent);
            for(int i=addressList.get(0);i<=addressList.get(addressList.size()-1);i++){
                fileWriter.write(content.get(i-1)+System.getProperty("line.separator"));//写入一行
            }
            fileWriter.close();
            bufferPool.saved = true;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}

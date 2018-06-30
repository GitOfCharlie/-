package edLineEditor;

import java.util.ArrayList;
import java.util.HashMap;

class commandDealer {
    previousVersionsOfText versionsOfText;//历史版本实例
    private BufferPool bufferPool;//缓存池实例
    //以下为s指令要保存的操作状态：
    private String str1_s;
    private String str2_s;
    private int count_s;

    //构造方法
    commandDealer(BufferPool bufferPool,previousVersionsOfText versionsOfText){
        this.bufferPool = bufferPool;
        this.versionsOfText = versionsOfText;
    }

    void inputing(String command,ArrayList<String> readThingList,ArrayList<Integer> addressList) {
        ArrayList<String> listToCompare = new ArrayList<>();
        listToCompare.addAll(bufferPool.getContent());
        switch (command.substring(command.length() - 1)) {
            case "a":
                for (int i = 0; i < readThingList.size(); i++) {
                    bufferPool.getContent().add(addressList.get(0) - 1 + 1 + i, readThingList.get(i));
                }
                bufferPool.lineNumber = addressList.get(0) + readThingList.size();
                break;
            case "i":
                if (addressList.get(0) == 0) {
                    addressList.set(0, 1);
                }
                for (int i = 0; i < readThingList.size(); i++) {
                    bufferPool.getContent().add(addressList.get(0) - 1 + i, readThingList.get(i));
                }
                bufferPool.lineNumber = addressList.get(0) + readThingList.size() - 1;
                break;
            case "c":
                if (addressList.get(0) == 0) {//0C情况
                    System.out.println("?");
                } else {
                    int beginIndex = addressList.get(0) - 1;
                    for (int i = addressList.get(0); i <= addressList.get(addressList.size() - 1); i++) {
                        bufferPool.getContent().remove(addressList.get(0) - 1);
                    }
                    for (int j = 0; j < readThingList.size(); j++) {
                        bufferPool.getContent().add(beginIndex + j, readThingList.get(j));
                    }
                    bufferPool.lineNumber = addressList.get(0) + readThingList.size() - 1;
                }
                break;
            default:
                break;
        }
        bufferPool.saved = false;

        if (!sameArrayList(listToCompare, bufferPool.getContent())) {//如果未保存
            this.saveHistoryContents();
        }
    }

    void dealing(String command){
        boolean cheXiao = false;
        ArrayList<String> listToCompare = new ArrayList<>();
        listToCompare.addAll(bufferPool.getContent());
        if(command.length()>=1) {
            ArrayList<Integer> addressList = new ArrayList<>();
            if (command.equals("u")){
                if(versionsOfText.versionNumber>0) {
                    versionsOfText.versionNumber--;
                }
                //以下复制
                bufferPool.content = versionsOfText.getHistoryContents().get(versionsOfText.versionNumber).getContent();
                bufferPool.lineNumber = versionsOfText.getHistoryContents().get(versionsOfText.versionNumber).getLineNumber();
                bufferPool.hashMap = new HashMap<>();
                for(String key:versionsOfText.getHistoryContents().get(versionsOfText.versionNumber).getHashMap().keySet()){
                    bufferPool.getHashMap().put(key,versionsOfText.getHistoryContents().get(versionsOfText.versionNumber).getHashMap().get(key));
                }
                versionsOfText.getHistoryContents().remove(versionsOfText.getHistoryContents().size() - 1);
                cheXiao = true;
            }else if (command.contains("k") && theLetterNotInAddressOrFilename(command,"k")>=0
                    && !command.contains("'k")){//k指令，标记
                int markedLine;//这是要被标记的行
                String key = command.substring(theLetterNotInAddressOrFilename(command,"k")+1);//标记的小写字母
                String address = command.substring(0, theLetterNotInAddressOrFilename(command, "k"));//目标行
                if(key.length()!=1 || key.charAt(0)<'a' || key.charAt(0)>'z'){//不合理的标记
                    System.out.println("?");
                }else {
                    if (command.startsWith("k")) {
                        markedLine = bufferPool.getLineNumber();
                        bufferPool.getHashMap().put(key, bufferPool.getContent().get(markedLine - 1));
                    } else {
                        if (this.findTargetLines(address).size() > 1 || !isValidLine(this.findTargetLines(address))) {
                            System.out.println("?");
                        } else {
                            markedLine = this.findTargetLines(address).get(0);//要标记的行
                            bufferPool.getHashMap().put(key, bufferPool.getContent().get(markedLine - 1));
                        }
                    }
                }
            }else if (command.substring(command.length() - 1).equals("p") && theLetterNotInAddressOrFilename(command,"p")>=0) {//打印函数
                if(command.equals("p")){
                    addressList.add(bufferPool.getLineNumber());
                    addressList.add(bufferPool.getLineNumber());
                }//默认情况
                else {
                    addressList = this.findTargetLines(command.substring(0, command.length() - 1));
                }
                this.printContent(addressList);
            } else if (command.substring(command.length() - 1).equals("d") && theLetterNotInAddressOrFilename(command,"d")>=0) {//删除行函数
                if(command.equals("d")){
                    addressList.add(bufferPool.getLineNumber());
                    addressList.add(bufferPool.getLineNumber());
                }
                else {
                    addressList = this.findTargetLines(command.substring(0, command.length() - 1));
                }
                this.deleteContent(addressList);
            } else if (command.substring(command.length() - 1).equals("=") && theLetterNotInAddressOrFilename(command,"=")>=0){//打印行号
                if(command.equals("=")){//默认地址是最后一行
                    addressList.add(bufferPool.getContent().size());
                }
                else {
                    addressList = this.findTargetLines(command.substring(0, command.length() - 1));
                }
                this.printLineNumber(addressList);
            } else if (command.substring(0,1).equals("f")){//设置默认文件名
                if(command.equals("f")){
                    if(bufferPool.getFileName()==null){
                        System.out.println("?");
                    }
                    else{
                        System.out.println(bufferPool.getFileName());
                    }
                }
                else{
                    bufferPool.fileName = command.substring(2);
                }
            } else if (command.contains("z") && !command.contains(" ") && theLetterNotInAddressOrFilename(command,"z")>=0){//打印指定行(z)
                if(command.substring(0,1).equals("z")){//默认行是当前行+1
                    addressList.add(bufferPool.getLineNumber()+1);
                }
                else {
                    addressList = this.findTargetLines(command.substring(0, command.indexOf("z")));
                }
                int n=-1;
                if(!command.substring(command.length()-1).equals("z")) {
                    n = Integer.parseInt(command.substring(command.indexOf("z") + 1));
                }//指定n的值
                this.printContentByZ(addressList,n);
            } else if(((command.contains("w")&&theLetterNotInAddressOrFilename(command,"w")>=0)
                    ||(command.contains("W")&&theLetterNotInAddressOrFilename(command,"W")>=0)) && !command.substring(0,1).equals("f")){//W，w保存指令
                int indexOfW;
                if(command.contains(" ")){
                    indexOfW = command.indexOf(" ")-1;
                }
                else {
                    indexOfW = command.length()-1;
                }//获取W/w的位置
                if(indexOfW==0){
                    addressList.add(1);
                    addressList.add(bufferPool.getContent().size());
                }//默认地址
                else {
                    String subStr = command.substring(0, indexOfW);//地址部分
                    addressList = this.findTargetLines(subStr);
                }

                String fileName = null;
                if(command.substring(command.length()-1).equals("w")||command.substring(command.length()-1).equals("W")){
                    if(bufferPool.getFileName()!=null) {
                        fileName = bufferPool.getFileName();
                    }//设为默认文件名
                    else{
                        System.out.println("?");//fileName仍然是null
                    }
                }//不指定文件名
                else {
                    fileName = command.substring(command.indexOf(" ") + 1);
                }

                if(fileName!=null) {
                    if (command.contains("w ")||command.substring(command.length()-1).equals("w")) {
                        this.saveToFile_w(addressList, fileName);
                    }
                    else if (command.contains("W ")||command.substring(command.length()-1).equals("W")) {
                        this.saveToFile_W(addressList, fileName);
                    }
                }
            }else if(command.substring(command.length()-1).equals("j")
                    && (!command.contains(" ")||(command.contains(" ")&&theLetterNotInAddressOrFilename(command," ")<0))){
                if(command.equals("j")){//后面一位使用默认地址
                    addressList.add(bufferPool.getLineNumber());
                    addressList.add(bufferPool.getLineNumber()+1);
                } else{
                    addressList = this.findTargetLines(command.substring(0,command.length()-1));
                }
                if(addressList.size()==1 || addressList.get(0).equals(addressList.get(1))){
                    System.out.println("？");//就指定一行，什么都不做
                }
                else {
                    this.joinLines(addressList);
                }
            } else if(command.contains("m") && !command.contains(" ") && theLetterNotInAddressOrFilename(command,"m")>=0){
                int indexOfm = theLetterNotInAddressOrFilename(command,"m");
                if(command.substring(0,1).equals("m")){//默认地址
                    addressList.add(bufferPool.getLineNumber());
                    addressList.add(bufferPool.getLineNumber());
                }else{
                    addressList = this.findTargetLines(command.substring(0,indexOfm));
                }//处理前面的地址
                int pointLine;
                if(command.substring(command.length()-1).equals("m")){
                    pointLine = bufferPool.getLineNumber();
                }else{
                    pointLine = this.findTargetLines(command.substring(indexOfm+1)).get(0);
                }
                this.move_m(addressList,pointLine);
            } else if(command.contains("t") && theLetterNotInAddressOrFilename(command,"t")>=0){
                int indexOft = theLetterNotInAddressOrFilename(command,"t");
                if(command.substring(0,1).equals("t")){//默认地址
                    addressList.add(bufferPool.getLineNumber());
                    addressList.add(bufferPool.getLineNumber());
                }else{
                    addressList = this.findTargetLines(command.substring(0,indexOft));
                }//处理前面的地址
                int pointLine;
                if(command.substring(command.length()-1).equals("t")){
                    pointLine = bufferPool.getLineNumber();
                }else{
                    pointLine = this.findTargetLines(command.substring(indexOft+1)).get(0);
                }
                this.move_t(addressList,pointLine);
            } else if(command.contains("s") && theLetterNotInAddressOrFilename(command,"s")>=0){
                int indexOfs = theLetterNotInAddressOrFilename(command,"s");
                if(command.substring(0,1).equals("s")){//默认地址
                    addressList.add(bufferPool.getLineNumber());
                    addressList.add(bufferPool.getLineNumber());
                }else{
                    addressList = this.findTargetLines(command.substring(0,indexOfs));
                }//处理前面的地址
                int count;
                if(command.endsWith("s")){//仿照上一次
                    this.replace_s(addressList,str1_s,str2_s,count_s);
                }
                else {
                    String[] commandSplit = command.substring(indexOfs + 1 + 1).split("/");//从s后面的斜杠后面切割
                    String str1, str2;
                    if(command.substring(indexOfs+1+1).contains("//")){//特殊情况，如:s/aaa//
                        str1 = commandSplit[0];
                        str2 = "";
                    }else {//一般情况，或者如:s//aaa/
                        str1 = commandSplit[0];
                        str2 = commandSplit[1];
                    }

                    if(command.endsWith("/")){
                        count = 1;//默认,count=1
                    }  else if (commandSplit[2].equals("g")) {
                        count = -1;//给一个负数，说明她要替换所有
                    } else{
                        count = Integer.parseInt(commandSplit[2]);//设定好count的值
                    }
                    this.replace_s(addressList,str1,str2,count);//执行
                }
            } else{
                System.out.println("?");//输入的命令不符合规范
            }
            //标记的改动
            for(String key:bufferPool.getHashMap().keySet()){
                if(!bufferPool.getContent().contains(bufferPool.getHashMap().get(key))){
                    bufferPool.getHashMap().remove(key);
                }
            }
            //如果未保存,增加历史版本
            if(!sameArrayList(listToCompare,bufferPool.getContent()) && !cheXiao){
                saveHistoryContents();
            }
        }
    }

    private void saveHistoryContents(){//保存历史版本
        BufferPool tempPool = new BufferPool();
        tempPool.getContent().addAll(bufferPool.getContent());//复制缓存
        tempPool.lineNumber = bufferPool.getLineNumber();//复制行号
        for (String key : this.bufferPool.getHashMap().keySet()) {//复制标记
            tempPool.getHashMap().put(key, bufferPool.getHashMap().get(key));
        }
        versionsOfText.getHistoryContents().add(tempPool);
        versionsOfText.versionNumber++;//版本号和版本List的index一致
    }

    private void printContent(ArrayList<Integer> addressList){//打印内容
        if(!this.isValidLine(addressList)){
            System.out.println("?");
        }//行数不合理输出“？”
        else if (addressList.size()!=0){
            if (addressList.size() == 2) {
                for (int i = addressList.get(0); i <= addressList.get(1); i++) {
                    System.out.println(bufferPool.getContent().get(i - 1));
                }
            }
            else if (addressList.size()==1){
                System.out.println(bufferPool.getContent().get(addressList.get(0) - 1));
            }
            bufferPool.lineNumber = addressList.get(addressList.size()-1);//当前行被设定为打印行最后一行
        }
    }

    private void printContentByZ(ArrayList<Integer> addressList,int n){//Z指令的打印方法
        if(!this.isValidLine(addressList)){
            System.out.println("?");
        }
        else {
            if (n == -1) {//未指定n值的打印
                for (int i = addressList.get(0); i <= bufferPool.getContent().size(); i++) {
                    System.out.println(bufferPool.getContent().get(i - 1));
                }
                bufferPool.lineNumber = bufferPool.getContent().size();
            }
            else if (n >= 0 && addressList.size() >= 1) {
                if ((addressList.get(0) + n) > bufferPool.getContent().size()) {//到末尾不足n行的情况
                    for (int i = addressList.get(0); i <= bufferPool.getContent().size(); i++) {
                        System.out.println(bufferPool.getContent().get(i - 1));
                    }
                    bufferPool.lineNumber = bufferPool.getContent().size();
                } else {//一般情况
                    for (int i = addressList.get(0); i <= addressList.get(0) + n; i++) {
                        System.out.println(bufferPool.getContent().get(i - 1));
                    }
                    bufferPool.lineNumber = addressList.get(0) + n;
                }
            }
        }
    }

    private void deleteContent(ArrayList<Integer> addressList){//删除行
        if(!this.isValidLine(addressList) || addressList.get(0)==0){
            System.out.println("?");
        }
        else{
            int lastLine = bufferPool.getContent().size();//保存最后一行行号
            if(addressList.size()==2){
                int deleteIndex = addressList.get(0);
                for(int i=addressList.get(0);i<=addressList.get(1);i++){
                    bufferPool.getContent().remove(deleteIndex-1);
                }
            }
            else{
                bufferPool.getContent().remove(addressList.get(0)-1);
            }
            if(addressList.get(addressList.size()-1) == lastLine){
                bufferPool.lineNumber = bufferPool.getContent().size();
            }
            else{
                bufferPool.lineNumber = addressList.get(0);
            }//设置当前行
            bufferPool.saved = false;
        }
    }

    private void printLineNumber(ArrayList<Integer> addressList){
        if(!this.isValidLine(addressList)){
            System.out.println("?");
        }//行数不合理输出“？”
        else if (addressList.size()==1){
            System.out.println(addressList.get(0));
        }
    }

    private void saveToFile_w(ArrayList<Integer> addressList, String fileName){
        if(!this.isValidLine(addressList)){
            System.out.println("?");
        }//行数不合理输出“？”
        else{
            bufferPool.savePartOfContent(addressList,fileName,false,this.bufferPool);
        }
    }

    private void saveToFile_W(ArrayList<Integer> addressList, String fileName){
        if(!this.isValidLine(addressList)){
            System.out.println("?");
        }//行数不合理输出“？”
        else{
            bufferPool.savePartOfContent(addressList,fileName,true,this.bufferPool);
        }
    }

    private void joinLines(ArrayList<Integer> addressList){
        if(!this.isValidLine(addressList)){
            System.out.println("?");
        }//行数不合理输出“？”
        else {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = addressList.get(0); i <= addressList.get(1); i++) {
                stringBuilder.append(bufferPool.getContent().get(i - 1));
            }//将几行的内容合起来
            for (int i = addressList.get(0)+1; i <= addressList.get(1); i++) {
                bufferPool.getContent().remove(addressList.get(0) +1 - 1);
            }//去掉后面几行内容
            bufferPool.getContent().set(addressList.get(0)-1, stringBuilder.toString());//将合并内容放入
            bufferPool.lineNumber = addressList.get(0);//改变当前行号
            bufferPool.saved = false;//设为没保存
        }
    }

    private void move_m(ArrayList<Integer> addressList,int pointLine){
        ArrayList<Integer> oneList = new ArrayList<>();
        oneList.add(pointLine);
        if(!this.isValidLine(addressList)||!this.isValidLine(oneList)){
            System.out.println("?");
        }
//        else if(pointLine>=addressList.get(0)&&pointLine<=addressList.get(addressList.size()-1)) {
//            System.out.println("?");
//        }
        else{
            ArrayList<String> thingsToMove = new ArrayList<>();
            for(int i=addressList.get(0);i<=addressList.get(addressList.size()-1);i++){
                thingsToMove.add(bufferPool.getContent().get(i-1));
            }//把要移动的行放进去
            for(int j=0;j<thingsToMove.size();j++){
                bufferPool.getContent().add(pointLine-1+1+j,thingsToMove.get(j));
            }//添加完毕
            int lines = addressList.get(addressList.size()-1)-addressList.get(0)+1;//一共有几行
            if(pointLine>addressList.get(addressList.size()-1)){
                for(int k=addressList.get(0);k<=addressList.get(addressList.size()-1);k++){
                    bufferPool.getContent().remove(addressList.get(0)-1);
                }
                bufferPool.lineNumber = pointLine;
            }
            else if(pointLine<addressList.get(0)){
                for(int l=addressList.get(0);l<=addressList.get(addressList.size()-1);l++){
                    bufferPool.getContent().remove(addressList.get(0)-1+lines);
                }
                bufferPool.lineNumber = pointLine+lines;
            }
            bufferPool.saved = false;
        }
    }

    private void move_t(ArrayList<Integer> addressList,int pointLine){
        ArrayList<Integer> oneList = new ArrayList<>();
        oneList.add(pointLine);
        if(!this.isValidLine(addressList)||!this.isValidLine(oneList)){
            System.out.println("?");
        }
        else{
            ArrayList<String> thingsToMove = new ArrayList<>();
            for(int i=addressList.get(0);i<=addressList.get(addressList.size()-1);i++){
                thingsToMove.add(bufferPool.getContent().get(i-1));
            }//把要复制的行放进去
            for(int j=0;j<thingsToMove.size();j++){
                bufferPool.getContent().add(pointLine-1+1+j,thingsToMove.get(j));
            }//添加完毕
            bufferPool.lineNumber = pointLine+thingsToMove.size();
            bufferPool.saved = false;
        }
    }

    private void replace_s(ArrayList<Integer> addressList,String str1,String str2,int count){
        if(!this.isValidLine(addressList)){
            System.out.println("?");
        }//行数不合理输出“？”
        else{
            boolean changed = false;//你到底改没改
            if(count<0){//g指令，全部替换
                for(int i=addressList.get(0);i<=addressList.get(addressList.size()-1);i++){
                    String oneLine = bufferPool.getContent().get(i-1);//一行内容
                    if(oneLine.contains(str1)){
                        bufferPool.lineNumber = i;
                        oneLine = oneLine.replaceAll(str1,str2);//替换
                        bufferPool.getContent().set(i-1,oneLine);//放回
                        changed = true;//改变了
                    }//修改当前行
                }
            }
            else if(count>0) {//替换第count个
                int num;//计数器
                for (int i = addressList.get(0); i <= addressList.get(addressList.size() - 1); i++) {
                    num = 0;
                    String oneLine = bufferPool.getContent().get(i - 1);//一行内容
                    for (int j = 0; j <= oneLine.length() - str1.length(); j++) {
                        if (oneLine.substring(j, j + str1.length()).equals(str1)) {
                            num++;
                        }
                        if (num == count) {//替换
                            bufferPool.getContent().set(i - 1, oneLine.substring(0, j) + str2 + oneLine.substring(j + str1.length()));
                            bufferPool.lineNumber = i;//改变行号
                            changed = true;//改变了
                            break;
                        }
                    }
                }
            }
            if(changed){
                bufferPool.saved = false;//改了就没保存
            }else{
                System.out.println("?");//没改则“？”
            }
            str1_s = str1;
            str2_s = str2;
            count_s = count;//保存数据
        }
    }

    ArrayList<Integer> findTargetLines(String command) {
        ArrayList<Integer> target = new ArrayList<>();//新建动态数组用来存储返回的数值
//        int validDouHao = theLetterNotInAddressOrFilename(command,",");
        if (!hasDouHao(command)||command.equals(",")||theLetterNotInAddressOrFilename(command,",")<0) {
            target = this.dealWithSingleAddress(command);
        }//单一指令
        else {
            int indexOfDouhao = theLetterNotInAddressOrFilename(command,",");
            target.add(this.dealWithSingleAddress(command.substring(0,indexOfDouhao)).get(0));
            target.add(this.dealWithSingleAddress(command.substring(indexOfDouhao+1)).get(0));
//            String[] list = command.split(",");
//            for (String subCommand : list) {
//                target.add(this.dealWithSingleAddress(subCommand).get(0));
//            }
        }//含有逗号的双重指令
        return target;
    }

    private ArrayList<Integer> dealWithSingleAddress(String command){
        ArrayList<Integer> target = new ArrayList<>();
        boolean singleCommand = true;//是否是复杂指令的标志
        int breakPoint = 0;//'-'和'+'的位置
        if(command.contains("-") && theLetterNotInAddressOrFilename(command,"-")>=0){
            breakPoint = theLetterNotInAddressOrFilename(command,"-");
        } else if (command.contains("+") && theLetterNotInAddressOrFilename(command,"+")>=0){
            breakPoint = theLetterNotInAddressOrFilename(command,"+");
        }
        if((command.contains("-") && breakPoint!=0)||(command.contains("+") && breakPoint!=0)){
            singleCommand = false;
        }//是复杂指令
        if(singleCommand) {//单一指令
            if (command.equals(".")) {//当前行行号
                target.add(bufferPool.getLineNumber());
            } else if (command.equals("$")) {//最后一行行号
                target.add(bufferPool.getContent().size());
            } else if (command.equals(",")) {
                target.add(1);
                target.add(bufferPool.getContent().size());
            } else if (command.equals(";")) {//当前行到最后一行
                target.add(bufferPool.getLineNumber());
                target.add(bufferPool.getContent().size());
            } else if (command.charAt(0) == '-') {//前n行
                int n = Integer.parseInt(command.substring(1));
                target.add(bufferPool.getLineNumber() - n);
            } else if (command.charAt(0) == '+') {//后n行
                int n = Integer.parseInt(command.substring(1));
                target.add(bufferPool.getLineNumber() + n);
            } else if (command.charAt(0) == '/' && command.charAt(command.length()-1)=='/') {//下一个匹配str的行
                String str = command.substring(1, command.length() - 1);
                boolean findIt = false;
                for (int i = bufferPool.getLineNumber() + 1; i <= bufferPool.getContent().size(); i++) {
                    if (this.containThisString(bufferPool.getContent().get(i - 1), str)) {
                        findIt = true;
                        target.add(i);
                        break;
                    }
                }//在当前行往后找
                if (!findIt) {
                    for (int i = 1; i <= bufferPool.getLineNumber(); i++) {
                        if (this.containThisString(bufferPool.getContent().get(i - 1), str)) {
                            target.add(i);
                            findIt = true;
                            break;
                        }
                    }
                }//找不到，从头开始找
                if(!findIt){
                    target.add(-1);//给他个负数让ta报错输出“?”
                }
            } else if (command.charAt(0) == '?' && command.charAt(command.length()-1)=='?') {//上一个匹配str的行
                String str = command.substring(1, command.length() - 1);
                boolean findIt = false;
                for (int i = bufferPool.getLineNumber() - 1; i >= 1; i--) {
                    if (this.containThisString(bufferPool.getContent().get(i - 1), str)) {
                        findIt = true;
                        target.add(i);
                        break;
                    }
                }//在当前行往前找
                if (!findIt) {
                    for (int i = bufferPool.getContent().size(); i >= bufferPool.getLineNumber(); i--) {
                        if (this.containThisString(bufferPool.getContent().get(i - 1), str)) {
                            findIt = true;
                            target.add(i);
                            break;
                        }
                    }
                }//找不到，从末尾开始找
                if(!findIt){
                    target.add(-1);//给他个负数让她报错输出“?”
                }
            } else if(isANumber(command)) {//第n行
                target.add(Integer.parseInt(command));
            } else if (command.substring(0,1).equals("'") && !command.equals("'")){
                String theKey = command.substring(1,2);
                if(bufferPool.getHashMap().containsKey(theKey)){
                    target.add(bufferPool.getContent().indexOf(bufferPool.getHashMap().get(theKey))+1);
                }else{
                    target.add(-1);
                }
            }
            else{
                target.add(-1);//给个负数让ta报错
            }
        }
        else{//复杂地址
            int finalLine;//最终得到的行数
            if(!isANumber(command.substring(breakPoint + 1))) {
                finalLine = -1;
            }else {
                int n = Integer.parseInt(command.substring(breakPoint + 1));//????
                String frontStr = command.substring(0, breakPoint);//+-前面的命令
                if (frontStr.equals(".")) {
                    finalLine = bufferPool.getLineNumber();
                } else if (frontStr.equals("$")) {
                    finalLine = bufferPool.getContent().size();
                } else if (isANumber(frontStr)) {
                    finalLine = Integer.parseInt(frontStr);
                } else if (frontStr.substring(0, 1).equals("/")) {
                    String string = frontStr.substring(1, frontStr.length() - 1);
                    finalLine = this.findMatchLine_down(string);
                } else if (frontStr.substring(0, 1).equals("?")) {
                    String string = frontStr.substring(1, frontStr.length() - 1);
                    finalLine = this.findMatchLine_Up(string);
                } else if (frontStr.substring(0, 1).equals("'")) {
                    String theKey = frontStr.substring(1, 2);
                    if (bufferPool.getHashMap().containsKey(theKey)) {
                        finalLine = bufferPool.getContent().indexOf(bufferPool.getHashMap().get(theKey)) + 1;
                    } else {
                        finalLine = -1;
                    }
                } else {
                    finalLine = -1;//给一个负，表示是错误的
                }
                //处理前面的指令
                if (finalLine >= 0) {
                    if (command.contains("-")) {
                        finalLine = finalLine - n;
                    } else {
                        finalLine = finalLine + n;
                    }//处理后面的指令
                }
            }
            target.add(finalLine);
        }
        return target;
    }

    private boolean containThisString(String oneline,String str){//判断一行是否包涵目标str
        boolean contains = false;
        int strLength = str.length();
        for(int i=0;i<=oneline.length()-strLength;i++){
            if(oneline.substring(i,i+strLength).equals(str)){
                contains = true;
                break;
            }
        }
        return contains;
    }

    boolean isValidLine(ArrayList<Integer> addressList){
        boolean isValid = true;
        for(int oneaddress:addressList){
            if((oneaddress<1&&bufferPool.getContent().size()!=0)
                    ||(oneaddress<0&&bufferPool.getContent().size()==0)
                    ||oneaddress>bufferPool.getContent().size()){
                isValid = false;
                break;
            }
        }
        if (addressList.size()==2 && addressList.get(0)>addressList.get(1)){
            isValid = false;//检验是否有不合理的行数
        }
        return isValid;
    }

    private static boolean hasDouHao(String string){
        boolean hasDouHao = false;
        for (int i = 0; i < string.length(); i++) {
            if (string.substring(i, i + 1).equals(",")) {
                hasDouHao = true;
                break;
            }
        }//判断是否有逗号，即是否有两个地址还是一个
        return hasDouHao;
    }

    private static boolean isANumber(String string){
        boolean isNumber = true;
        for(int i=0;i<string.length();i++){
            if(string.charAt(i)<'0' || string.charAt(i)>'9'){
                isNumber = false;
            }
        }
        return isNumber;
    }

    private int findMatchLine_down(String str){//寻找匹配str行，“/”
        int returnLine = -1;
        boolean findIt = false;
        for (int i = bufferPool.getLineNumber() + 1; i <= bufferPool.getContent().size(); i++) {
            if (this.containThisString(bufferPool.getContent().get(i - 1), str)) {
                findIt = true;
                returnLine = i;
                break;
            }
        }//在当前行往后找
        if (!findIt) {
            for (int i = 1; i <= bufferPool.getLineNumber(); i++) {
                if (this.containThisString(bufferPool.getContent().get(i - 1), str)) {
                    returnLine = i;
                    break;
                }
            }
        }//找不到，从头开始找
        return returnLine;
    }

    private int findMatchLine_Up(String str){//寻找匹配str行，"?"
        int returnLine = -1;
        boolean findIt = false;
        for (int i = bufferPool.getLineNumber() - 1; i >= 1; i--) {
            if (this.containThisString(bufferPool.getContent().get(i - 1), str)) {
                findIt = true;
                returnLine = i;
                break;
            }
        }//在当前行往前找
        if (!findIt) {
            for (int i = bufferPool.getContent().size(); i >= bufferPool.getLineNumber(); i--) {
                if (this.containThisString(bufferPool.getContent().get(i - 1), str)) {
                    returnLine = i;
                    break;
                }
            }
        }//找不到，从末尾开始找
        return returnLine;
    }

    static int theLetterNotInAddressOrFilename(String command,String letter) {
        ArrayList<Integer> sameLetterList = new ArrayList<>();//把command里所有letter的索引放进去
        for (int i = 0; i < command.length(); i++) {
            if (command.substring(i, i + 1).equals(letter)) {
                sameLetterList.add(i);
            }
        }
        int isValid = -1;
        for (int index : sameLetterList) {
            if (!(command.contains(" ") && index > command.indexOf(" "))||letter.equals("-")||letter.equals("+")) { //第一个条件：不在filename里
                boolean hasBefore = false, hasAfter = false;
                for (int i = 0; i < index; i++) {//前面有没有/，？
                    if (command.substring(i, i + 1).equals("/") || command.substring(i, i + 1).equals("?")) {
                        hasBefore = true;
                        break;
                    }
                }
                for (int i = index; i < command.length(); i++) {//后面有没有勒？
                    if (command.substring(i, i + 1).equals("/") || command.substring(i, i + 1).equals("?")) {
                        hasAfter = true;
                        break;
                    }
                }
                if (!(hasAfter && hasBefore)) {//第二个条件不在str里
                    isValid = index;
                    break;
                }
            }
        }
        if(letter.equals("s") && !command.endsWith("s") && isValid<0){//特殊情况，命令为s时可能有错误
            for(int index:sameLetterList){
                int leftNumber = 0,rightNumber = 0;
                for(int i=0;i<index;i++){
                    if(command.substring(i,i+1).equals("/")||command.substring(i,i+1).equals("?")){
                        leftNumber++;
                    }
                }
                for(int i=index;i<command.length();i++){
                    if(command.substring(i,i+1).equals("/")||command.substring(i,i+1).equals("?")){
                        rightNumber++;
                    }
                }
                if(leftNumber%2==0&&rightNumber==3){
                    isValid = index;
                    break;
                }
            }
        }
        if((letter.equals("m")&&!command.endsWith("m")&&isValid<0)
                ||(letter.equals("t")&&!command.endsWith("t")&&isValid<0)
                ||(letter.equals(","))){//m,t和逗号也有错误
            for(int index:sameLetterList) {
                int leftNumber = 0, rightNumber = 0;
                for (int i = 0; i < index; i++) {
                    if (command.substring(i, i + 1).equals("/") || command.substring(i, i + 1).equals("?")) {
                        leftNumber++;
                    }
                }
                for (int i = index; i < command.length(); i++) {
                    if (command.substring(i, i + 1).equals("/") || command.substring(i, i + 1).equals("?")) {
                        rightNumber++;
                    }
                }
                if(leftNumber%2==0 && rightNumber%2==0){
                    isValid = index;
                    break;
                }
            }
        }
        return isValid;
    }

    private static boolean sameArrayList(ArrayList arrayListA,ArrayList arrayListB){
        boolean twoSameLists = true;
        if(arrayListA.size() != arrayListB.size()){
            twoSameLists = false;
        }else{
            for(int i=0;i<arrayListA.size();i++){
                if(!arrayListA.get(i).equals(arrayListB.get(i))){
                    twoSameLists = false;
                    break;
                }
            }
        }
        return twoSameLists;
    }
}

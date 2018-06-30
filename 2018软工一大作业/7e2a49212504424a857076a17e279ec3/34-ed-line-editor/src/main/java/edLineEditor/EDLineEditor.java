package edLineEditor;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class EDLineEditor {
	/**
	 * 接收用户控制台的输入，解析命令，根据命令参数做出相应处理。
	 * 不需要任何提示输入，不要输出任何额外的内容。
	 * 输出换行时，使用System.out.println()。或者换行符使用System.getProperty("line.separator")。
	 *
	 * 待测方法为public static void main(String[] args)方法。args不传递参数，所有输入通过命令行进行。
	 * 方便手动运行。
	 *
	 * 说明：可以添加其他类和方法，但不要删除该文件，改动该方法名和参数，不要改动该文件包名和类名
	 */
	public static void main(String[] args) {
		BufferPool bufferPool = new BufferPool();//new一个缓存池
		previousVersionsOfText versionsOfText = new previousVersionsOfText();//new版本储存器
		commandDealer dealer = new commandDealer(bufferPool,versionsOfText);//new一个命令处理器
		Scanner scanner = new Scanner(System.in);
		String command = scanner.nextLine();
		while(!command.substring(0,2).equals("ed")){
			command = scanner.nextLine();
		}
		setContent(bufferPool,command);//从“ed”行指令开始,初始化文件内容
		BufferPool tempPool = new BufferPool();
		tempPool.content.addAll(bufferPool.getContent());
		tempPool.lineNumber = bufferPool.getLineNumber();
		dealer.versionsOfText.getHistoryContents().add(tempPool);//添加初始版本
		//以下处理ed后的指令
		command = scanner.nextLine();
		while(true) {
			boolean toTheEnd = false;
			if (command.equals("Q")) {
				break;
			}//直接退出
			else if (command.equals("q")) {//判断是否再次输入“q”，判断是否确认退出
				if(!bufferPool.isSaved()){
					System.out.println("?");
					command = scanner.nextLine();
					if (command.equals("q")){//如果再输入q则退出，否则继续
						break;
					}
				}
				else{
					break;
				}
			}//"q"情况
			else {
				String lastLetter = command.substring(command.length()-1);
				int isNotAddress = commandDealer.theLetterNotInAddressOrFilename(command,lastLetter);
				if((lastLetter.equals("a")||lastLetter.equals("i")||lastLetter.equals("c"))
						&&!command.contains(" ")&&isNotAddress>=0
						&&(!command.endsWith("ka")&&!command.endsWith("ki")&&!command.endsWith("kc"))) {
					//输入模式见下
					ArrayList<Integer> addressList = new ArrayList<>();
					if(command.equals("a")||command.equals("i")||command.equals("c")){//默认地址，即指令只是"a","i","c"
						switch (command){
							case "a":addressList.add(bufferPool.getLineNumber());break;
							case "i":addressList.add(bufferPool.getLineNumber());break;
							case "c":
								addressList.add(bufferPool.getLineNumber());
								addressList.add(bufferPool.getLineNumber());
								break;
						}
					} else {
						addressList = dealer.findTargetLines(command.substring(0, command.length() - 1));
					}//获取地址

					if(!dealer.isValidLine(addressList)){
						System.out.println("?");
					}//行数不合理输出“？”
					else if(command.equals("c") && bufferPool.getContent().size()==0){
						System.out.println("?");//0c情况
					}else {
						ArrayList<String> readThingList = new ArrayList<>();
						String readThing = scanner.nextLine();
						while (!readThing.equals(".")) {
							readThingList.add(readThing);
							if (scanner.hasNextLine()) {
								readThing = scanner.nextLine();
							} else {
								System.out.println("?");
								toTheEnd = true;
								break;
							}
						}//获取一行一行的内容
						dealer.inputing(command, readThingList,addressList);
					}
				}
				else {
					dealer.dealing(command);
				}

				if(!toTheEnd) {
					command = scanner.nextLine();
				}else{
					break;
				}
			}// 非退出情况
		}
		scanner.close();//关闭scanner
	}

	private static void setContent(BufferPool bufferPool,String command){
		if( !command.equals("ed")){
			String name = command.substring(3);//获取文件名
			bufferPool.setFileName(name);//设置文件名
			try{
				BufferedReader bufferedReader = new BufferedReader(new FileReader(name));
				String oneLine = bufferedReader.readLine();
				while(oneLine!=null){
					bufferPool.content.add(oneLine);
					oneLine = bufferedReader.readLine();
				}//一行一行添加文件内容
				bufferPool.lineNumber = bufferPool.getContent().size();
				bufferedReader.close();//关闭
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		//若指令为“ed”时不执行操作
		bufferPool.lineNumber = bufferPool.getContent().size();
	}
}

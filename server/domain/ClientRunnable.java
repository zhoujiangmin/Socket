package socket.server.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;


public class ClientRunnable implements Runnable{
	
	private ClientBean myself;
	private boolean run_flag;
	private Socket connect;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	
	public ClientRunnable(Socket socket, ClientBean myself) {
		run_flag = true;
		this.connect = socket;
		this.myself = myself;
	}
	
	public void close() {
		run_flag = false;
	}
	
	private void destroy() {
		
		//设置为offline
		myself.setConnect("offline");
		myself.setMemory("unknown");
		myself.setThreads("unknown");
		
		try {
			printWriter.close();
			bufferedReader.close();
			connect.close();
		} catch (IOException e) {
			System.out.println("Error when destroy connect <" + myself.getIp() + "> !!!");
		}
	}
	
	public void parseClientResponse(String response) {
		
		String[] standard = {"memory:", "0%", "threads:", "0"};
		String[] info = {"", "", "", ""};
		int i = 0;
		
		StringTokenizer st = new StringTokenizer(response, " "); 
		
        while(st.hasMoreElements()) {  
        	info[i++] = st.nextElement().toString();
        	if (i >= 4)    break;  //只接受4个字段
        } 
        
        if (i != 4 || !(info[0].equals(standard[0]))  || !(info[2].equals(standard[2]))) return;
        	
		myself.setMemory(info[1]);
		myself.setThreads(info[3]);
		myself.setConnect("online");
	}
	
	public void run() {
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			printWriter = new PrintWriter(connect.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("Error in Tread +<" + myself.getIp() + "> !");
			System.out.println("Cannot create bufferedReader and printWriter!");
			run_flag = false;
		}	
		
		while (run_flag)
		{
			String line;
			try {
				
				line = bufferedReader.readLine();
				
				if (line != null)
				{
					System.out.println(myself.getIp() + " say: " + line);
					myself.setConnect("online");
					parseClientResponse(line);
				}
				else
				{
					System.out.println("lose connect to client <" + myself.getIp() + "> !!!");
					break;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("lose connect to client <" + myself.getIp() + "> !!!");
				break;
			}
		}
		
		destroy();
	}
}

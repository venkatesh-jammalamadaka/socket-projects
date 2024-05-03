
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPServer {
	private static final int sPort = 5106; // The server will be listening on this port number

	public static void main(String[] args) throws Exception {
		System.out.println("The server is running.");
		ServerSocket listener = new ServerSocket(sPort);
		int clientNum = 1;
		try {
			while (true) {
				new Handler(listener.accept(), clientNum).start();
				System.out.println("Client " + clientNum + " is connected!");
				clientNum++;
			}
		} finally {
			listener.close();
		}

	}

	/**
	 * A handler thread class. Handlers are spawned from the listening loop and are
	 * responsible for dealing with a single client's requests.
	 */
	private static class Handler extends Thread {

		private Socket connection;
		private ObjectInputStream in; // stream read from the socket
		private ObjectOutputStream out; // stream write to the socket
		private int no; // The index number of the client

		public Handler(Socket connection, int no) {
			this.connection = connection;
			this.no = no;
		}
        public void run() {
 		try{
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			try{
				while(true)
				{
					
					Object obj = in.readObject();
					String command = (String) obj;
					if (command.contains("get")) {
						sendFile(command.split("_")[1]);
					} else if (command.contains("upload")) {
						getFile(command.split("_")[1]);
					}
				}
			}
			catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + no);
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + no);
			}
		}
	}
//		public void run() {
//			System.out.println("Waiting for connection...");
//			try {
//				System.out.println(
//						"Connection received from client-" + this.no + " " + connection.getInetAddress().getHostName());
//
//				// Initialize input and output streams
//				out = new ObjectOutputStream(connection.getOutputStream());
//				in = new ObjectInputStream(connection.getInputStream());
//				Object obj = in.readObject();
//				String command = (String) obj;
//				if (command.contains("get")) {
//					sendFile(command.split("_")[1]);
//				} else if (command.contains("upload")) {
//					getFile(command.split("_")[1]);
//				}
////	                connection.close();
//
//			} catch (IOException | ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//		}

		private void sendFile(String filename) {
			try {
				String filePath = System.getProperty("user.dir") + "\\" + filename;
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
				byte[] buffer = new byte[1000];
				int bytesRead;
				out.writeObject("FileFound");
				out.flush();
				while ((bytesRead = bis.read(buffer)) != -1) {

					out.write(buffer, 0, bytesRead);
					out.flush();
				}

				System.out.println("File sent successfully to client-" + this.no + " File- " + filename);
				bis.close();
			} catch (FileNotFoundException e) {
				System.err.println("File not found for request of client- " + this.no + filename);
				try {
					out.writeObject("FileNotFound");
					out.flush();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			} catch (IOException e) {
				System.err.println("Error reading file or sending data: " + e.getMessage());
			}
		}

		void getFile(String filename) {
			try {
				// stream write the message

				System.out.println("name of file about to be received from client "+this.no+" : " + filename);
				// ------------------------

				// Create a new file to save the received chunks
				File outputFile = new File(System.getProperty("user.dir"), "new_" + filename);
				FileOutputStream fos = new FileOutputStream(outputFile);

				byte[] buffer = new byte[1000];
				int bytesRead;

				while ((bytesRead = in.read(buffer)) != -1) {
					// Write the received chunk to the file

					fos.write(buffer, 0, bytesRead);
					if (bytesRead < 1000) {
		                break;
		            }
				}

				System.out.println("File received successfully from client-" + this.no +" "+ outputFile.getAbsolutePath());
				fos.close();
			}

			catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

	}
}

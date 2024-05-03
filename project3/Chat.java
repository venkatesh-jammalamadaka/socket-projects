

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Chat {
	Socket connection = null;
	ServerSocket listener;
	String username;
	boolean connectionRequestSent = false;
	
	public static void main(String[] args) throws Exception {
		Chat chat = new Chat();
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter name:");
		chat.username = scanner.nextLine();
		
		int sPort = 0;
		chat.listener = new ServerSocket(sPort);
		System.out.println(chat.username + " is running...");
		System.out.println("The server is running on port: " + chat.listener.getLocalPort());
		new Handler(chat).start();
		chat.run();
		
	}

	public void run() {
		
		ObjectOutputStream out;
		ObjectInputStream in;
		String message;
		
		try {
			connection = listener.accept();
			
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			String user2 = (String)in.readObject();
			System.out.println("New Client is connected! client name: "+ user2);
			if(!this.connectionRequestSent) {
				System.out.println("Enter port number of server to which messages are to be sent:");
			}
			while (true) {
				// receive the message sent from the client
				try {
					message = (String) in.readObject();
					System.out.println("Received message: " + message);
					
					if(message.contains("transfer")) {
						String[] filename= message.split(" ");
						getFile(filename[1], in);
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	
	void getFile(String filename, ObjectInputStream in) {
		try {
			// stream write the message

			System.out.println("name of file about to be received from client. File- " + filename);
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

			System.out.println("File received successfully from client. File- " + outputFile.getAbsolutePath());
			fos.close();
		}

		catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	private static class Handler extends Thread {

		private Socket connection;
		private ObjectInputStream in; // stream read from the socket
		private ObjectOutputStream out; // stream write to the socket
		String message;
		private Chat chat;
		boolean connectionRequestSent = false;
		
		public Handler(Chat chat) {
			this.chat = chat;
		}
		
		public void run() {
			try {
				Scanner scanner = new Scanner(System.in);
				while(!connectionRequestSent) {
					System.out.println("Enter port number of server to which messages are to be sent:");
					String input = scanner.nextLine();
					connection = new Socket("localhost", Integer.parseInt(input));
					connectionRequestSent=true;
					this.chat.connectionRequestSent=true;
				}
				
				// initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.writeObject(chat.username);
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

				while (true) {

					// read a sentence from the standard input
					message = bufferedReader.readLine();
					// Send the sentence to the server
					sendMessage(message);
					if(message.contains("transfer")) {
						String[] filename= message.split(" ");
						sendFile(filename[1]);
					}
				}

			} catch (IOException ioException) {
				System.out.println("Disconnect with Client ");
			} finally {
				// Close connections
				try {
					in.close();
					out.close();
					connection.close();
				} catch (IOException ioException) {
					System.out.println("Disconnect with Client ");
				}
			}
		}

		private void sendFile(String filename) {
			try {
				String filePath = System.getProperty("user.dir") + "\\" + filename;
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
				byte[] buffer = new byte[1000];
				int bytesRead;
//				out.writeObject("FileFound");
//				out.flush();
				while ((bytesRead = bis.read(buffer)) != -1) {

					out.write(buffer, 0, bytesRead);
					out.flush();
				}

				System.out.println("File sent successfully to client. File- " + filename);
				bis.close();
			} catch (FileNotFoundException e) {
				System.err.println("File not found for request of client. File- " + filename);
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

				System.out.println("name of file about to be received from client. File- " + filename);
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

				System.out.println("File received successfully from client. File- " + outputFile.getAbsolutePath());
				fos.close();
			}

			catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

		// send a message to the output stream
		void sendMessage(String msg) {
			try {
				// stream write the message
				out.writeObject(msg);
				out.flush();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

}

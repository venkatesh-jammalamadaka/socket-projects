
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
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class FTPClient {
	
		Socket requestSocket;           //socket connect to the server
		ObjectOutputStream out;         //stream write to the socket
	 	ObjectInputStream in;          //stream read from the socket
		String message;                //message send to the server
		String MESSAGE;                //capitalized message read from the server

		void run(int serverPort) {
			Scanner scanner = new Scanner(System.in);
			System.out.println("In run of client");
			System.out.println("CWD: "+System.getProperty("user.dir"));
			// create a socket to connect to the server
			try {
				requestSocket = new Socket("localhost", serverPort);
				// initialize inputStream and outputStream
				out = new ObjectOutputStream(requestSocket.getOutputStream());

				in = new ObjectInputStream(requestSocket.getInputStream());
			} 
			catch (ConnectException e) {
				System.err.println("Connection refused. You need to initiate a server first.");
			}

			catch (UnknownHostException unknownHost) {
				System.err.println("You are trying to connect to an unknown host!");
			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			System.out.println("Connected to localhost server in port " + serverPort);
			while(true) {
				try {
					System.out.println("Enter get or upload: Format: <get/upload filename.extension>");
					String input = scanner.nextLine();

			        // Split the input string into two parts
			        String[] parts = input.split(" ", 2);
					// get Input from standard input
					
					String command = parts[0];
					String filename = parts[1];
					
					// Send the sentence to the server
					if (command.contains("get")) {
						getFile(filename);
					} else if (command.contains("upload")) {
						sendFile(filename);
					}

				}  finally {
					// Close connections
//					try {
//						in.close();
//						out.close();
//						requestSocket.close();
//					} catch (IOException ioException) {
//						ioException.printStackTrace();
//					}
				}
			}
		}

		// send a message to the output stream
		void getFile(String filename) {
			try {
				// stream write the message
				String command_filename = "get_"+filename;
				out.writeObject(command_filename);
				out.flush();
				System.out.println("Filename requested for download: " + filename);
				// ------------------------
				Object response = in.readObject();
				String resp = (String) response;
				System.out.println("initial response = "+ resp);
				if (resp.equalsIgnoreCase("FileNotFound")) {
					System.out.println("Please enter valid filename present in the current dir with extension");
					return;
				}

//				// receiving the object from server
//				File directory = new File("C:\\Users\\venka\\eclipse-workspace\\CN\\src\\project1");

				// Create a new file to save the received chunks
				File outputFile = new File(System.getProperty("user.dir"), "new_" + filename);
				FileOutputStream fos = new FileOutputStream(outputFile);

				byte[] buffer = new byte[1000];
				int bytesRead;

				while ((bytesRead = in.read(buffer)) != -1) {
					// Write the received chunk to the file
					//System.out.println("bytes read: "+ bytesRead);
					fos.write(buffer, 0, bytesRead);
					if (bytesRead < 1000) {
		                break;
		            }
				}

				System.out.println("File received successfully from server: " + outputFile.getAbsolutePath());
				fos.close();
			}

			catch (IOException | ClassNotFoundException ioException) {
				ioException.printStackTrace();
			}
		}

		private void sendFile(String filename) {
			String command_filename = "upload_"+filename;
			try {
				out.writeObject(command_filename);
				String filePath = System.getProperty("user.dir")+ "\\" + filename;
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
				byte[] buffer = new byte[1000];
				int bytesRead;

				while ((bytesRead = bis.read(buffer)) != -1) {
					
					out.write(buffer, 0, bytesRead);
					out.flush();
				}

				System.out.println("File sent successfully to server: " + filename);
				bis.close();
			} catch (FileNotFoundException e) {
				System.out.println("Please enter valid filename present in the current dir with extension");

			} catch (IOException e) {
				System.err.println("Error reading file or sending data: " + e.getMessage());
			}
		}

		// main method
		public static void main(String args[]) {
			
			if (args.length != 1) {
				System.out.println("Usage: java FTPClient <server_port>");
				return;
			}

			int serverPort = Integer.parseInt(args[0]);
			
			FTPClient client = new FTPClient();
			client.run(serverPort);
		}
}

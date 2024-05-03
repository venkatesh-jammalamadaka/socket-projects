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
	
    ServerSocket serverSocket; // ServerSocket used to listen on port number 5106
    Socket connection = null; // Socket for the connection with the client
    ObjectOutputStream out; // Stream write to the socket
    ObjectInputStream in; // Stream read from the socket
	
	public static void main(String args[]) {
		FTPServer s = new FTPServer();
		s.run(5106);

	}
	
	void run(int serverPort) {
		System.out.println("Waiting for connection...");
		try {
	        serverSocket = new ServerSocket(serverPort);

	        while(true) {
	        	// Accept a connection from the client
		        connection = serverSocket.accept();
		        System.out.println("Connection received from " + connection.getInetAddress().getHostName());

		        // Initialize input and output streams
		        out = new ObjectOutputStream(connection.getOutputStream());
		        in = new ObjectInputStream(connection.getInputStream());
		        Object obj = in.readObject();
		        String command = (String) obj;
		        if (command.contains("get")) {
		        	sendFile(command.split("_")[1]);
		        }
		        else if (command.contains("upload")){
		        	getFile(command.split("_")[1]);
		        }
                connection.close();
	        }
		}
		catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
	}
	
	   private void sendFile(String filename) {
	        try {
	            String filePath = System.getProperty("user.dir")+ "\\" + filename;
	            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
	            byte[] buffer = new byte[1000];
	            int bytesRead;
	            out.writeObject("FileFound");
                out.flush();
	            while ((bytesRead = bis.read(buffer)) != -1) {
	            	
	                out.write(buffer, 0, bytesRead);
	                out.flush();
	            }

	            System.out.println("File sent successfully to client: " + filename);
	            bis.close();
	        } catch (FileNotFoundException e) {
	            System.err.println("File not found: " + filename);
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
				
				System.out.println("filename about to be received: " + filename);
				// ------------------------


				// Create a new file to save the received chunks
				File outputFile = new File(System.getProperty("user.dir"), "new_" + filename);
				FileOutputStream fos = new FileOutputStream(outputFile);

				byte[] buffer = new byte[1000];
				int bytesRead;

				while ((bytesRead = in.read(buffer)) != -1) {
					// Write the received chunk to the file
					
					fos.write(buffer, 0, bytesRead);

				}

				System.out.println("File received successfully from client: " + outputFile.getAbsolutePath());
				fos.close();
			}

			catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
}

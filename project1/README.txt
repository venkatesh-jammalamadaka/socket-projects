1.  The FTPServer program needs to be run first. It listens on port 5106.
    The command to run FTPServer is :
    java FTPServer
	
	The FTPServer keeps on listening, so no need to run it for each get/upload command from client.
	
########### For Downloading a file #############################

2.  Now the FTPClient needs to be run. 
	For downloading a file enter the follwing command in command promt:
	java FTPClient 5106 get downloadTestFile.pptx

3.  Now the above command will download the file from server and store a file with name
    new_downloadTestFile.pptx in the same directory.

########### For Uploading a file #############################

4.  Now again run the FTPClient to upload the file.
	For uploading a file enter the follwing command in command promt:
	java FTPClient 5106 upload uploadTestFile.pptx

5.  Now the above command will upload the file from client to server. The server will store a file with name
    new_uploadTestFile.pptx in the same directory.
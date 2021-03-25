Build instructions:

1. Open the project in Android studios
2. Click the current emulator device, and choose "AVD Manager"
3. Click 'Create Virtual Device' and choose 'Pixel 4 XL'
4. Click next then click 'Oreo' for the system image and ensure you select the 8.1 version
5. Now click finish and wait until the emulator is downloaded
6. Ensure that you will be running 'app' in the configurations
7. Now press the green triangle button in the top left to begin running the application (The project may take a few minutes please wait until it loads)
8. Give the application location permissions
9. Click continue anyways when prompted to change settings
10. Click launch (You may recieve a runtime error on this first try)
10b. If you receive a runtime error, please repeat steps 7 through 10. This is a bug, and it fixes itself upon 
	 recompiling once or twice.

Errors that may occur when compiling:
	1. Something about SDK location: go to local.properties and enter the correct path to your mapcovid (remove the quotations)
	    -C\:\\Users\\"YOUR USERNAME"\\AppData\\Local\\Android\\Sdk <-Windows
	    - /"YOUR USEERNAME"/nicksaunders/Library/Android/sdk   <- Mac
	2. Anything else: Delete the orange '.gradle' folder and the orange 'build' folder
	3. If those don't work contact team 16 please

Errors when running:
	1. Try recompiling it will work eventually
	2. If the 'run' console is outputting something like "grpc - failed" and/or the emulator is 
	   not responding go to AVD manager again like in step 2, click on the white upside down triangle
	   on the row of the emulator. Close the emulator click the triangle and choose "Wipe Data"

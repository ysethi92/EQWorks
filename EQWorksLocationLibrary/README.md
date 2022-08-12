# How to use the Library
- To run the library in any android application, we need to load it as a library by generating a AAR file.
- To get the AAR file, locate the eqworks.aar file inside the library folder
- Or generate a new AAR file :
- ## Generate a AAR File
  - Load the library folder in Android Studio
  - Go to Build/Make Selected Modules
  - A new AAR file will be generated inside \app\build\outputs\aar
  - Rename the file app-debug.aar file to eqworks.aar 
- Using the generated AAR file in your application.
  - Paste the AAR file in the libs folder inside the app folder of your application
  - Open the app module gradle file and add the dependency : implementation files('libs/eqworks.aar')
  - Sync your project, the library is ready to use.

# Features
- Robust SDK in kotlin 
# Mobile App
To use the app, you will need the following:
- [OI File Manager](https://github.com/openintents/filemanager/releases)
- [ODK-X Services APK](https://github.com/odk-x/services/releases/tag/2.1.8)
- [ODK-X Survey APK](https://github.com/odk-x/survey/releases/tag/2.1.8)
- [ODK-X Tables APK](https://github.com/odk-x/tables/releases/tag/2.1.8)

Once you download all of those prerequisites on your android phone, you can use the mobile app. To do this, launch ODK-X Tables. Click the settings in the top right, go to "Preferences", then click "Server Settings". You'll want to turn "Server Sign-on Credential" to be "Username" instead of anonymous. Then, set the following information:
- Server URL: https://capstone.odkxdemo.com
- Username: adminx
- Passowrd: x123Admin!

Then, you'll want to click "Authenticate New User". This will login you into the system and set up the app. Then, you can enter data accordingly. Whenever you are ready to sync, go into the settings and click "Sync".

# Developing the Mobile App
To develop with the mobile app, you'll need Java, NodeJs, Grunt, and the Android SDK. Follow the instructions [here at ODK-X's documentation](https://docs.odk-x.org/app-designer-prereqs/).

Additionally, you will need to set up your android device to work with ODK-X. To do this, you will need OI File Manager, ODK-X Services, ODK-X Survey, and ODK-X Tables (in that order). Detailed instructions on how to get these in the [ODK-X documentation here](https://docs.odk-x.org/basics-install/);

Once you get those, you can use the app. To start, clone the repo somehwere on your local computer. Navigate to that directory in a terminal. Run `npm install` to get the necessary node modules. Connect your android device to your computer and ensure USB debugging is enabled. To check if your android device is properly connected, you can use the command `adb devices`. If your device is connected, then you can do `grunt adbpush` (in the application directory). This will push the files to your android device. You can then open the application on your device. If the tables aren't there, click on the three dots in the top right, click "Preferences", click "Reset Configuration", and then click OK. They then should appear.

Additionally, you should click the three dots in the top right, click "Preferences", and then click "Server Settings". Change the "Server URL", "Username", and "Server Password" based on your ODK-X server. If "Server Sign-on Credential" is set to "None (anonymous access)", then change that to be "Username". It should then authenticate your identity and you should be good to go.

If you modify the app contents of the app directory to change the application, you should run `grunt` in the terminal. This will open up the Application Builder. Navigate to the "XLSX Converter" tab and select the form files that you made modifications to. These should be located at `/app/config/tables/tableId/forms/formId/formId.xlsx`. This will regenerate the rest of the necessary files.

# google-calendar-api
Connect to Google Calendar API and export to ICS

THIS IS A POC

	To Run this follow the following steps:

	Steps 1 through 3, for setting up OAuth . Follows instructions from : https://developers.google.com/calendar/auth
	1. Go to https://console.developers.google.com/ and make a new project
	2. Go to OAuth consent screen and set parameters as required. Mine is External.
	3. Create Credentials. 
	  3.1 Select Create OAuth client ID. 
	  3.2 Select Application type as Web Application.
	  3.3 Set Redirect URI to http://localhost:8888/Callback.

	4. Download your Credentials.json file
	5. Add it to the resource folder in project. Set the file path in variable CREDENTIALS_FILE_PATH in CalendarQuickstart.java.
	6. Set FOLDER_PATH in CalendarQuickstart.java to set the Folder Path you want to create the ICS files in.
	7. Run the file CalendarQuickstart.java. 


	NOTE: To test I imported the Files back to Google Calendar. To make ics file work in google calendar, set UID to UID:X. 
	      It might give "not enough persmission" error if not done.

	NOTE: This is far from done. This is just a POC and code hasn't been refactored either.

	NOTE: There are certain parameters like :
	    X-MICROSOFT-CDO-APPT-SEQUENCE:2
			X-MICROSOFT-CDO-BUSYSTATUS:TENTATIVE
			X-MICROSOFT-CDO-IMPORTANCE:1
	    which can be found in ics file when exported from Google Calendar but not sent as response through endpoint.

NOTE: use GoogleCalendarToICSFile.java to pass userName, accessToken, folderPath as arguments.

Please feel free to suggest changes to make it better.


NOTE: 

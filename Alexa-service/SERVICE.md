### Team + Unity IDs

* Raghavendra Nayak Muddur (rmuddur)
* Sachin Saligram (ssaligr)
* Kshitija Mohan Murudi (kmurudi)
* Vishal Kumar Seshagirirao Anil (vseshag)

## SELENIUM TESING - PART OF MILESTONE - BOT 

So, due to extension granted by Professor Parnin for our project, we have Selenium testing due for Bot Milestone, included in the folder for this milestone. The link for folder for Selenium testing is -> [Selenium](https://github.ncsu.edu/rmuddur/Scrumster/tree/master/Alexa-service/Selenium)

The file which includes all the Selenium tests -> [WebTest.java](https://github.ncsu.edu/rmuddur/Scrumster/blob/master/Alexa-service/Selenium/src/test/java/selenium/tests/WebTest.java)

## Screencast for Selenium Testing ->
[Selenium-Screencast](https://youtu.be/ChUwR0zFxh4)

## SERVICE MILESTONE ->
In this milestone, we have implemented all the below listed components ->

* Use Case #1 Implementation (20%)
* Use Case #2 Implementation (20%)
* Use Case #3 Implementation (20%)
* Task Tracking -- WORKSHEET.md (20%)
* Screencast (20%)

For deploying our service, we have used Spring framework and the entire code is in Java and our Java service is deployed on AWS Lambda. We have implemented separate controllers to handle different use cases. To automate the backend service we have used JIRA REST APIs and Google Calendar API. Alexa is used as Scumster - doing the voice integration and we have defined the required Alexa skills so as the interpret the user's request and give required output.

## Screencast -

The screencast includes the demo of all our 3 uses cases and how we have used Amazon's Alexa as the Scrumster. 

Link for screencast - [Service-Screencast](http://www.youtube.com/watch?v=snJTRYRXu4I)

## Use Case #1 Implementation -

Use Case: Move tasks from one state to another as per user input.

User gives two inputs - 1) Task ID, 2) Status of task, and Scrumster shall move the task accordingly. 
This has been implemented by using the JIRA REST APIs. Service is implemented in our Java service, functions mentioned in controller - [ScrumController.java](https://github.ncsu.edu/rmuddur/Scrumster/blob/master/Alexa-service/ScrumsterService/src/main/java/com/nmvk/controller/ScrumController.java)

## Use Case #2 Implementation - 

Use Case: Sprint Summary and Feedback

Scrumster shall give the team sprint summary, saying how many points have been burnt. This too has been implemented by using JIRA REST APIs and the service has been included in [ScrumController.java](https://github.ncsu.edu/rmuddur/Scrumster/blob/master/Alexa-service/ScrumsterService/src/main/java/com/nmvk/controller/ScrumController.java)

## Use Case #3 Implementation - 

Use Case: Schedule a meeting for the team.

Scrumster schedules a meeting for team by taking the "date" as input. We implemented this using our Java service which uses Google Calendar API to access the team's users' calendars and schedule a meeting accordingly.
The service for this use case has been included in a separate controller - [CalendarController.java](https://github.ncsu.edu/rmuddur/Scrumster/blob/master/Alexa-service/ScrumsterService/src/main/java/com/nmvk/controller/CalendarController.java)

## Task Tracking - 

We have tracked our tasks throughout the period we worked for this milestone using the Atlassian product - JIRA software and the worksheet where we have tracked tasks is present here ->
[WORKSHEET.md](https://github.ncsu.edu/rmuddur/Scrumster/blob/master/Alexa-service/WORKSHEET.md)

## Credentials - 

This milestone may require login credentials for several accounts. For convenience, we have provided all credentials that will allow anyone to login into the mentioned accounts.

1. JIRA Account
* Link: https://scrumster.atlassian.net/secure/RapidBoard.jspa?rapidView=1
* Username: scrumuser2017@gmail.com
* Password: scrumster2017

2. Google Calendar Account
* Link: http://calendar.google.com
* Username: sachin@ouruse.com
* Password: scrumster2017

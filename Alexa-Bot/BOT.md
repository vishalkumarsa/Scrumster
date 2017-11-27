## BOT Milestone phase

__Team Members (Unity Id) and Contributions__
* Raghavendra Nayak Muddur (rmuddur): Java code implementation for usecases, Lambda function mapping
* Kshitija Murudi (kmurudi): Mocking Service Component, Use Case Refinement
* Vishal Kumar Seshagirirao Anil (vseshag): Task and JIRA scrum board Tracking , Design of Usecase interactions, BOT.md documentation
* Sachin Saligram (ssaligr): Intent scheme creation, Infrastructure setup, Selenium Testing

### Use Case Refinement
#### Refined BOT Description
Scrumster is a bot designed to serve as an advocate of Scrum. The end goal would be to replace one of the existing Scrum roles (scrum master in particular), but at the level of implemetation we are focusing on, we would want the bot to be a catalyst for the overall Scrum process within teams in an organization. Most of us who have experience working in Scrum teams find updating Kanban boards to be a laborious task. Scrumster looks to aid in the process of updating tasks by having a simple voice interaction, through an Alexa application, with engineers during a stand-up to update tasks pertaining to a sprint. At the back end, it is tied to JIRA/Trello APIs to acheive this task. Scrumster aims to provide the following functionality:

* Assist in moving tasks from one state to another (for eg. from 'In-Progess' to 'Done'). 
* Schedule meetings for a duration of 30 minutes or 1 hour when all team members are available. This is achieved by accessing their calendars via the Google Calendar API.
* Summarize the sprint up to that point and provide feedback as to whether the sprint is on track or not.

#### Use Cases

1. Use Case - 1
  
  ```
  
Use Case: Move tasks from one state to another as per user input.
1 Preconditions
     User must have access to the Scumster Bot and his/her name must be listed as members of the JIRA Scrum team. The Scrum has to be triggered to be started, user will do this by using a key phrase like "Hey Scrumster". 
2 Main Flow
     After the user starts the Scrumster, the users can give instructions to Scrumster Bot [S1] to move tasks by mentioning the task ID that will be recognized by the Bot[S2]. The user can ask Scrumster to move from any of the valid states. Example - user says "Move task AT-10 to 'Done'/'Completed' column. 
     
3 Subflows
    [S1] User gives Bot instructions to move tasks by calling out task IDs.
    [S2] Bot will move the tasks by recognizing the task ID.
4 Alternative Flows
    [E1] No tasks make transition from one state to the other in the collected updates.
  
  ```
  
2. Use Case - 2

```
Use Case: Sprint Summary and Feedback 
1 Preconditions
   Scrumster Bot should have finished with all the team members' updates. 
2 Main Flow
   At the end of the stand-up meeting, one of the users will use a key-phrase such as "Scrumster, End the meeting" to indicate that the team has finished giving updates and tasks are done moving[S1]. Scrum Bot will analyze the Scrum Board and talk about the progress of the team[S2]. Bot will review performance of the team and comment on current progress v/s previous sprint performance[S3]. 
3 Subflows
  [S1] Bot gets input from user to end the current meeting.
  [S2] Bot will analyze the Scrum board(JIRA/Trello) for tracking the team's overall progress.
  [S3] Talk about overall performance of the team and comparison with past sprint performance.
  
```

3. Use Case - 3

```
Use Case: Schedule a meeting for the team.
1 Preconditions
   All users must have access to Scrum Board.    
2 Main Flow
   One of the users gives speech instruction to Scrumster Bot to schedule a meeting indicating the duration of meeting (we will support only 30mins/1hour meeting slots) [S1]. This will be done for the entire team. Bot checks the calendars of all the team members and schedule a team meeting [S2]. Sends out a notification about the meeting timings [S3]. If no timings match for all the team members, Bot will notify that meeting cannot be scheduled [E1].
3 Subflows
  [S1] User asks Scrumster to schedule a team meeting.
  [S2] Bot checks calendars of all the team members.
  [S3] Bot gives back an update to team mentioning the meeting slot.
 
4 Alternative Flows
  [E1] No timings matching for team members.
```
### Mocking Service Component
For mocking sevice component we have used the "WireMock" framework. The key challenge that we faced in this step is to mock the service component in AWS Lambda. Many other frameworks do not seem to address the service mocking requirements or do not work well in AWS lambda. All potential REST API calls to the service have been mocked. 

MockServer class in the package `com.nmvk.scrumster.mock` contains mock server logic. MockData class in `com.nmvk.scrumster.mock` contains mock data used in the Lambda. One of the design considerations in AWS lambda function is to keep less computation logic in the Lambda function, this is required to reduce the latency of response.

### Bot Implementation
#### Bot platform
Every implementation of an Amazon Alexa application has an intent scheme.The intent scheme is linked to an Amazon lambda function. The lambda function continuously listens for input from the intent scheme through the Alexa module or simulator. For eg. if a user says "schedule meeting", the lambda function picks up the intent and maps to the corresponding funtion within the lamdba. This java function would help realize the back-end integration required for the intent (Google calender API call, Integration with JIRA/Trello etc). We have developed our bot in the echosim platform, which is a simulator for Alexa that Amazon provides. We have hooks into the platform through both text and voice input which will be demonstrated in our screencast. We have defiend an intent scheme as follows:

```
{
  "intents": [
    {
      "name": "AMAZON.CancelIntent",
      "samples": []
    },
    {
      "name": "AMAZON.HelpIntent",
      "samples": []
    },
    {
      "name": "AMAZON.StartOverIntent",
      "samples": [
        "start over",
        "restart",
        "start again\n",
        "play again"
      ]
    },
    {
      "name": "AMAZON.StopIntent",
      "samples": []
    },
    {
      "name": "MoveWorkItem",
      "samples": [
        "move task",
        "move task {taskid}",
        "move story",
        "move story {taskid}",
        "move story {taskid} to {status}",
        "move task {taskid} to {status}",
        "{status}",
        "{taskid}"
      ],
      "slots": [
        {
          "name": "taskid",
          "type": "AMAZON.NUMBER",
          "samples": []
        },
        {
          "name": "status",
          "type": "Status",
          "samples": []
        }
      ]
    },
    {
      "name": "ScheduleMeeting",
      "samples": [
        "schdule meeting",
        "schedule meetind {day}",
        "can you schedule meeting",
        "can you schedule meeting {day}",
        "{day}"
      ],
      "slots": [
        {
          "name": "day",
          "type": "AMAZON.DATE",
          "samples": []
        }
      ]
    },
    {
      "name": "Summary",
      "samples": [
        "to tell spring summary",
        "summary",
        "tell summary",
        "what is summary",
        "end stand up",
        "end meeting"
      ],
      "slots": []
    }
  ],
  "types": [
    {
      "name": "Status",
      "values": [
        {
          "id": null,
          "name": {
            "value": "Done",
            "synonyms": [
              "done",
              "dne"
            ]
          }
        },
        {
          "id": null,
          "name": {
            "value": "In-Progress",
            "synonyms": [
              "in-progress",
              "inprogress",
              "In-progress"
            ]
          }
        }
      ]
    }
  ]
}
```

#### Bot Integration
The bot has been full integrated for the 3 use cases defined with all possible input/output combinations through voice and text as illustrated in the screencast. An example of the good path is as shown below for the 3 use cases: 
##### UseCase1:
I/p: Ask Scrumster Move Task 20 to Done

O/p: Your task has been successfully moved to done 
##### UseCase2:
I/p: Ask Scrumster to End stand-up

O/p: Your team has burnt 12 points and is on track
##### UseCase3:
I/p: Ask Scrumster schedule meeting today

O/p: Meeting Scheduled for October 25th at 2:30 PM

We have used mocked data for the JIRA/Trello board updates and reads for now. In the next phase the bot would read from JIRA APIs to fetch sprint summaries and update tasks from one state to the other. It would also look up google calender data instead of mocked data for team availability to schedule a meeting. 

### Selenium Testing
*** We are building an Alexa based bot. For our Selenium testing we were logging in to Amazon Developer and providing text commands in echo simulation to invoke our bot implementation. However, we were not able to login to Amazon developer account using selenium since Amazon systems detect automation scripts and give a captcha to solve in order to make sure its not a Robot. So if a captcha is thrown the user will have to key his/her response in manually. We spoke to the professor and he told us that he will make an exception for our project to post-pone selenium testing until the next milestone, when we can run it on the JIRA/Trello page to test actual updation of data through the Alexa interface.  

### Task Tracking

We started using JIRA to track our project in terms of sprints from the first milestone itself. The following image gives a summary of the epic that we completed as part of that milestone (DESIGN).
![m1_jirasummary](https://media.github.ncsu.edu/user/8297/files/311342b4-b98b-11e7-9133-01645740bd83)

We planned the entire sprint for milestone BOT ahead in time and the following is the screenshot of the backlogs at the beginning of the sprint.
![before_sprint](https://media.github.ncsu.edu/user/8297/files/183677ac-b9cc-11e7-80df-56bb76d55766)

### Worksheet
The Worksheet for Milestone 2 (BOT) can be found here: [Worksheet](https://github.ncsu.edu/rmuddur/Scrumster/blob/master/Alexa-Bot/WORKSHEET.md)

### Screencast

The following are the links to our screencast: 

[Demo1- Text i/p o/p](https://youtu.be/z-fNjAiUzsw)

[Demo2- Voice i/p o/p](https://youtu.be/lqh-eRfbq3g)



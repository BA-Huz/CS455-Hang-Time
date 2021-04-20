# CS455-Hang-Time
![image](https://user-images.githubusercontent.com/56166683/115088823-4996c000-9ece-11eb-9dfb-4dc85e170846.png)

Hang Time is a social and personal time managment app that alows friend groups to see and organize social events at the best times for all. Users can add into their personal calendar things like work or times when they are busy. Users can then create groups with their friends that allow them to see the times group members are free and times they are busy in order to schedule group events.

**Put screenshots of the app here**

## Table of Contents
- [Installation](#installation)

- [App Usage](#app-usage)
  - [Starting Out](#starting-out)
  - [Your Personal Calendar](#your-personal-calendar)
  - [Groups](#groups)

- [Use Case](#use-case)

- [Manifest](#maniest)

- [Future Changes](#future-changes)

## Installation
Download a copy of the repo and ensure all libraries and dependencies are up to date. To run your own instance of Hang Time, you will need to create a Google Firebase application for it to interface with. There are instructions on how to do that [here](https://firebase.google.com/docs/android/setup).
Once your application is linked with Firebase, you will need to create collections in Firestore. Create three collections called "events", "groups", and "users" respectively. You will also need to update the Firestore rules to allow authenticated users to access Firestore. Change the rules to the following to allow all authenticated users read and write access.
```javascript
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```
You should be able to build and run the application now. When running the application, you may receive _collection not indexed_ errors when querrying Firestore. These errors should contain a link that will automatically index the appropriate collections and prevent these errors from happening again in the future.

## App Usage
### Starting Out
Your first time using Hangtime will look quite similar to your first use of many other popular applications. Users will be presented with a login screen. If you have signed up previously feel free to login. If this is your first time then click the "NEW USER" buttton. This button will take you to the sign up page which will ask you for a username to use, and your email and password. Upon signing up or logging in you will be taken to the groups page. If you were to close the app and re open it you would always be taken to this page until you log out.

### Your Personal Calendar
In your personal calendar you can choose to add an event. Events need a name, and a starting and ending time you can also chose if this event is a repeating event or a one time event. Once you have submitted an event youll notice that selecting the day on the calendar in which your event takes place will show it in a list. This list shows all the events in your currently selected day.

### Groups
You can create groups with the emails of other users and then youll see all your groups your a member of in the group list. Clicking on a group brings you to that groups calendar. You'll notice this page is quite different then your personal calendar. You will see two days at a time as coloumns divided by time. You will also see grey regions spanning these days. These regions represent the times when the other group members are busy with personal events or other group events. the darker the grey the greator the percentage of group members are busy at that time. Touching the grey region will toast who is busy at that point in time. You can schedule group events much like you could personal events with a start and end time. These group events will also show on the calendarbut as blue regions.

## Use Case

## Manifest
* FirebaseDataObjects.kt
* FragmentEventEditor.kt
* FragmentPersonalCalendar.kt
* GroupCalendar.kt
* GroupCreate.kt
* GroupList.kt
* MainActivity.kt
* PersonalSchedule.kt
* SignUp.kt
* TwoDayViewFragment.kt
* UserSelectFragment.kt
* activity_group_calendar.xml
* activity_group_create.xml
* activity_group_list.xml
* activity_main.xml
* activity_personal_schedule.xml
* activity_sign_up.xml
* fragment_event_editor.xml
* fragment_personal_calendar.xml
* fragment_tow_day_view.xml
* fragment_user_select.xml
* item_event.xml
* item_user.xml

## Future Changes
Some features that weren't able to make the initial release of Hang Time are still in demand. 
### Editing Events and Groups
While modifying Firestore documents is trivial, creating the UI to easily and intuitively edit events or groups is more complicated. The next feature we'd like to see implemented is a simple way to edit these documents so that users can easily fix mistakes or update their plans as things change.
### Push Notifications
Automatically notifying your friends of upcoming scheduled events, or letting them know about events you've created right now. Notifications would alleviate the hassle of having to constantly check Hang Time to see who is available. 
### Friend Codes
Currently, inviting a friend to one of your groups requires you to know their email address. In the future, we'd like to implement a variety of easier ways to connect with friends such as scanable QR codes, hyper links, and short codes that can easily be used to join grups or add people to them without requiring you to know everything about someone.




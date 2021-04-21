# CS455-Hang-Time
<p align="center">
  <img src="https://user-images.githubusercontent.com/56166683/115088823-4996c000-9ece-11eb-9dfb-4dc85e170846.png" width="300" height="300">
</p>

Hang Time is a social and personal time managment app that alows friend groups to see and organize social events at the best times for all. Users can add into their personal calendar things like work or times when they are busy. Users can then create groups with their friends that allow them to see the times group members are free and times they are busy in order to schedule events. While it can be used like other traditonal time managment applications to schedule large events well inadvance such as a party or dance, Hang Time hopes to fill a void that is currently missing by giving users the ability to easily schedule less formal events on short notice. By consolidating scheduled events for users, Hang Time hopes to replace messy group chats that are currently used by many to try to schedule these events.

Hang Time is created by Brandon Huzil and Viktor Fries

## Table of Contents
- [Installation](#installation)
- [App Usage](#app-usage)
  - [Starting Out](#starting-out)
  - [Your Personal Calendar](#your-personal-calendar)
  - [Groups](#groups)
- [Manifest](#maniest)
- [Future Changes](#future-changes)
  - [Editing Events and Groups](#editing-events-and-eroups)
  - [Push Notifications](#push-notifications)
  - [Recurring Events](#recurring-events)
  - [Friend Codes](#friend-codes)
- [Contact Us](##-contact-us)
- [Copyright](#copyright)
- [Bug Report](#bug-report)

## Installation
Download a copy of the repo and ensure all libraries and dependencies are up to date. Using android studio will allow you to build and install a copy of the app onto an android device. 
### Custom Database
To run your own instance of Hang Time, you will need to create a Google Firebase application for it to interface with. There are instructions on how to do that [here](https://firebase.google.com/docs/android/setup).
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
In your personal calendar you can choose to add an event. Events need a name, and a starting and ending time. Once you have submitted an event youll notice that selecting the day on the calendar in which your event takes place will show it in a list. This list shows all the events in your currently selected day.
<p align="center">
  <img src="https://user-images.githubusercontent.com/56166683/115486829-aa96fe80-a214-11eb-9351-42658c8a411f.jpg" width="200" height="400">
</p>

### Groups
You can create groups with the emails of other users and then youll see all your groups your a member of in the group list. Clicking on a group brings you to that groups calendar. You'll notice this page is quite different then your personal calendar. You will see two days at a time as coloumns divided by time. You will also see grey regions spanning these days. These regions represent the times when the other group members are busy with personal events or other group events. the darker the grey the greator the percentage of group members are busy at that time. Touching the grey region will toast who is busy at that point in time. You can schedule group events much like you could personal events with a start and end time. These group events will also show on the calendarbut as blue regions.
<p align="center">
  <img src="https://user-images.githubusercontent.com/56166683/115486840-aff44900-a214-11eb-8e83-d50282a7d097.jpg" width="200" height="400">
</p>

## Manifest
### FirebaseDataObjects.kt 
This data class contains objects used to interact with Google Firestore, as well as a few related utility functions. If new Firestore document types are needed, or existing documents require new fields, the appropriate updates should be made here. To prevent possible errors and mistakes, object values should not be nullable if they are required.
### FragmentEventEditor.kt
This fragment contains the UI elements to create or edit an event.
### UserSelectFragment.kt
This fragment contains an autocomplete TextView that can be populated with a list of users. It maintains an internal list of selected users that can be retrieved for populating group members or event particpants.
### Layout
All layout.xml files are contained here. If new layouts are needed, or changes to existing ones should be made, the files will be in here.

## Future Changes
Some features that weren't able to make the initial release of Hang Time are still in demand. 
### Editing Events and Groups
While modifying Firestore documents is trivial, creating the UI to easily and intuitively edit events or groups is more complicated. The next feature we'd like to see implemented is a simple way to edit these documents so that users can easily fix mistakes or update their plans as things change.
### Push Notifications
Automatically notifying your friends of upcoming scheduled events, or letting them know about events you've created right now. Notifications would alleviate the hassle of having to constantly check Hang Time to see who is available. 
### Recurring Events
While Hang Time events are generally spontaneous, we'd like to add the functionality for users to easily set up repeating events. This would allow users to easily mark off times they are always busy, such as with work, or schedule group events for the same time every week, such as RPG meetups.
### Friend Codes
Currently, inviting a friend to one of your groups requires you to know their email address. In the future, we'd like to implement a variety of easier ways to connect with friends such as scanable QR codes, hyper links, and short codes that can easily be used to join grups or add people to them without requiring you to know everything about someone.

## Contact Us
If you need to get in contact with the main authors for any reason, you can reach us using the information below.
Brandon Huzil - [Email](mailto:ba.huzil@sasktel.net?subject=[GitHub]%20Hang%20Time)
Viktor Fries - [Email](mailto:vafries@gmail.com?subject=[GitHub]%20Hang%20Time)

## Copyright
This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to <http://unlicense.org/>

## Bug Report
Currently we have radio buttons that will have no effect. When creating an event you will see 3 radio buttons that say "Non Repetitive", "Daily Event", and "Weekly Event". as of now these widgets are not hooked up to anything. See [Recurring Events](#recurring-events) for details. When adding a new group event, it wont actually display the blue region on that day until you have performed a swipe or have changed pages.

See a bug we dont? [Email](mailto:vafries@gmail.com?subject=[GitHub]%20Hang%20Time) us so we can squash it!




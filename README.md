# Project Timeslot

The purpose of this project is to create a scheduling system for our [padel](https://en.wikipedia.org/wiki/Padel_(sport)) games that is better than Whatsapp discussions. There are plenty of apps that allow you to schedule events, therefore my intention is to create an extremely tailored solution that is specific to our use case. I primarily want to get some practice in with Scala, in addition to having a go at frontend development with React.

## My initial plan

I want to create a web client for both selecting free padel court reservations and allowing willing members to signal their willingness to participate for any given available reservation. Generally, most users will be using this via a mobile device, therefore I will prioritize mobile the most. Secondary usage will be on desktop. 

Down the line I would want to either create an Electron app or a mobile app (probably would be Android only). If I have the motivation, I will attempt both.

## Step 1

The first course of action is to read up on the available technologies to settle on the most feasible alternatives. At this point I have already selected React for the frontend technology, and additionally express a strong desire for the backend to be Dockerized. Once the technology stack has been outlined (probably will be subject to change), I will begin working via a combination of trial-and-error and loose planning. Spending too much time on planning is a guaranteed way to exhaust all motivation for the project, thus I will try to focus more on coding.

## Step 2

Now that I locked the framework down (Play framework), I will begin developing the MVP with the following set of features (some already completed):
* Scala REST API with ability to:
    * Get court times by date for all locations (Padel Tampere, Padeluxe)
    * Get court times by start and end datetime
* An intial React frontend for:
    * providing datetime filters
    * and displaying results

**NOTE:** The template used is play-scala-rest-api-example provided by Lightbend: https://github.com/playframework/play-samples/tree/2.8.x/play-scala-rest-api-example

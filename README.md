slackpts
========


The slack points server!

Uses simple HTTP POST requests to and from a webhook sent by Slack. The server simply keeps track
of users with a point system. The server is also able to talk back to Slack given the proper
webhook url.

Currently uses the Apache Http Client library to send http POST requests to slack. Note to compile, 
you need to reference the relevant libaries provided here. The licenses for each can be found in 
their respective folders.


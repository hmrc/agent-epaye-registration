# agent-epaye-registration

[![Build Status](https://travis-ci.org/hmrc/agent-epaye-registration.svg)](https://travis-ci.org/hmrc/agent-epaye-registration) [ ![Download](https://api.bintray.com/packages/hmrc/releases/agent-epaye-registration/images/download.svg) ](https://bintray.com/hmrc/releases/agent-epaye-registration/_latestVersion)

This is a backend microservice for Agent EPAYE Registration, part of interim replacement of OPRA (On-Line Pre-Registration of Agents).

The OPRA system provides a way for PAYE agents (not otherwise known to PAYE systems and therefore without known facts) to obtain a reference number that can be used as a known fact to enable them to register and enrol as a PAYE agent.
 
 ## Features
 
 - Agent who needs an Agent PAYE reference code is able to enter its details and can be issued a code
 - Team capturing OPRA data has a way for stored data to be extracted so that it is available to other services

## Running the tests

    sbt test it:test

## Running the app locally

    sm --start AGENT_EPAYE_REG -f
    sm --stop AGENT_EPAYE_REGISTRATION
    ./run-local

## API

We're still building this service so some/all of the API described here might not be implemented yet!

### Register for an Agent EPAYE reference code

    POST /agent-epaue-registration/registrations

Request body:

    {
        "agentName": "<Agent's name>",
        "contactName": "<Agent's contact name",
        "telephoneNumber": "<Agent's telephone number>",
        "faxNumber": "<Agent's fax number>",
        "emailAddress": "<Agent's email address>",
        "address": {
            "addressLine1": "<Agent's address line 1>",
            "addressLine2": "<Agent's address line 2>",
            "addressLine3": "<Agent's address line 3>",
            "addressLine4": "<Agent's address line 4>",
            "postCode": "<Agent's postcode>"
        }
    }

Possible responses:

#### OK

    {
        "payeAgentReference": "<New Agent EPAYE reference code>"
    }

#### Bad Request

    {
        "errors": [
            {
                "code": "<ERROR_CODE>",
                "error": "<Error message>"
            }
        ]
    }

This endpoint validates the json input and returns Bad Request with a collection of error codes and messages for invalid input.
An example of error code is ```MISSING_FIELD```.

### License


This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

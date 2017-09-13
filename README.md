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

It should then be listening on port 9445

## API

We're still building this service so some/all of the API described here might not be implemented yet!

### Register for an Agent EPAYE reference code

    POST /agent-epaye-registration/registrations

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
                "message": "<Error message>"
            }
        ]
    }

This endpoint validates the json input and returns Bad Request with a collection of error codes and messages for invalid input.
An example of error code is ```MISSING_FIELD```.

### Extract registrations between a date range

This endpoint is secured by the [STRIDE (internal) Auth](https://confluence.tools.tax.service.gov.uk/display/PE/STRIDE+%28Internal%29+Auth)
model. It requires requests to be from signed in users authorised with the "T2 Technical" auth group.

    GET /agent-epaye-registration/registrations?dateFrom=yyyy-MM-dd&dateTo=yyyy-MM-dd

The ```dateFrom``` and ```dateTo``` parameters follow the ISO 8604 date format with a full date as four digit year, two digit
month of year, and two digit day of month (yyyy-MM-dd), e.g. ```2017-02-27```.
The date range may span 1 or more days, up to a year, but the dates must be in the past - today's date is not allowed.

Possible responses:

#### OK

If there's no registrations within the date range, then a HTTP 200 response is returned with a body like:

    {
        "registrations": [],
        "complete" : true
    }

If there are registrations withint the date range, a HTTP 200 response is given with a body like:

    {
        "registrations": [
            {
                "agentReference": "HX2000",
                "agentName": "Dave Agent",
                "contactName": "Charlie Contact",
                "telephoneNumber": "04372895",
                "faxNumber": "04372895",
                "emailAddress": "some@email.com",
                "addressLine1": "First line of address",
                "addressLine2": "Second line of address",
                "addressLine3": "Optional 3rd line of address",
                "addressLine4": "Optional 4th line of address",
                "postCode": "CC111CC",
                "createdDateTime": "2017-09-08T10:03:29.544Z"
            },
            {
                "agentReference": "HX2001",
                "agentName": "Some Agent",
                "contactName": "Some Contact",
                "addressLine1": "First line of address",
                "addressLine2": "Second line of address",
                "postCode": "DD111DD",
                "createdDateTime": "2017-09-09T01:01:01.000Z"
            }
        ],
        "complete" : true
    }

The following JSON fields are optional and will be omitted if there is no value:
- ```telephoneNumber```
- ```faxNumber```
- ```emailAddress```
- ```addressLine3```
- ```addressLine4```

The ```createdDateTime``` field is a combined date and time in UTC in ISO 8601 format (format is yyyy-MM-ddTHH:mm:ss.SSSZZ).

As the response is streamed, the ```complete``` field indicates whether all registrations were returned in
their entirety and without error.
If ```complete``` is true then no error occured and all registrations
within the date range where returned.
If ```complete``` is false then an error meant that not all
registrations within the date range could be returned.

#### Bad Request (date parsing failure)

If one of the date parameters can not be parsed, a Bad Request is returned with a JSON body like:

    {
        "statusCode": 400,
        "message": "'To' date must be in ISO format (yyyy-MM-dd)",
        "requested": "/agent-epaye-registration/registrations?dateFrom=2017-08-10&dateTo=2017-09-1x"
    }

#### Bad Request (date validation failure)

The endpoint validates the dates and returns a Bad Request with a collection of error codes and messages for invalid input.
An example of an error code is ```INVALID_DATE_RANGE```.

    {
        "errors": [
            {
                "code": "<ERROR_CODE>",
                "message": "<Error message>"
            }
        ]
    }

### License


This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

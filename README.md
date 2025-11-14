# agent-epaye-registration
[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

This is a backend microservice for Agent EPAYE Registration, part of interim replacement of OPRA (On-Line Pre-Registration of Agents).

The OPRA system provides a way for PAYE agents (not otherwise known to PAYE systems and therefore without known facts) to obtain a reference number that can be used as a known fact to enable them to register and enrol as a PAYE agent.
 
 ## Features
 
 - Agent who needs an Agent PAYE reference code is able to enter its details and can be issued a code
 - Team capturing OPRA data has a way for stored data to be extracted so that it is available to other services

## Running the tests
```bash
sbt test it/test
```

## Test Coverage
```bash
sbt clean coverage test coverageReport
```

## Running the app locally

Start service dependencies with:
```bash
sm2 --start AGENT_EPAYE_REG_ALL
sm2 --stop AGENT_EPAYE_REGISTRATION
```

And then run the service with:
```bash
./run.sh
```

It should then be listening on port 9445

## API

### Register for an Agent EPAYE reference code
```http request
POST /agent-epaye-registration/registrations
```

#### Request body:
```json
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
```

Unless otherwise specified, all fields allow uppercase/lowercase letters, numbers, spaces, and `.,()!@` punctuation.

| **Field**              | **Required?** | Type   | **Max Length** | **Constraints**                                                     |
|------------------------|---------------|--------|----------------|---------------------------------------------------------------------|
| `agentName`            | Required      | String | 56             |                                                                     |
| `contactName`          | Required      | String | 56             |                                                                     |
| `telephoneNumber`      | Optional      | String | 35             | Numbers and spaces only                                             |
| `faxNumber`            | Optional      | String | 35             | Numbers and spaces only                                             |
| `emailAddress`         | Optional      | String | 129            | Must be a valid email address                                       |
| `address.addressLine1` | Required      | String | 35             |                                                                     |
| `address.addressLine2` | Required      | String | 35             |                                                                     |
| `address.addressLine3` | Optional      | String | 35             |                                                                     |
| `address.addressLine4` | Optional      | String | 35             |                                                                     |
| `address.postCode`     | Required      | String | 8              | Must be a valid UK postcode (with or without space); BFPO supported |

#### OK
```json
{
	"payeAgentReference": "<New Agent EPAYE reference code>"
}
```

#### Bad Request
```json
{
	"errors": [
		{
			"code": "<ERROR_CODE>",
			"message": "<Error message>"
		}
	]
}
```

This endpoint validates the json input and returns `400 Bad Request` with a collection of error codes and messages for invalid input.

### License
 

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

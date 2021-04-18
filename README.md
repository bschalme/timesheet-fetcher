# Timesheet Fetcher

Grabs timesheet entries from the timeheet service and stuffs them into QuickBooks Desktop.

Set up the following environment variables:

* QBD_API_URL (Set to the base URL of the API Gateway)
* QBD_API_ACCESS_TOKEN (For now, Use Postman to obtain an access token)
* TSHEETS_REST_TOKEN (Get this from the TSheets console)

To build:

* ./gradlew build

To run:

1. . ./setenv
2. time java -jar build/libs/timesheet-fetcher-1.0.0-SNAPSHOT-all.jar

For now Timesheet Fetcher just grabs timesheets from June 1, 2020.
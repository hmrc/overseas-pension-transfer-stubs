
# overseas-pension-transfer-stubs

This is the stub microservice for Overseas Pension Transfer service. This service stubs responses for the HIP endpoints.

## Running the service

1. Make sure you run all the dependant services through the service manager:

   > `sm2 --start OVERSEAS_PENSION_TRANSFER_ALL`

2. Stop the frontend microservice from the service manager and run it locally:

   > `sm2 --stop OVERSEAS_PENSION_TRANSFER_STUB`

   > `sbt run`

The service runs on port `15602` by default.

## Test Data Matrix

getAll and getSpecific

The stub uses PSTR for the references for JSON files for returned payloads in order to mimic the data that is required by HIP.
In order to ensure the correct PSTR is sent the below matrix matches SRN (required for access to frontend), PSA/PSP ID (required for authentication in the frontend service).

| SRN         | PSTR       | PSAID    | PSPID    |
|-------------|------------|----------|----------|
| S2400000001 | 24000001IN | A2100005 | 21000005 |
|             |            | A2100007 | 21000007 |
|             |            | A2100011 |          |
|             |            | A2100021 |          |
|             |            | A2100022 |          |
|             |            | A2100032 |          |
|             |            | A2100041 |          |
|             |            | A2100042 |          |

submitTransfer

The stub for when a user submits their transfer to HMRC. The stub will return 201 status code and randomly generated QT reference and Form Bundle Number and the current timestamp is returned as the processingDate.

### Unit tests

> `sbt test`

### Integreation tests

> `sbt it/test`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
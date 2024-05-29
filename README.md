# eCMR backend

This is the backend application of the eCMR service. The Backend is designed to create, manage, archive sign and send an
eCMR ( digital convention relative au contrat de transport international de marchandises par route) with your partners
in road transports, including cross-border cases.
Core technology of the frontend is Java and the Sprint Boot Framework.

## Versions

The versions of the runtime environment and the most relevant frameworks used are listed below:

* Java: 21
* Sprint Boot: 3.2.4



## Run with docker compose

### Preparation
To start the project with docker compose, the ecmr-frontend and the ecmr-backend have to be in the same directory.

Prepare your ecmr-backend for using via docker compose. Add the next line to
application.properties:

    spring.security.oauth2.resourceserver.jwt.issuer-uri=https://idp.example.com/issuer

### Build

    docker compose build

### Run

    docker compose up

## Documentation

For more details, please refer to the `documentation` directory.

## License
Licensed under the Open Logistics Foundation License 1.3.
For details on the licensing terms, see the LICENSE file.

## Licenses of third-party dependencies

The licenses used by this project's third-party dependencies are documented in the `third-party-licenses` directory.
This is done to encourage developers to check the licenses used by the third-party dependencies to ensure they do not conflict with the license of
this project itself.
The directory contains the following files:

* `third-party-licenses.txt` - Contains the licenses used by this project's third-party dependencies.
  The content of this file is/can be generated.
* `third-party-licenses-complementary.txt` - Contains entries for third-party dependencies for which the licenses cannot be determined automatically.
  The content of this file is maintained manually.

## Generating third-party license reports

This project uses the [license-maven-plugin](https://github.com/mojohaus/license-maven-plugin) to generate a file containing the licenses used by the
third-party dependencies.
The content of the `mvn license:add-third-party` Maven goal's output (`target/generated-sources/license/THIRD-PARTY.txt`) can be copied
into `third-party-licenses/third-party-licenses.txt`.

Third-party dependencies for which the licenses cannot be determined automatically by the license-maven-plugin have to be documented manually
in `third-party-licenses/third-party-licenses-complementary.txt`.
In the `third-party-licenses/third-party-licenses.txt` file these third-party dependencies have an "Unknown license" license.

## Generate License Header

Add license header to all files:

    mvn license:format

Check all files, if a license header is given

    mvn license:check

## Contact information
  * Working Group Information
  * Development Team
  * etc.

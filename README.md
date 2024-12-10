# eCMR backend

This is the backend application of the eCMR service. The Backend is designed to create, manage, archive sign and send an
eCMR ( digital convention relative au contrat de transport international de marchandises par route) with your partners
in road transports, including cross-border cases.
Core technology of the frontend is Java and the Sprint Boot Framework.

## Versions

The versions of the runtime environment and the most relevant frameworks used are listed below:

* Java: 21
* Spring Boot: 3.2.11



## Run with docker compose

### Preparation
The docker compose setup starts all necessary components: the front-and backend, a postgres database and a keycloak instance.
To start the project with docker compose, the ecmr-frontend and the ecmr-backend have to be in the same directory.
All config files can be found in the config folder.

Note to the docker compose setup: When creating a token through the frontend (and therefore through the browser), the keycloak host is set as the issuer in the token.
When the backend validates the token the issuer must match the host in the
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI, which tells the backend how to reach keycloak.
Since the backend runs in a container and the browser runs on the local machine the url to keycloak is different (
localhost vs container name), which then leads to an error in the backend because the issuer is different.
As a solution a localhost service is used, which ensures that the host name is identical.

### Configuration
To start the applications these changes must be made first:

1. Edit the files config/postgres-params.env and config/backend-params.env to add usernames and passwords.
2. Change the user password in the realm configuration files.
3. To add other users you must add them to realm config file and to the backend project (resources/db/init-data.xml). Please verify that the email addresses are identical.

### Build & Run
Build the application with: ```docker compose build```

Run the application with: ```docker compose up```

The backend is available at http://localhost:8081.

The frontend is available at http://localhost:8082.

Note: this docker compose is for developing purposes only!

## Run Tests
Docker must be available to run the integration tests (src/test/e2e) as it is used to start the keycloak testcontainer.

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

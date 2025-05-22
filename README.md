# eCMR Backend

This is the backend application of the eCMR service. The backend is designed to create, manage, archive sign and send an
eCMR ( digital convention relative au contrat de transport international de marchandises par route) with your partners
in road transports, including cross-border cases.

The core technology of the backend is Java and the Spring Boot Framework.

## Project Context

This project is part of a broader ecosystem initiated by the [Open Logistics Foundation](https://openlogisticsfoundation.org/), which brings together logistics stakeholders to develop standardized, manufacturer-independent, and open-source software components for digital logistics infrastructure.

Specifically, this repository is maintained by members of the **[Working Group “Electronic Transport Documents”](https://openlogisticsfoundation.org/en/working-groups/electronic-transport-documents/)**,
whose goal is to define and implement digital document standards (like eCMR, delivery notes, transport orders, etc.).

## Getting Started

> **If you are new to the eCMR project, please start by exploring this
> repository**. After reading this Readme.md, please visit the
> [documentation section](https://gitlab.com/openlogistics/ecmr-backend/-/tree/main/docs) to understand the API structure and system architecture.


## Related Projects
- [eCMR Data Model](https://git.openlogisticsfoundation.org/wg-electronictransportdocuments/ecmr/ecmr-model)
- [eCMR Frontend](https://git.openlogisticsfoundation.org/wg-electronictransportdocuments/ecmr/ecmr-frontend)
- [eSEAL Open Source Implementation](https://git.openlogisticsfoundation.org/wg-electronictransportdocuments/ecmr/eseal)


## Features

- [RESTful API](openapi.yaml) for creating, updating, and retrieving eCMRs
- eSEAL (see for [eSEAL Repository](https://git.openlogisticsfoundation.org/wg-electronictransportdocuments/ecmr/eseal) more details) for signing eCMRs and verifying of signed eCMRs
- Role-based access and data model based on the official eCMR specification
- Integration hooks for ERP, TMS, and other relevant systems

## Project Structure

    ecmr-backend/
    ├── ecmr-backend/ # Java source code
    ├── documentation/ # eCMR project documentation
    ├── README.md # Project description
    ├── openapi.yaml # API description
    ├── LICENSE # Licensing information
    ├── docker-compose.yml # Docker compose setup for development purpose
    └── CONTRIBUTING.md # Contribution guidelines

## Technologies used

- Java 21
- Spring Boot 3.4.5
- OpenAPI Specification
- Docker

## Documentation

For more details, please refer to the `documentation/` directory.

## Contributing

We welcome contributions from the community! Please read our [CONTRIBUTING.md](./CONTRIBUTING.md) for guidelines and setup instructions.

## License
Licensed under the Open Logistics Foundation License 1.3.
For details on the licensing terms, see the LICENSE file.

## Maintainers & Contact
This repository is maintained by contributors from the **Open Logistics Foundation Working Group "Electronic Transport Documents"**.

Maintainers:
- Jens Leveling (jens.leveling@iml.fraunhofer.de)
- Artur Blek (artur.blek@rhenus.com)

For general questions or collaboration inquiries, please contact info@openlogisticsfoundation.org or visit the [official website](https://openlogisticsfoundation.org/en/contact/).

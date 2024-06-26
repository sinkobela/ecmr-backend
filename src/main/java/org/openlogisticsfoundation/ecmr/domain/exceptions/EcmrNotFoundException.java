package org.openlogisticsfoundation.ecmr.domain.exceptions;

import java.util.UUID;

public class EcmrNotFoundException extends Exception {
    public EcmrNotFoundException(UUID ecmrId) {
        super("Ecmr with id " + ecmrId + " not found");
    }
}

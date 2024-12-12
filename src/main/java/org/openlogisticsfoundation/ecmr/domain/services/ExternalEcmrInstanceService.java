/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import org.openlogisticsfoundation.ecmr.api.model.SealedDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
public class ExternalEcmrInstanceService {

    private WebClient webClient;

    public SealedDocument importEcmr(String uri, UUID ecmrId, String shareToken) {
        webClient = WebClient.builder().baseUrl(uri).build();
        return this.webClient.get()
            .uri("api/external/ecmr/{ecmrId}/export?shareToken={shareToken}", ecmrId, shareToken)
            .retrieve()
            .bodyToMono(SealedDocument.class).block();
    }
}

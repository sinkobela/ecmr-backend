/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public class PdfHelper {
    private PdfHelper() {}

    static ResponseEntity<StreamingResponseBody> createPdfResponse(byte[] ecmrReportData, String filename) {
        StreamingResponseBody streamingResponseBody = outputStream -> {
            try (InputStream inputStream = new ByteArrayInputStream(ecmrReportData)) {
                inputStream.transferTo(outputStream);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
            }
        };

        return ResponseEntity.ok().contentLength(ecmrReportData.length).contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"eCMR-"+ filename + ".pdf\"")
                .body(streamingResponseBody);
    }
}

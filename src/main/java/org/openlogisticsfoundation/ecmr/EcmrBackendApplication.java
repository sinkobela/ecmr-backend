/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"org.openlogisticsfoundation.ecmr", "ecmr.seal.verify", "ecmr.seal.sign"})
@EnableScheduling
public class EcmrBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcmrBackendApplication.class, args);
	}

}

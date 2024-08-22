/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.models;

import lombok.Getter;

@Getter
public class InternalOrExternalUser {
    private User internalUser;
    private ExternalUser externalUser;

    public InternalOrExternalUser(User internalUser) {
        if (internalUser == null) {
            throw new NullPointerException("InternalUser must not be null");
        }
        this.internalUser = internalUser;
    }

    public InternalOrExternalUser(ExternalUser externalUser) {
        if (externalUser == null) {
            throw new NullPointerException("ExternalUser must not be null");
        }
        this.externalUser = externalUser;
    }

    public boolean isInternalUser() {
        return this.internalUser != null;
    }

    public boolean isExternalUser() {
        return this.externalUser != null;
    }

    public String getFullName() {
        return String.format("%s %s",
                this.isInternalUser() ? this.getInternalUser().getFirstName() : this.getExternalUser().getFirstName(),
                this.isInternalUser() ? this.getInternalUser().getLastName() : this.getExternalUser().getLastName());
    }
}

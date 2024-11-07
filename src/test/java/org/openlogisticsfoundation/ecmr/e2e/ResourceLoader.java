/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.e2e;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

public class ResourceLoader {

    private ResourceLoader() {
    }

    /**
     * Loads the content of the resource file with the given path and returns it as a string.
     *
     * @param path The path to the file to load.
     * @return The content of the file as a string.
     */
    public static String load(String path) {
        requireNonNull(path, "path");

        try (InputStream is = ResourceLoader.class.getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + path, e);
        }
    }

}

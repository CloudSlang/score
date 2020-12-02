/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cloudslang.runtime.impl.python.external;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.io.FileUtils.deleteQuietly;


public class PythonResourceCleanupHolder implements Runnable {

    public static final int RETRIES_DELETE = 3;

    private final ConcurrentMap<String, Boolean> folders;
    private final LinkedList<String> couldNotDeleteList;

    public PythonResourceCleanupHolder() {
        this.folders = new ConcurrentHashMap<>();
        this.couldNotDeleteList = new LinkedList<>();
    }

    @Override
    public void run() {
        try {
            List<String> toDelete = new LinkedList<>();
            for (String folder : folders.keySet()) {
                if (deleteFolderWithRetry(folder)) {
                    toDelete.add(folder);
                } else {
                    couldNotDeleteList.add(folder);
                }
            }
            folders.keySet().removeAll(toDelete);
            couldNotDeleteList.removeIf(this::deleteFolderWithRetry);
        } catch (Exception ignore) {
        }
    }

    private boolean deleteFolderWithRetry(String folder) {
        for (int i = 1; i <= RETRIES_DELETE; i++) {
            if (deleteQuietly(new File(folder))) {
                return true;
            }
        }
        return false;
    }

    public void addResource(String path) {
        folders.put(path, TRUE);
    }
}

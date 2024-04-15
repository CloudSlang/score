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

package io.cloudslang.dependency.impl.services.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipUtil {

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public static void unzipToFolder(String folderPath, InputStream source) {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        File mavenHome = new File(folderPath);
        if(!mavenHome.exists()) {
            try (ZipInputStream zio = new ZipInputStream(source)) {
                ZipEntry ze;
                while ((ze = zio.getNextEntry()) != null) {
                    if(ze.isDirectory()) {
                        new File(mavenHome, ze.getName()).mkdirs();
                    } else {
                        try (FileOutputStream fos = new FileOutputStream(new File(mavenHome, ze.getName()))) {
                            int len;
                            while ((len = zio.read(buffer)) > 0)
                            {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

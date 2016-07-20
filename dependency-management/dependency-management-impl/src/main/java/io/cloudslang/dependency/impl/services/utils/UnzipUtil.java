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

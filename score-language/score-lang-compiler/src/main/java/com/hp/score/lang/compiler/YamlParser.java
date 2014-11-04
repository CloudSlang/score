package com.hp.score.lang.compiler;

import com.hp.score.lang.compiler.domain.SlangFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;

@Component
public class YamlParser {

    @Autowired
    private Yaml yaml;

    public SlangFile loadMomaFile(File source) {
        SlangFile slangFile;
        try (FileInputStream is = new FileInputStream(source)) {
            slangFile = yaml.loadAs(is, SlangFile.class);
        } catch (java.io.IOException e) {
            throw new RuntimeException("couldn't parse file for some reason", e);
        }
        return slangFile;
    }

}

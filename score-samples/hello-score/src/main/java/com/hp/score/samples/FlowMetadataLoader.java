package com.hp.score.samples;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 10/27/2014
 *
 * @author Bonczidai Levente
 */
public class FlowMetadataLoader {
    private final static Logger logger = Logger.getLogger(FlowMetadataLoader.class);

    public static List<FlowMetadata> loadPredefinedFlowsMetadata(String path) {
        List<FlowMetadata> predefinedFlows = new ArrayList<>();

        try {
            InputStream inputStream = FlowMetadataLoader.class.getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            String document = "";
            while ((line = reader.readLine()) != null) {
                document += line;
                document += "\n";
            }
            reader.close();

            Constructor constructor = new Constructor(FlowMetadataContainer.class);
            TypeDescription flowDescription = new TypeDescription(FlowMetadataContainer.class);
            flowDescription.putListPropertyType("flows", FlowMetadata.class);
            constructor.addTypeDescription(flowDescription);
            Yaml yaml = new Yaml(constructor);
            Object flowAsObject = yaml.load(document);
            predefinedFlows = ((FlowMetadataContainer) flowAsObject).getFlows();
        } catch (Exception ex) {
            logger.info(ex);
        }

        return predefinedFlows;
    }
}

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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

public class XmlDocUtils {

    public static DocumentBuilderFactory getSecuredDocumentFactory() {
        // Must add the following to avoid the XEE Vulnerability - https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            documentBuilderFactory.setFeature(FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setExpandEntityReferences(false);
        } catch (ParserConfigurationException ignore) {
        }

        return documentBuilderFactory;
    }

    public static TransformerFactory getSecuredTransformerFactory() {
        // Must add the following to avoid the XEE Vulnerability - https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        try {
            transformerFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            transformerFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            transformerFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            transformerFactory.setFeature(FEATURE_SECURE_PROCESSING, true);
        } catch (TransformerConfigurationException ignore) {
        }

        return transformerFactory;
    }
}

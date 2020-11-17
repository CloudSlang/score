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

import org.vibur.objectpool.PoolObjectFactory;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static java.lang.Integer.getInteger;


public class ViburDocumentBuilderPoolObjectFactory implements PoolObjectFactory<DocumentBuilder> {

    private static final int RETRY_CREATE_OBJ = getInteger("python.outputParser.retryCreateObject", 3);

    @Override
    public DocumentBuilder create() {
        try {
            return createDocumentBuilderFactory().newDocumentBuilder();
        } catch (ParserConfigurationException parserConfigurationException) {
            for (int i = 1; i <= RETRY_CREATE_OBJ; i++) {
                try {
                    return createDocumentBuilderFactory().newDocumentBuilder();
                } catch (ParserConfigurationException ignore) {
                }
            }
            throw new RuntimeException("Could not create document builder: ", parserConfigurationException);
        }
    }

    @Override
    public boolean readyToTake(DocumentBuilder documentBuilder) {
        documentBuilder.reset();
        return true;
    }

    @Override
    public boolean readyToRestore(DocumentBuilder documentBuilder) {
        documentBuilder.reset();
        return true;
    }

    @Override
    public void destroy(DocumentBuilder documentBuilder) {
        // intentionally blank
    }

    private DocumentBuilderFactory createDocumentBuilderFactory() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException ignore) {
        }
        documentBuilderFactory.setExpandEntityReferences(false);
        return documentBuilderFactory;
    }
}

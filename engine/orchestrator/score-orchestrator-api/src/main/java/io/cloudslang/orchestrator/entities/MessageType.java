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
package io.cloudslang.orchestrator.entities;

public enum MessageType {
    ITPA(0),
    RPA(1);

    private final int indexNumber;

    MessageType(int indexNumber) {
        this.indexNumber = indexNumber;
    }

    public int getIndexNumber() {
        return indexNumber;
    }

    public static MessageType find(int indexNumber) {
        MessageType messageType = null;

        switch (indexNumber) {
            case 0:
                messageType = MessageType.ITPA;
                break;
            case 1:
                messageType = MessageType.RPA;
                break;
        }

        return messageType;
    }
}

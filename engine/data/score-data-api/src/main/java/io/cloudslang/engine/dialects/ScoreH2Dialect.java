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

package io.cloudslang.engine.dialects;

import org.hibernate.dialect.H2Dialect;

import java.sql.Types;

public class ScoreH2Dialect extends H2Dialect {

    @Override
    protected String columnType(int sqlTypeCode) {
        if (sqlTypeCode == Types.LONGVARBINARY) {
            return "blob";
        }

        if (sqlTypeCode == Types.LONGVARCHAR) {
            return "clob";
        }

        return super.columnType(sqlTypeCode);
    }

    @Override
    public String toBooleanValueString(boolean bool) {
        return String.valueOf(bool);
    }
}

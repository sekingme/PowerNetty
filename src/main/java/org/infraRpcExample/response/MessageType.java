/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.infraRpcExample.response;

import java.util.Arrays;

/**
 * @author jiangshequan
 * @title: MessageType
 * @date 2024/6/13 16:25
 */
public enum MessageType {

    /**
     * 枚举值
     */
    TEXT("text", "文本类型"),

    TABLE("table", "报表类型"),

    FILE("file", "文件类型"),

    ID("id", "整数类型ID");

    private final String name;

    private final String desc;

    MessageType(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public static MessageType of(String key) {
        return Arrays.stream(MessageType.values())
                .filter(systemType -> systemType.name.equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown system type key, key = " + key));
    }

    /**
     * Gets the value of name.
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of desc.
     *
     * @return the value of desc
     */
    public String getDesc() {
        return desc;
    }
}

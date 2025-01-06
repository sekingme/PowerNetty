/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.infraRpcExample.registry;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;

/**
 * @author: sekingme
 * @description: 注册器接口，不同的注册器继承各自实现
 * @create: 2024-09-26 16:04
 */
public interface Registry extends Closeable {
    void start(Map<String, String> config);

    boolean subscribe(String path, SubscribeListener listener);

    void unsubscribe(String path);

    void addConnectionStateListener(ConnectionListener listener);

    String get(String key);

    void put(String key, String value, boolean deleteOnDisconnect);

    void delete(String key);

    Collection<String> children(String key);

    boolean exists(String key);

    boolean acquireLock(String key);

    boolean releaseLock(String key);
}

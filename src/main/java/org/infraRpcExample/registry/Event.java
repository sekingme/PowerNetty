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

/**
 * @author: sekingme
 * @description: 业务层定义注册中心的操作
 * @create: 2024-09-30 11:52
 */
public class Event {
    private String key;
    private String path;
    private String data;
    private Type   type;

    public Event(String key, String path, String data, Type type) {
        this.key = key;
        this.path = path;
        this.data = data;
        this.type = type;
    }

    public Event() {
    }

    public static EventBuilder builder() {
        return new EventBuilder();
    }

    public String key() {
        return this.key;
    }

    public String path() {
        return this.path;
    }

    public String data() {
        return this.data;
    }

    public Type type() {
        return this.type;
    }

    public Event key(String key) {
        this.key = key;
        return this;
    }

    public Event path(String path) {
        this.path = path;
        return this;
    }

    public Event data(String data) {
        this.data = data;
        return this;
    }

    public Event type(Type type) {
        this.type = type;
        return this;
    }

    public String toString() {
        return "Event(key=" + this.key() + ", path=" + this.path() + ", data=" + this.data() + ", type=" + this.type() + ")";
    }

    public enum Type {
        ADD,
        REMOVE,
        UPDATE
    }

    public static class EventBuilder {
        private String key;
        private String path;
        private String data;
        private Type   type;

        EventBuilder() {
        }

        public EventBuilder key(String key) {
            this.key = key;
            return this;
        }

        public EventBuilder path(String path) {
            this.path = path;
            return this;
        }

        public EventBuilder data(String data) {
            this.data = data;
            return this;
        }

        public EventBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public Event build() {
            return new Event(key, path, data, type);
        }

        public String toString() {
            return "Event.EventBuilder(key=" + this.key + ", path=" + this.path + ", data=" + this.data + ", type=" + this.type + ")";
        }
    }
}

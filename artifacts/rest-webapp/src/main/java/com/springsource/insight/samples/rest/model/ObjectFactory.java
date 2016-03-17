/**
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.samples.rest.model;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * @author lgoldstein
 */
@XmlRegistry
public final class ObjectFactory {
    public ObjectFactory() {
        super();
    }

    public final RestfulData createRestfulData () {
        return new RestfulData();
    }

    public final RestfulDataList createRestfulDataList () {
        return new RestfulDataList();
    }
}

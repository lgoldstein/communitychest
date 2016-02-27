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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author lgoldstein
 */
@XmlRootElement(name="data-list")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class RestfulDataList extends ArrayList<RestfulData> {
	private static final long serialVersionUID = -8672870945043644748L;

	public RestfulDataList() {
		super();
	}

	public RestfulDataList(int initialCapacity) {
		super(initialCapacity);
	}

	public RestfulDataList(Collection<? extends RestfulData> c) {
		super(c);
	}

	@XmlElement(name="restful-data", nillable=true)
	public List<RestfulData> getInstances () {
		return this;
	}

	public void setInstances (List<RestfulData> l)	{
		if (size() > 0) {
			clear();
		}

		addAll(l);
	}
}

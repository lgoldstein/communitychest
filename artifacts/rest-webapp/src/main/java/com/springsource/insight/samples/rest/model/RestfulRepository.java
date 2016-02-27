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

import java.util.List;

/**
 * @author lgoldstein
 */
public interface RestfulRepository {
	List<RestfulData> findAll ();
	RestfulData getData (long id);	// returns null if no such ID found
	RestfulData create (int balance);
	RestfulData setBalance (long id, int balance); // returns non-null if found and updated
	RestfulData removeData (long id);	// return non-null if successful
	List<RestfulData> removeAll ();
}

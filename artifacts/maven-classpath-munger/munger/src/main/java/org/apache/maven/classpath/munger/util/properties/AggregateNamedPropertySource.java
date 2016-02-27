/*
 * Copyright 2013 Lyor Goldstein
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.maven.classpath.munger.util.properties;

import java.util.Collection;
import java.util.TreeSet;

/**
 * @author Lyor G.
 * @since Mar 25, 2014 9:48:31 AM
 */
public class AggregateNamedPropertySource extends AggregatePropertySource implements NamedPropertySource {
    public AggregateNamedPropertySource(NamedPropertySource ... sources) {
        super(sources);
    }

    @Override
    public Collection<String> getAvailableNames() {
        Collection<String>  names=new TreeSet<>();
        for (PropertySource src : props) {
            Collection<String>  available=((NamedPropertySource) src).getAvailableNames();
            if ((available == null) || available.isEmpty()) {
                continue;
            }
            
            names.addAll(available);
        }

        return names;
    }
}

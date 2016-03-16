
@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(value=DateTimeValueAdapter.class, type=Date.class)
})
package net.community.chest.spring.test.entities;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;


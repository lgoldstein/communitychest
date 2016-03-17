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

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

/**
 * @author lgoldstein
 */
@XmlRootElement(name="restful-data")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Entity
@Table(name="restful_data")
public class RestfulData implements Serializable, Cloneable {
    private static final long serialVersionUID = -2893908928078750418L;

    private Date    _lastModified;
    public RestfulData () {
        super();
    }

    private Long    _id;
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        _id = id;
    }

    private int    _version;
    @Version
    @Column(nullable=false)
    int getVersion () {
        return _version;
    }

    void setVersion (int version) {
        _version = version;
    }

    @XmlElement(name="last-modified")
    @XmlSchemaType(name="dateTime")
    @Column(name="lastModified",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    public Date getLastModified() {
        return _lastModified;
    }

    public void setLastModified(Date lastModified) {
        _lastModified = lastModified;
    }

    private int    _balance;
    @XmlElement(name="balance")
    @Column(name="balance",nullable=false)
    // min/max added just for facilitating validation framework tests
    @Min(Short.MIN_VALUE)
    @Max(Short.MAX_VALUE << 10)
    public int getBalance() {
        return _balance;
    }

    public void setBalance(int balance) {
        _balance = balance;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final Date    thisModified=this.getLastModified();
        return getBalance() + ((thisModified == null) ? 0 : thisModified.hashCode());
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        final Class<?>    tc=getClass(), oc=(obj == null) ? null : obj.getClass();
        if (tc != oc) {
            return false;
        }

        final RestfulData    other=(RestfulData) obj;
        if (this.getBalance() != other.getBalance()) {
            return false;
        }

        final Date    thisModified=this.getLastModified(), otherModified=other.getLastModified();
        if (thisModified == null) {
            return (otherModified == null) ? true : false;
        }

        return thisModified.equals(otherModified);
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    public RestfulData clone() {
        final RestfulData    cloneInstance;
        try {
            if ((cloneInstance=getClass().cast(super.clone())) == null) {
                throw new CloneNotSupportedException("Internal clone failed");
            }
        } catch(CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        final Date            cloneDate=cloneInstance.getLastModified();
        if (cloneDate != null) {    // do deep clone
            cloneInstance.setLastModified(new Date(cloneDate.getTime()));
        }

        return cloneInstance;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Balance = " + getBalance() + " at " + DateFormat.getDateTimeInstance().format(getLastModified());
    }
}

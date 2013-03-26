/*
 * Copyright 2013 JBoss Inc
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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.01.17 at 01:09:37 PM EST 
//


package org.s_ramp.xmlns._2010.s_ramp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Service complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Service">
 *   &lt;complexContent>
 *     &lt;extension base="{http://s-ramp.org/xmlns/2010/s-ramp}Element">
 *       &lt;sequence>
 *         &lt;element name="hasContract" type="{http://s-ramp.org/xmlns/2010/s-ramp}serviceContractTarget" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="hasInterface" type="{http://s-ramp.org/xmlns/2010/s-ramp}serviceInterfaceTarget" maxOccurs="unbounded"/>
 *         &lt;element name="hasInstance" type="{http://s-ramp.org/xmlns/2010/s-ramp}serviceInstanceTarget" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Service", propOrder = {
    "hasContract",
    "hasInterface",
    "hasInstance"
})
public class Service
    extends Element
    implements Serializable
{

    private static final long serialVersionUID = -7936448390950410315L;
    protected List<ServiceContractTarget> hasContract;
    @XmlElement(required = true)
    protected List<ServiceInterfaceTarget> hasInterface;
    protected ServiceInstanceTarget hasInstance;

    /**
     * Gets the value of the hasContract property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hasContract property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHasContract().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceContractTarget }
     * 
     * 
     */
    public List<ServiceContractTarget> getHasContract() {
        if (hasContract == null) {
            hasContract = new ArrayList<ServiceContractTarget>();
        }
        return this.hasContract;
    }

    /**
     * Gets the value of the hasInterface property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hasInterface property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHasInterface().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceInterfaceTarget }
     * 
     * 
     */
    public List<ServiceInterfaceTarget> getHasInterface() {
        if (hasInterface == null) {
            hasInterface = new ArrayList<ServiceInterfaceTarget>();
        }
        return this.hasInterface;
    }

    /**
     * Gets the value of the hasInstance property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceInstanceTarget }
     *     
     */
    public ServiceInstanceTarget getHasInstance() {
        return hasInstance;
    }

    /**
     * Sets the value of the hasInstance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceInstanceTarget }
     *     
     */
    public void setHasInstance(ServiceInstanceTarget value) {
        this.hasInstance = value;
    }

}
/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.atom.services;

import static org.jboss.resteasy.test.TestPortProvider.generateURL;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.namespace.QName;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.test.BaseResourceTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.overlord.sramp.atom.MediaType;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

import test.org.overlord.sramp.atom.TestUtils;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 * @version $Revision: 1 $
 */
public class XsdDocumentResourceTest extends BaseResourceTest {

	@Before
	public void setUp() throws Exception {
		// bring up the embedded container with the XsdDocument resource
		// deployed.
		dispatcher.getRegistry().addPerRequestResource(XsdDocumentResource.class);
	}

	/**
	 * Called to serialize an Atom entry to an XML string.
	 * 
	 * @param entry
	 * @throws JAXBException
	 * @throws PropertyException
	 */
	private String serializeAtomEntry(Entry entry) throws JAXBException, PropertyException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Entry.class, Artifact.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

		StringWriter writer = new StringWriter();
		JAXBElement<Entry> element = new JAXBElement<Entry>(new QName("http://www.w3.org/2005/Atom",
				"entry", "atom"), Entry.class, entry);

		marshaller.marshal(element, writer);
		return writer.toString();
	}

	@Test
	public void testFullPurchaseOrderXSD() throws Exception {
		// Add
		Entry entry = doAddXsd();
		URI entryId = entry.getId();
		
		// Get
		entry = doGetXsdEntry(entryId);
		
		// Get artifact content
		String content = doGetXsdContent(entryId);
		verifyXsdContent(content);

		// Feed
		Feed feed = doGetXsdFeed();
		verifyXsdFeedContains(feed, entryId);
		
		// Update
		doUpdateXsdEntry(entry);
		entry = doGetXsdEntry(entryId);
		verifyEntryUpdated(entry);
		
		// TODO implement delete functionality
//		deleteXsdEntry(entryId);
//		verifyEntryDeleted();
	}

	/**
	 * Adds an XSD to the repo by POSTing the content to /s-ramp/xsd/XsdDocument.
	 */
	private Entry doAddXsd() throws Exception {
		// Making a client call to the actual XsdDocument implementation running in
		// an embedded container.
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument"));

		// read the XsdDocument from file
		String artifactFileName = "PO.xsd";
		InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		String xmltext = TestUtils.convertStreamToString(POXsd);
		POXsd.close();

		request.header("Slug", artifactFileName);
		request.body(MediaType.APPLICATION_XML, xmltext);

		ClientResponse<Entry> response = request.post(Entry.class);

		Entry entry = response.getEntity();
		Assert.assertEquals(artifactFileName, entry.getTitle());
		Artifact artifact = entry.getAnyOtherJAXBObject(Artifact.class);
		Assert.assertEquals(Long.valueOf(2376), artifact.getXsdDocument().getContentSize());
		Assert.assertEquals(artifactFileName, artifact.getXsdDocument().getName());

		// Serializing to XML so we can check that it looks good.
		String actualXml = serializeAtomEntry(entry);
		// TODO do some XML assertions here
		System.out.println(actualXml);

		return entry;
	}

	/**
	 * GETs the Atom entry from the repository (to ensure we have the latest).
	 * @param entryId
	 * @throws Exception
	 */
	private Entry doGetXsdEntry(URI entryId) throws Exception {
		// TODO I think the entryId should be of the format urn:{uuid} and we'll need to parse it - this isn't happening right now though
		String uuid = entryId.toString();

		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument/" + uuid));
		ClientResponse<Entry> response = request.get(Entry.class);

		Entry entry = response.getEntity();
		Artifact artifact = entry.getAnyOtherJAXBObject(Artifact.class);
		Assert.assertNotNull(artifact.getXsdDocument());
		Assert.assertEquals(Long.valueOf(2376), artifact.getXsdDocument().getContentSize());

		return entry;
	}

	/**
	 * Gets the content for the artifact from the repo.
	 * @param entryId
	 * @throws Exception 
	 */
	private String doGetXsdContent(URI entryId) throws Exception {
		String uuid = entryId.toString();

		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument/" + uuid + "/media"));
		ClientResponse<String> response = request.get(String.class);

		return response.getEntity();
	}

	/**
	 * Verify that the content returned from the repo is right.
	 * @param content
	 * @throws IOException 
	 */
	private void verifyXsdContent(String content) throws IOException {
		Assert.assertNotNull(content);
		
		String artifactFileName = "PO.xsd";
		InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			String expectedContent = TestUtils.convertStreamToString(POXsd);
			Assert.assertEquals(expectedContent, content);
		} finally {
			POXsd.close();
		}
	}
	
	/**
	 * GETs a {@link Feed} of the XsdDocument artifacts.
	 * @throws Exception 
	 */
	private Feed doGetXsdFeed() throws Exception {
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument"));
		ClientResponse<Feed> response = request.get(Feed.class);
		return response.getEntity();
	}

	/**
	 * Verifies that the 
	 * @param feed
	 * @param entryId 
	 */
	private void verifyXsdFeedContains(Feed feed, URI entryId) {
		boolean hasEntryId = false;
		List<Entry> entries = feed.getEntries();
		for (Entry entry : entries) {
			if (entry.getId().equals(entryId))
				hasEntryId = true;
		}
		Assert.assertTrue("Feed did not contain entry with ID: " + entryId, hasEntryId);
	}

	/**
	 * PUTs the Atom entry back into the repository (after making some changes).
	 * @param entry
	 * @throws Exception 
	 */
	private void doUpdateXsdEntry(Entry entry) throws Exception {
		// First, make a change to the entry.
		Artifact srampArtifactWrapper = entry.getAnyOtherJAXBObject(Artifact.class);
		XsdDocument xsdDocument = srampArtifactWrapper.getXsdDocument();
		String uuid = xsdDocument.getUuid();
		xsdDocument.setDescription("** Updated description! **");
		
		entry.setAnyOtherJAXBObject(srampArtifactWrapper);
		
		// Now PUT the changed entry into the repo
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument/" + uuid));
		request.body(MediaType.APPLICATION_ATOM_XML_ENTRY, entry);
		ClientResponse<Void> response = request.put(Void.class);
		response.getEntity();
	}

	/**
	 * Verifies that the entry has been updated, by checking that the s-ramp extended
	 * Atom entry returned contains the classification and custom property set during
	 * the update phase of the test.
	 * @param entry
	 */
	private void verifyEntryUpdated(Entry entry) throws Exception {
		Artifact srampArtifactWrapper = entry.getAnyOtherJAXBObject(Artifact.class);
		XsdDocument xsdDocument = srampArtifactWrapper.getXsdDocument();
		Assert.assertEquals("** Updated description! **", xsdDocument.getDescription());
	}

}

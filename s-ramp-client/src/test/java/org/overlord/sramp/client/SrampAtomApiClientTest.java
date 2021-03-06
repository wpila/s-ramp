/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.client;

import static org.junit.Assert.fail;
import static org.overlord.sramp.common.test.resteasy.TestPortProvider.generateURL;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.atom.mappers.RdfToOntologyMapper;
import org.overlord.sramp.atom.providers.HttpResponseProvider;
import org.overlord.sramp.atom.providers.SrampAtomExceptionProvider;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.overlord.sramp.common.test.resteasy.BaseResourceTest;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.jcr.JCRRepositoryFactory;
import org.overlord.sramp.repository.jcr.modeshape.JCRRepositoryCleaner;
import org.overlord.sramp.server.atom.services.ArtifactResource;
import org.overlord.sramp.server.atom.services.BatchResource;
import org.overlord.sramp.server.atom.services.FeedResource;
import org.overlord.sramp.server.atom.services.OntologyResource;
import org.overlord.sramp.server.atom.services.QueryResource;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

/**
 * Unit test for the
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampAtomApiClientTest extends BaseResourceTest {

	@BeforeClass
	public static void setUp() throws Exception {
		// use the in-memory config for unit tests
		System.setProperty("sramp.modeshape.config.url", "classpath://" + JCRRepositoryFactory.class.getName()
				+ "/META-INF/modeshape-configs/inmemory-sramp-config.json");

		deployment.getProviderFactory().registerProvider(SrampAtomExceptionProvider.class);
		deployment.getProviderFactory().registerProvider(HttpResponseProvider.class);
		dispatcher.getRegistry().addPerRequestResource(ArtifactResource.class);
		dispatcher.getRegistry().addPerRequestResource(FeedResource.class);
		dispatcher.getRegistry().addPerRequestResource(BatchResource.class);
        dispatcher.getRegistry().addPerRequestResource(QueryResource.class);
        dispatcher.getRegistry().addPerRequestResource(OntologyResource.class);
	}

    @Before
    public void cleanRepository() {
        new JCRRepositoryCleaner().clean();
    }

	@AfterClass
	public static void cleanup() {
		PersistenceFactory.newInstance().shutdown();
	}

	/**
	 * Test method for {@link SrampAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
	 */
	@Test
	public void testUploadArtifact() throws Exception {
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
			BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
			Assert.assertNotNull(artifact);
			Assert.assertEquals(artifactFileName, artifact.getName());
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

    /**
     * Test method for {@link SrampAtomApiClient#createArtifact(BaseArtifactType)}.
     */
    @Test
    public void testCreateArtifact() throws Exception {
        ExtendedArtifactType artifact = new ExtendedArtifactType();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setExtendedType("TestArtifact");
        artifact.setName("My Test Artifact");
        artifact.setDescription("Description of my test artifact.");
        SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
        BaseArtifactType createdArtifact = client.createArtifact(artifact);
        Assert.assertNotNull(artifact);
        Assert.assertEquals("My Test Artifact", createdArtifact.getName());
        Assert.assertEquals("Description of my test artifact.", createdArtifact.getDescription());
    }

    /**
     * Test method for {@link SrampAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
     */
    @Test
    public void testExtendedDocumentArtifact() throws Exception {
        String artifactFileName = "PO.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        try {
            SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.ExtendedDocument("TestDocument"), is, artifactFileName);
            Assert.assertNotNull(artifact);
            Assert.assertEquals(artifactFileName, artifact.getName());
            Assert.assertEquals(BaseArtifactEnum.EXTENDED_DOCUMENT, artifact.getArtifactType());
            Assert.assertEquals(ExtendedDocument.class, artifact.getClass());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Test method for {@link SrampAtomApiClient#getArtifactMetaData(ArtifactType, String)}
     */
    @Test
    public void testGetArtifactMetaData() throws Exception {
        String uuid = null;
        String artifactFileName = "PO.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        try {
            SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
            Assert.assertNotNull(artifact);
            uuid = artifact.getUuid();
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Now test that we can fetch the meta-data using the artifact type and UUID
        {
            SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
            BaseArtifactType metaData = client.getArtifactMetaData(ArtifactType.XsdDocument(), uuid);
            Assert.assertNotNull(metaData);
            Assert.assertEquals(artifactFileName, metaData.getName());
        }
    }

    /**
     * Test method for {@link SrampAtomApiClient#getArtifactMetaData(String)}
     */
    @Test
    public void testGetArtifactMetaDataNoType() throws Exception {
        String uuid = null;
        String artifactFileName = "PO.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        try {
            SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
            Assert.assertNotNull(artifact);
            uuid = artifact.getUuid();
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Now test that we can fetch the meta-data using just the UUID
        {
            SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
            BaseArtifactType metaData = client.getArtifactMetaData(uuid);
            Assert.assertNotNull(metaData);
            Assert.assertEquals(artifactFileName, metaData.getName());
        }
    }


	/**
     * Test method for {@link SrampAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
     */
    @Test
    public void testUploadArtifactAndContent() throws Exception {
        String artifactFileName = "PO.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        try {
            SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
            XsdDocument xsdDocument = new XsdDocument();
            xsdDocument.setName(artifactFileName);
            xsdDocument.setUuid("my-client-side-supplied-UUID");
            BaseArtifactType artifact = client.uploadArtifact(xsdDocument, is);
            Assert.assertNotNull(artifact);
            Assert.assertEquals(artifactFileName, artifact.getName());
            Assert.assertEquals("my-client-side-supplied-UUID", artifact.getUuid());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

	/**
	 * Test method for {@link SrampAtomApiClient#getArtifactContent(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetArtifactContent() throws Exception {
		SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
		String uuid = null;

		// First, upload an artifact so we have some content to get
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
			Assert.assertNotNull(artifact);
			Assert.assertEquals(artifactFileName, artifact.getName());
			uuid = artifact.getUuid();
		} finally {
			is.close();
		}

		// Now get the content.
		InputStream content = client.getArtifactContent(ArtifactType.XsdDocument(), uuid.toString());
		try {
			Assert.assertNotNull(content);
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			String line1 = reader.readLine();
			String line2 = reader.readLine();
			Assert.assertTrue("Unexpected content found.", line1.startsWith("<?xml version=\"1.0\""));
			Assert.assertTrue("Unexpected content found.", line2.startsWith("<xsd:schema"));
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Tests updating an artifact.
	 * @throws Exception
	 */
	public void testUpdateArtifactMetaData() throws Exception {
		SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
		String uuid = null;
		XsdDocument xsdDoc = null;

		// First, upload an artifact so we have some content to update
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
			Assert.assertNotNull(artifact);
			Assert.assertEquals(artifactFileName, artifact.getName());
			uuid = artifact.getUuid();
			xsdDoc = (XsdDocument) artifact;
		} finally {
			IOUtils.closeQuietly(is);
		}

		// Now update the description
		xsdDoc.setDescription("** DESCRIPTION UPDATED **");
		client.updateArtifactMetaData(xsdDoc);

		// Now verify
		BaseArtifactType artifact = client.getArtifactMetaData(ArtifactType.XsdDocument(), uuid.toString());
		Assert.assertEquals("** DESCRIPTION UPDATED **", artifact.getDescription());
	}

	/**
	 * Tests updating an artifact.
	 * @throws Exception
	 */
	public void testUpdateArtifactContent() throws Exception {
		SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
		String uuid = null;
		XsdDocument xsdDoc = null;

		// First, upload an artifact so we have some content to update
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
			Assert.assertNotNull(artifact);
			Assert.assertEquals(artifactFileName, artifact.getName());
			uuid = artifact.getUuid();
			xsdDoc = (XsdDocument) artifact;
		} finally {
			IOUtils.closeQuietly(is);
		}

		// Now update the artifact content
		is = this.getClass().getResourceAsStream("/sample-files/xsd/PO-updated.xsd");
		try {
			client.updateArtifactContent(xsdDoc, is);
		} finally {
			IOUtils.closeQuietly(is);
		}

		// Now verify
		BaseArtifactType artifact = client.getArtifactMetaData(ArtifactType.XsdDocument(), uuid.toString());
		xsdDoc = (XsdDocument) artifact;
		Assert.assertEquals(new Long(2583), xsdDoc.getContentSize());
	}

	/**
	 * Test method for {@link SrampAtomApiClient#query(java.lang.String, int, int, java.lang.String, boolean)}.
	 */
	@Test
	public void testQuery() throws Exception {
		SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
		String uuid = null;

		// First add an artifact so we have something to search for
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
			Assert.assertNotNull(artifact);
			Assert.assertEquals(artifactFileName, artifact.getName());
			uuid = artifact.getUuid();
		} finally {
			IOUtils.closeQuietly(is);
		}

		// Now search for all XSDs
		QueryResultSet rset = client.query("/s-ramp/xsd/XsdDocument", 0, 50, "name", false);
		boolean uuidFound = false;
		for (ArtifactSummary entry : rset) {
			if (entry.getUuid().equals(uuid))
				uuidFound = true;
		}
		Assert.assertTrue("Failed to find the artifact we just added!", uuidFound);
	}

    /**
     * Test method for {@link SrampAtomApiClient#query(String, int, int, String, boolean, java.util.Collection)
     */
    @Test
    public void testQueryWithPropertyName() throws Exception {
        SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
        String uuid = null;

        // First add an artifact so we have something to search for
        String artifactFileName = "PO.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        try {
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
            Assert.assertNotNull(artifact);
            Assert.assertEquals(artifactFileName, artifact.getName());
            uuid = artifact.getUuid();

            // Set a couple of custom properties and update
            SrampModelUtils.setCustomProperty(artifact, "prop1", "foo");
            SrampModelUtils.setCustomProperty(artifact, "prop2", "bar");
            SrampModelUtils.setCustomProperty(artifact, "prop3", "baz");
            client.updateArtifactMetaData(artifact);
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Now search for the artifact and request one of the custom
        // properties be returned in the result set.
        Set<String> propertyNames = new HashSet<String>();
        propertyNames.add("prop1");
        propertyNames.add("prop2");
        QueryResultSet rset = client.query("/s-ramp/xsd/XsdDocument[@uuid='"+uuid+"']", 0, 50, "name", false, propertyNames);
        Assert.assertEquals("Expected a single artifact returned.", 1, rset.size());
        ArtifactSummary summary = rset.get(0);
        Assert.assertEquals("foo", summary.getCustomPropertyValue("prop1"));
        Assert.assertEquals("bar", summary.getCustomPropertyValue("prop2"));
        Assert.assertNull("I didn't ask for 'prop3' to be returned!", summary.getCustomPropertyValue("prop3"));
    }

    /**
     * Test method for {@link SrampAtomApiClient#buildQuery(String)
     */
    @Test
    public void testBuildQuery() throws Exception {
        SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
        String uuid = null;

        // First add an artifact so we have something to search for
        String artifactFileName = "PO.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        try {
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
            Assert.assertNotNull(artifact);
            Assert.assertEquals(artifactFileName, artifact.getName());
            uuid = artifact.getUuid();
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Now search for the XSD by its UUID
        QueryResultSet rset = client.buildQuery("/s-ramp/xsd/XsdDocument[@uuid = ?]").parameter(uuid)
                .count(1).query();
        Assert.assertTrue("Failed to find the artifact we just added!", rset.size() == 1);

        // Do a couple of date-based queries here
        rset = client.buildQuery("/s-ramp[@lastModifiedTimestamp < ?]")
                .parameter(new Date(System.currentTimeMillis() + 86400000L))
                .count(1).query();
        Assert.assertTrue("Failed to find an artifact by lastModifiedTimestamp!", rset.size() == 1);
        rset = client.buildQuery("/s-ramp[@lastModifiedTimestamp > ?]")
                .parameter(new Date(System.currentTimeMillis() + 86400000L))
                .count(1).query();
        Assert.assertTrue("Found an artifact by lastModifiedTimestamp, but should *not* have!", rset.size() == 0);

        // Now by DateTime
        Calendar endOfToday = Calendar.getInstance();
        endOfToday.set(Calendar.HOUR_OF_DAY, 0);
        endOfToday.set(Calendar.MINUTE, 0);
        endOfToday.set(Calendar.SECOND, 0);
        endOfToday.set(Calendar.MILLISECOND, 0);
        endOfToday.add(Calendar.DAY_OF_YEAR, 1);
        rset = client.buildQuery("/s-ramp[@lastModifiedTimestamp < ?]")
                .parameter(endOfToday)
                .count(1).query();
        Assert.assertTrue("Failed to find an artifact by lastModifiedTimestamp!", rset.size() == 1);
        rset = client.buildQuery("/s-ramp[@lastModifiedTimestamp > ?]")
                .parameter(endOfToday)
                .count(1).query();
        Assert.assertTrue("Found an artifact by lastModifiedTimestamp, but should *not* have!", rset.size() == 0);
    }

    /**
     * Test method for {@link SrampAtomApiClient#buildQuery(String)
     */
    @Test
    public void testResultSetAttributes() throws Exception {
        SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
        for (int i = 0; i < 20; i++) {
            addXmlDoc();
        }
        QueryResultSet rs = client.buildQuery("/s-ramp/core").count(2).startIndex(5).query();
        Assert.assertEquals(20, rs.getTotalResults());
        Assert.assertEquals(2, rs.getItemsPerPage());
        Assert.assertEquals(5, rs.getStartIndex());
    }

	/**
	 * Test method for {@link SrampAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
	 */
	@Test
	public void testQueryError() throws Exception {
		SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
		try {
			QueryResultSet rset = client.query("12345", 0, 20, "name", false);
			fail("Expected a remote exception from the s-ramp server, but got: " + rset);
		} catch (SrampAtomException e) {
			Assert.assertEquals("Invalid artifact set (step 2).", e.getMessage());
		}
	}

	/**
	 * Test method for {@link SrampAtomApiClient#uploadBatch(SrampArchive)}.
	 */
	@Test
	public void testArchiveUpload() throws Exception {
		// First, create an s-ramp archive
		SrampArchive archive = null;
		InputStream is1 = null;
		InputStream is2 = null;
		try {
			archive = new SrampArchive();

			String artifactFileName = "PO.xsd";
			is1 = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
			BaseArtifactType metaData = new XsdDocument();
			metaData.setName("PO.xsd");
			metaData.setVersion("1.1");
			metaData.setDescription("This is a test description (XSD).");
			archive.addEntry("schemas/PO.xsd", metaData, is1);

			artifactFileName = "PO.xml";
			is2 = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
			metaData = new XsdDocument();
			metaData.setName("PO.xml");
			metaData.setVersion("1.2");
			metaData.setDescription("This is a test description (XML).");
			archive.addEntry("core/PO.xml", metaData, is2);
		} catch (Exception e) {
			SrampArchive.closeQuietly(archive);
			throw e;
		} finally {
			IOUtils.closeQuietly(is1);
			IOUtils.closeQuietly(is2);
		}

		try {
			// Now use the s-ramp atom api client to upload the s-ramp archive
			SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
			Map<String, ?> results = client.uploadBatch(archive);
			Assert.assertEquals(2, results.size());
			Assert.assertTrue(results.keySet().contains("schemas/PO.xsd"));
			Assert.assertTrue(results.keySet().contains("core/PO.xml"));

			XsdDocument xsdDoc = (XsdDocument) results.get("schemas/PO.xsd");
			Assert.assertNotNull(xsdDoc);
			Assert.assertEquals("PO.xsd", xsdDoc.getName());
			Assert.assertEquals("1.1", xsdDoc.getVersion());

			XmlDocument xmlDoc = (XmlDocument) results.get("core/PO.xml");
			Assert.assertNotNull(xmlDoc);
			Assert.assertEquals("PO.xml", xmlDoc.getName());
			Assert.assertEquals("1.2", xmlDoc.getVersion());
		} finally {
			SrampArchive.closeQuietly(archive);
		}
	}

    /**
     * Test method for {@link SrampAtomApiClient#uploadBatch(SrampArchive)}.
     */
    @Test
    public void testArchiveUpload_Empty() throws Exception {
        // First, create an s-ramp archive
        SrampArchive archive = null;
        try {
            archive = new SrampArchive();
        } catch (Exception e) {
            SrampArchive.closeQuietly(archive);
            throw e;
        } finally {
        }

        try {
            // Now use the s-ramp atom api client to upload the s-ramp archive
            SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
            Map<String, ?> results = client.uploadBatch(archive);
            Assert.assertTrue(results.isEmpty());
        } finally {
            SrampArchive.closeQuietly(archive);
        }
    }

    /**
     * Test method for {@link SrampAtomApiClient#uploadBatch(SrampArchive)}.
     */
    @Test
    public void testArchiveUpload_AtomOnly() throws Exception {
        // First, create an s-ramp archive
        SrampArchive archive = null;
        try {
            archive = new SrampArchive();
            ExtendedArtifactType nonDocArtifact = new ExtendedArtifactType();
            nonDocArtifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            nonDocArtifact.setExtendedType("TestArtifact");
            nonDocArtifact.setName("My Test Artifact");

            archive.addEntry("myLogicalArtifact", nonDocArtifact, null);
        } catch (Exception e) {
            SrampArchive.closeQuietly(archive);
            throw e;
        } finally {
        }

        try {
            // Now use the s-ramp atom api client to upload the s-ramp archive
            SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
            Map<String, ?> results = client.uploadBatch(archive);
            Assert.assertFalse(results.isEmpty());
            Assert.assertEquals(1, results.size());

            QueryResultSet resultSet = client.buildQuery("/s-ramp/ext").query();
            Assert.assertNotNull(resultSet);
            Assert.assertEquals(1, resultSet.getTotalResults());

            resultSet = client.buildQuery("/s-ramp/ext/TestArtifact").query();
            Assert.assertNotNull(resultSet);
            Assert.assertEquals(1, resultSet.getTotalResults());

            resultSet = client.buildQuery("/s-ramp/ext/TestArtifact[@name = 'My Test Artifact']").query();
            Assert.assertNotNull(resultSet);
            Assert.assertEquals(1, resultSet.getTotalResults());
        } finally {
            SrampArchive.closeQuietly(archive);
        }
    }

	/**
	 * Test method for {@link SrampAtomApiClient#uploadBatch(SrampArchive)}.
	 */
	@Test
	public void testArchiveUploadWithError() throws Exception {
		// First, create an s-ramp archive
		SrampArchive archive = null;
		InputStream is1 = null;
		InputStream is2 = null;
		try {
			archive = new SrampArchive();

			String artifactFileName = "PO.xsd";
			is1 = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
			BaseArtifactType metaData = new XsdDocument();
			metaData.setName("PO.xsd");
			metaData.setVersion("1.1");
			metaData.setDescription("This is a test description (XSD).");
			archive.addEntry("schemas/PO.xsd", metaData, is1);

			artifactFileName = "PO.xml";
			metaData = new XsdDocument();
			metaData.setName("PO.xml");
			metaData.setVersion("1.2");
			metaData.setDescription("This is a test description (XML).");
			archive.addEntry("core/PO.xml", metaData, null);
		} catch (Exception e) {
			SrampArchive.closeQuietly(archive);
			throw e;
		} finally {
			IOUtils.closeQuietly(is1);
			IOUtils.closeQuietly(is2);
		}

		try {
			// Now use the s-ramp atom api client to upload the s-ramp archive
			SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
			Map<String, ?> results = client.uploadBatch(archive);
			Assert.assertEquals(2, results.size());
			Assert.assertTrue(results.keySet().contains("schemas/PO.xsd"));
			Assert.assertTrue(results.keySet().contains("core/PO.xml"));

			XsdDocument xsdDoc = (XsdDocument) results.get("schemas/PO.xsd");
			Assert.assertNotNull(xsdDoc);
			Assert.assertEquals("PO.xsd", xsdDoc.getName());
			Assert.assertEquals("1.1", xsdDoc.getVersion());

			Exception xmlError = (Exception) results.get("core/PO.xml");
			Assert.assertNotNull(xmlError);
		} finally {
			SrampArchive.closeQuietly(archive);
		}
	}

	/**
	 * Adds an XML document.
	 * @throws Exception
	 */
	protected void addXmlDoc() throws Exception {
        String artifactFileName = "PO.xml";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        try {
            SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
            client.uploadArtifact(ArtifactType.XmlDocument(), is, artifactFileName);
        } finally {
            IOUtils.closeQuietly(is);
        }
	}

	/**
     * Test method for {@link SrampAtomApiClient#uploadOntology(InputStream)}.
     */
    @Test
    public void testUploadOntology() throws Exception {
        String ontologyFileName = "colors.owl.xml";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/ontologies/" + ontologyFileName);
        Assert.assertNotNull(is);
        try {
            SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
            RDF rdf = client.uploadOntology(is);
            Assert.assertNotNull(rdf);
            SrampOntology ontology = RdfToOntologyMapper.rdf2ontology(rdf);
            Assert.assertNotNull(ontology);
            Assert.assertEquals("http://www.example.org/colors.owl", ontology.getBase());
            Assert.assertNotNull(ontology.getUuid());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Test method for {@link SrampAtomApiClient#getOntologies()}.
     */
    @Test
    public void testGetOntologies() throws Exception {
        SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
        List<OntologySummary> ontologies = client.getOntologies();
        Assert.assertNotNull(ontologies);
        Assert.assertTrue(ontologies.isEmpty());
        // Re-use another test to upload an ontology
        testUploadOntology();

        // Now go again with data there.
        ontologies = client.getOntologies();
        Assert.assertNotNull(ontologies);
        Assert.assertFalse(ontologies.isEmpty());
        Assert.assertEquals(1, ontologies.size());
        OntologySummary ontologySummary = ontologies.get(0);
        Assert.assertEquals("http://www.example.org/colors.owl", ontologySummary.getBase());
        Assert.assertEquals("Colors ontology", ontologySummary.getComment());
        Assert.assertEquals("Colors", ontologySummary.getId());
        Assert.assertEquals("Colors", ontologySummary.getLabel());
        Assert.assertNotNull(ontologySummary.getUuid());
    }

    /**
     * Test method for {@link SrampAtomApiClient#getOntology(String)}.
     */
    @Test
    public void testGetOntology() throws Exception {
        SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
        RDF rdf = null;
        try {
            rdf = client.getOntology("INVALID_UUID");
        } catch (Exception e) {
            Assert.assertEquals("No ontology found with UUID: INVALID_UUID", e.getMessage());
        }
        Assert.assertNull(rdf);

        // Re-use another test to upload an ontology
        testUploadOntology();

        // Now go again with data there.
        List<OntologySummary> ontologies = client.getOntologies();
        Assert.assertNotNull(ontologies);
        Assert.assertFalse(ontologies.isEmpty());
        Assert.assertEquals(1, ontologies.size());
        OntologySummary ontologySummary = ontologies.get(0);
        String uuid = ontologySummary.getUuid();

        rdf = client.getOntology(uuid);
        Assert.assertNotNull(rdf);
        SrampOntology ontology = RdfToOntologyMapper.rdf2ontology(rdf);
        Assert.assertNotNull(ontology);
        Assert.assertEquals("http://www.example.org/colors.owl", ontology.getBase());
        Assert.assertNotNull(ontology.getUuid());
    }

}

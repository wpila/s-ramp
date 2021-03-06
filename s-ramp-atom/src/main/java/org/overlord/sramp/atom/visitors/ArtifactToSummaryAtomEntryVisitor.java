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
package org.overlord.sramp.atom.visitors;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.jboss.resteasy.plugins.providers.atom.Category;
import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Link;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Artifact;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.visitors.ArtifactVisitorAdapter;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;

/**
 * Visitor used to convert an artifact to an Atom entry.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactToSummaryAtomEntryVisitor extends ArtifactVisitorAdapter {

    protected String baseUrl = "";
	protected Entry atomEntry;
	protected Exception failure;
	protected Set<String> propertyNames;

	/**
	 * Constructor.
	 * @param baseUrl
	 */
	public ArtifactToSummaryAtomEntryVisitor(String baseUrl) {
	    this.baseUrl = baseUrl;
	}

	/**
	 * Constructor.
	 * @param baseUrl
	 * @param propNames
	 */
	public ArtifactToSummaryAtomEntryVisitor(String baseUrl, Set<String> propNames) {
	    this.baseUrl = baseUrl;
		this.propertyNames = propNames;
	}

	/**
	 * Called to reset the created entry and failure {@link Exception}.  Call this
	 * between visits of different artifacts - useful if you want to re-use the
	 * visitor in a loop.
	 */
	public void reset() {
		this.atomEntry = null;
		this.failure = null;
	}

	/**
	 * @return the atomEntry
	 */
	public Entry getAtomEntry() throws Exception {
		if (this.failure != null)
			throw this.failure;
		return atomEntry;
	}

	/**
	 * Sets the Atom entry.
	 * @param entry an Atom {@link Entry}
	 */
	protected void setAtomEntry(Entry entry) {
		this.atomEntry = entry;
	}

	/**
	 * Creates the base Atom Entry, doing the stuff that's common to all types of artifacts.
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitorAdapter#visitBase(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType)
	 */
	@SuppressWarnings("unchecked")
    @Override
	protected void visitBase(BaseArtifactType artifact) {
		try {
			ArtifactType artifactType = ArtifactType.valueOf(artifact);
			Entry entry = new Entry();
			if (artifact.getUuid() != null)
				entry.setId(new URI(artifact.getUuid()));
			if (artifact.getLastModifiedTimestamp() != null)
				entry.setUpdated(artifact.getLastModifiedTimestamp().toGregorianCalendar().getTime());
			if (artifact.getName() != null)
				entry.setTitle(artifact.getName());
			if (artifact.getCreatedTimestamp() != null)
				entry.setPublished(artifact.getCreatedTimestamp().toGregorianCalendar().getTime());
			if (artifact.getCreatedBy() != null)
				entry.getAuthors().add(new Person(artifact.getCreatedBy()));
			if (artifact.getDescription() != null)
				entry.setSummary(artifact.getDescription());
			entry.getExtensionAttributes().put(SrampConstants.SRAMP_DERIVED_QNAME, String.valueOf(artifactType.isDerived()));

			String atomLink = baseUrl + "/s-ramp/"
					+ artifactType.getModel() + "/"
					+ artifactType.getType() + "/" + artifact.getUuid();
			String mediaLink = atomLink + "/media";

			if (SrampModelUtils.isDocumentArtifact(artifact)) {
			    // Original content can be accessed at /s-ramp/{model}/{artifact-type}/{uid}/media
	            ArtifactContentTypeVisitor ctVisitor = new ArtifactContentTypeVisitor();
	            ArtifactVisitorHelper.visitArtifact(ctVisitor, artifact);
	            Content content = new Content();
	            content.setType(ctVisitor.getContentType());
	            content.setSrc(new URI(mediaLink));
	            entry.setContent(content);

	            // Alternate can be accessed at /s-ramp/{model}/{artifact-type}/{uid}/media
	            // Only for Document style artifacts.
	            Link linkToAlternate = new Link();
                linkToAlternate.setType(ctVisitor.getContentType());
                linkToAlternate.setRel("alternate");
                linkToAlternate.setHref(new URI(mediaLink));
                entry.getLinks().add(linkToAlternate);
			}

			// Self can be accessed at /s-ramp/{model}/{artifact-type}/{uid}
			Link linkToSelf = new Link();
			linkToSelf.setType(MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
			linkToSelf.setRel("self");
			linkToSelf.setHref(new URI(atomLink));
			entry.getLinks().add(linkToSelf);

			// Link to edit-media can be accessed at /s-ramp/{model}/{artifact-type}/{uid}/edit-media
			Link linkToEditMedia = new Link();
			linkToEditMedia.setType(MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
			linkToEditMedia.setRel("edit-media");
			linkToEditMedia.setHref(new URI(mediaLink));
			entry.getLinks().add(linkToEditMedia);

			// Link to edit can be accessed at /s-ramp/{model}/{artifact-type}/{uid}
			Link linkToEdit = new Link();
			linkToEdit.setType(MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
			linkToEdit.setRel("edit");
			linkToEdit.setHref(new URI(atomLink));
			entry.getLinks().add(linkToEdit);

			// Type category
			Category typeCat = new Category();
			typeCat.setTerm(artifactType.getType());
			typeCat.setLabel(artifactType.getLabel());
			typeCat.setScheme(new URI("x-s-ramp:2010:type"));
			entry.getCategories().add(typeCat);

			// Model category
			Category modelCat = new Category();
			modelCat.setTerm(artifactType.getModel());
			modelCat.setLabel(artifactType.getLabel());
			modelCat.setScheme(new URI("x-s-ramp:2010:model"));
			entry.getCategories().add(modelCat);

			setAtomEntry(entry);

			if (includeArtifact()) {
				Artifact artifactWrapper = new Artifact();
				BaseArtifactType includedArtifact = createIncludedArtifact(artifact);
				Method method = Artifact.class.getMethod("set" + includedArtifact.getClass().getSimpleName(), includedArtifact.getClass());
				method.invoke(artifactWrapper, includedArtifact);
				entry.setAnyOtherJAXBObject(artifactWrapper);
			}
		} catch (Exception e) {
			this.failure = e;
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
	 */
	@SuppressWarnings("unchecked")
    @Override
	public void visit(ExtendedArtifactType artifact) {
	    super.visit(artifact);

	    if (this.atomEntry != null) {
	        String extendedType = artifact.getExtendedType();
	        this.atomEntry.getExtensionAttributes().put(SrampConstants.SRAMP_EXTENDED_TYPE_QNAME, extendedType);
	    }
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument)
	 */
    @SuppressWarnings("unchecked")
	@Override
	public void visit(ExtendedDocument artifact) {
        super.visit(artifact);

        if (this.atomEntry != null) {
            String extendedType = artifact.getExtendedType();
            this.atomEntry.getExtensionAttributes().put(SrampConstants.SRAMP_EXTENDED_TYPE_QNAME, extendedType);
        }
	}

	/**
	 * Returns true if we should include the Artifact wrapper in the Entry.  For the summary
	 * {@link Entry} we would include the artifact wrapper only if we're returning some
	 * additional custom properties.
	 */
	protected boolean includeArtifact() {
		return propertyNames != null && propertyNames.size() > 0;
	}

	/**
	 * Creates the artifact that should be included in the returned Entry.
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	protected BaseArtifactType createIncludedArtifact(BaseArtifactType artifact) throws InstantiationException, IllegalAccessException {
		if (includeArtifact()) {
			BaseArtifactType includedArtifact = artifact.getClass().newInstance();
			List<Property> properties = artifact.getProperty();
			for (Property prop : properties) {
				if (this.propertyNames.contains(prop.getPropertyName())) {
					SrampModelUtils.setCustomProperty(includedArtifact, prop.getPropertyName(), prop.getPropertyValue());
				}
			}
			return includedArtifact;
		} else {
			return null;
		}
	}

}

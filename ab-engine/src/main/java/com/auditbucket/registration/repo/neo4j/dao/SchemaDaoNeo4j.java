package com.auditbucket.registration.repo.neo4j.dao;

import com.auditbucket.dao.SchemaDao;
import com.auditbucket.engine.repo.neo4j.ConceptTypeRepo;
import com.auditbucket.engine.repo.neo4j.DocumentTypeRepo;
import com.auditbucket.engine.repo.neo4j.model.ConceptNode;
import com.auditbucket.engine.repo.neo4j.model.DocumentTypeNode;
import com.auditbucket.registration.bean.TagInputBean;
import com.auditbucket.registration.model.Company;
import com.auditbucket.registration.model.Fortress;
import com.auditbucket.registration.model.Relationship;
import com.auditbucket.track.bean.ConceptInputBean;
import com.auditbucket.track.model.Concept;
import com.auditbucket.track.model.DocumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Maintains company specific Schema deets
 * User: mike
 * Date: 3/04/14
 * Time: 7:30 AM
 * To change this template use File | Settings | File Templates.
 */
@Repository
public class SchemaDaoNeo4j implements SchemaDao {
    @Autowired
    DocumentTypeRepo documentTypeRepo;

    @Autowired
    ConceptTypeRepo conceptTypeRepo;

    @Autowired
    Neo4jTemplate template;

    private Logger logger = LoggerFactory.getLogger(SchemaDaoNeo4j.class);

    @Override
    public boolean registerTagIndex(Company company, String indexName) {
        if (isSystemIndex(indexName))
            return true;

        if (!tagExists(company, indexName)) {

            String cypher = "merge (tag:TagLabel { name:{name}, companyKey:{key}}) " +
                    "with tag " +
                    "match (c:ABCompany) where id(c) = {cid} " +
                    "merge (c)<-[:TAG_INDEX]-(tag) " +
                    "return tag";
            Map<String, Object> params = new HashMap<>();
            params.put("name", indexName);
            params.put("key", parseTagIndex(company, indexName));
            params.put("cid", company.getId());

            template.query(cypher, params);


        }
        return true;
    }

    public void getDocumentTypes(Company company) {

    }

    /**
     * Tracks the DocumentTypes used by a Fortress that can be used to find MetaHeader objects
     *
     * @param fortress        fortress generating
     * @param docName         name of the Label
     * @param createIfMissing if not found will create
     * @return the node
     */
    public DocumentType findDocumentType(Fortress fortress, String docName, Boolean createIfMissing) {
        DocumentType docResult = documentExists(fortress, docName);
        if (docResult == null && createIfMissing) {
            docResult = new DocumentTypeNode(fortress, docName);
            String cypher = "merge (docType:_DocType :DocType{code:{code}, name:{name}, companyKey:{key}}) " +
                    "with docType " +
                    "match (f:Fortress) where id(f) = {fId} " +
                    "merge (f)<-[:FORTRESS_DOC]-(docType) " +
                    "return docType";

            Map<String, Object> params = new HashMap<>();
            params.put("code", docResult.getCode());
            params.put("name", docResult.getName());
            params.put("key", docResult.getCompanyKey());
            params.put("fId", fortress.getId());

            template.query(cypher, params);
            docResult = documentExists(fortress, docName);

        }
        return docResult;

    }

    @Override
    public Collection<DocumentType> getFortressDocumentsInUse(Fortress fortress) {
        return documentTypeRepo.getFortressDocumentsInUse(fortress.getId());
    }

    @Override
    public Collection<DocumentType> getCompanyDocumentsInUse(Company company) {
        return documentTypeRepo.getCompanyDocumentsInUse(company.getId());
    }


    @Cacheable(value = "companyDocType", unless = "#result == null")
    private DocumentType documentExists(Fortress fortress, String docName) {
        return documentTypeRepo.findFortressDocType(fortress.getId(), DocumentTypeNode.parse(docName));
    }

    @Cacheable(value = "companySchemaTag", unless = "#result == false")
    private boolean tagExists(Company company, String indexName) {
        //logger.info("Looking for co{}, {}", company.getId(), parseTagIndex(company, docName));
        Object o = documentTypeRepo.findCompanyTag(company.getId(), parseTagIndex(company, indexName));
        return (o != null);
        //return documentTypeRepo.findBySchemaPropertyValue("companyKey", parseTagIndex(company, docName)) != null;
    }

    /**
     * Make sure a unique index exists for the tag
     * Being a schema alteration function this is synchronised to avoid concurrent modifications
     *
     * @param company   who owns the tags
     * @param tagInputs collection to process
     */
    public synchronized void ensureUniqueIndexes(Company company, Iterable<TagInputBean> tagInputs, Collection<String> added) {

        for (TagInputBean tagInput : tagInputs) {
            if (tagInput != null) {
                String index = tagInput.getIndex();
                if (!added.contains(index)) {
                    //if (index != null && !tagExists(company, index)) { // This check causes deadlocks in TagEP ?
                    ensureIndex(company, tagInput);
                    //}
                    added.add(tagInput.getIndex());
                }
                if (!tagInput.getTargets().isEmpty()) {
                    for (String key : tagInput.getTargets().keySet()) {
                        if (key != null)
                            ensureUniqueIndexes(company, tagInput.getTargets().get(key), added);
                    }
                }
            } else
                logger.debug("Why is this null?");

        }

    }

    @Transactional
    private void ensureIndex(Company company, TagInputBean tagInput) {
        // _Tag is a special label that can be used to find all tags so we have to allow it to handle duplicates
        if (tagInput.isDefault() || isSystemIndex(tagInput.getIndex()))
            return;
        String index = tagInput.getIndex();

        template.query("create constraint on (t:`" + index + "`) assert t.key is unique", null);
        logger.info("Creating constraint on [{}]", tagInput.getIndex());

    }

    @Async
    public void ensureSystemIndexes(Company company, String suffix) {
        // Performance issue with constraints?
        logger.debug("Creating System Indexes...");
        template.query("create constraint on (t:Country) assert t.key is unique", null);
        template.query("create constraint on (t:City) assert t.key is unique", null);
    }

    @Override
    public void registerConcepts(Company company, Map<DocumentType, Collection<ConceptInputBean>> conceptInput) {
        logger.trace("Registering concepts");
        Set<DocumentType> documentTypes = conceptInput.keySet();
        Collection<String> docs = new ArrayList<>(documentTypes.size());
        for (String doc : docs) {
            docs.add(doc);
        }

        for (DocumentType docType : conceptInput.keySet()) {
            logger.trace("Looking for existing concepts {}", docType.getName());
            DocumentTypeNode documentTypeNode = (DocumentTypeNode) docType;
            template.fetch(documentTypeNode.getConcepts());

            Set<Concept> concepts = documentTypeNode.getConcepts();
            logger.trace("[{}] - Found {} existing concepts", documentTypeNode.getName(), concepts.size());
            for (ConceptInputBean concept : conceptInput.get(docType)) {
                //logger.debug("Looking to create [{}]", concept.getName());
                ConceptNode existingConcept = conceptTypeRepo.findBySchemaPropertyValue("name", concept.getName());

                boolean save = false;
                for (String relationship : concept.getRelationships()) {
                    if (existingConcept == null) {
                        logger.debug("No existing concept found for [{}]. Creating it", relationship);
                        existingConcept = new ConceptNode(concept.getName(), relationship);
                        save = true;
                    } else {
                        logger.trace("Found an existing concept {}", existingConcept);
                        template.fetch(existingConcept.getRelationships());
                        Relationship existingR = existingConcept.hasRelationship(relationship);
                        if (existingR == null) {
                            save = true;
                            existingConcept.addRelationship(relationship);
                            logger.debug("Creating {} concept for{}", relationship, existingConcept);
                        }
                    }
                    if (save) {
                        documentTypeNode.add(existingConcept);
                        logger.debug("Creating concept {}", existingConcept);
                    }
                }
            }
            logger.trace("About to register {} concepts", concepts.size());
            documentTypeRepo.save(documentTypeNode);
            logger.trace("{} Concepts registered", concepts.size());
        }
    }

    @Override
    public Set<DocumentType> findConcepts(Company company, Collection<String> docNames, boolean withRelationships) {
        Set<DocumentType> documents;
        if (docNames == null)
            documents = documentTypeRepo.findAllDocuments(company);
        else
            documents = documentTypeRepo.findDocuments(company, docNames);

        for (DocumentType document : documents) {
            template.fetch(document.getConcepts());
            if (withRelationships) {
                for (Concept concept : document.getConcepts()) {
                    template.fetch(concept);
                    template.fetch(concept.getRelationships());
                }
            }
        }
        return documents;
    }

    private boolean isSystemIndex(String index) {
        return (index.equals("Country") || index.equals("City"));
    }

    private String parseTagIndex(Company company, String indexName) {
        return company.getId() + ".t." + indexName.toLowerCase().replaceAll("\\s", "");
    }


}

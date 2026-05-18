package main.java.com.ecm.library.service;

import com.ecm.library.entity.AttributeDefinition;
import com.ecm.library.entity.AttributeGroup;
import com.ecm.library.entity.DocumentClass;

import java.util.List;

/**
 * Provides read access to the document class schema definitions.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Resolving a document class by its code string</li>
 *   <li>Returning the ordered attribute groups for a document class</li>
 *   <li>Returning all attribute definitions across all groups for a class</li>
 * </ul>
 *
 * <p>These operations are the foundation of DocumentAttributeService's
 * validation logic and the admin console's schema browser.</p>
 *
 * <p>Not responsible for: creating or modifying document class definitions
 * (admin console operations added in Phase 5), document instance retrieval,
 * or permission checks.</p>
 */
public interface DocumentClassService {

    /**
     * Returns the document class for the given code.
     *
     * @throws com.ecm.common.exception.DocumentClassNotFoundException if not found
     */
    DocumentClass getDocumentClassByCode(String code);

    /**
     * Returns the document class only if it is currently active.
     * Used during document ingestion — inactive classes cannot accept new documents.
     *
     * @throws com.ecm.common.exception.DocumentClassNotFoundException if not found or inactive
     */
    DocumentClass getActiveDocumentClassByCode(String code);

    /** Returns all active document classes, ordered by display name. */
    List<DocumentClass> getAllActiveDocumentClasses();

    /**
     * Returns the attribute groups for a document class in display order.
     * Each group includes its attribute definitions (eagerly loaded).
     */
    List<AttributeGroup> getAttributeGroupsForDocumentClass(String classCode);

    /**
     * Returns a flat list of all attribute definitions across all attribute
     * groups assigned to the given document class.
     *
     * <p>Used by DocumentAttributeService to validate incoming attribute maps
     * without having to iterate nested group/definition structures at the call site.</p>
     */
    List<AttributeDefinition> getAllAttributeDefinitionsForDocumentClass(String classCode);
}

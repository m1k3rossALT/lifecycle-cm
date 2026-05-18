package main.java.com.ecm.library.service.impl;

import com.ecm.common.exception.DocumentClassNotFoundException;
import com.ecm.library.entity.AttributeDefinition;
import com.ecm.library.entity.AttributeGroup;
import com.ecm.library.entity.DocumentClass;
import com.ecm.library.entity.DocumentClassAttributeGroup;
import com.ecm.library.repository.DocumentClassRepository;
import com.ecm.library.service.DocumentClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of DocumentClassService.
 *
 * <p>All methods are read-only transactions. Document class definitions are
 * seeded once and rarely change — they are good candidates for caching in a
 * later phase if query volume warrants it. The @Transactional(readOnly = true)
 * annotation signals to the JPA provider that no dirty-checking is needed,
 * which reduces overhead on every request.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DocumentClassServiceImpl implements DocumentClassService {

    private final DocumentClassRepository documentClassRepository;

    @Override
    public DocumentClass getDocumentClassByCode(String code) {
        return documentClassRepository.findByCode(code)
                .orElseThrow(() -> new DocumentClassNotFoundException(code, MDC.get("correlationId")));
    }

    @Override
    public DocumentClass getActiveDocumentClassByCode(String code) {
        return documentClassRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new DocumentClassNotFoundException(code, MDC.get("correlationId")));
    }

    @Override
    public List<DocumentClass> getAllActiveDocumentClasses() {
        return documentClassRepository.findAllByActiveTrue()
                .stream()
                .sorted((a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName()))
                .toList();
    }

    @Override
    public List<AttributeGroup> getAttributeGroupsForDocumentClass(String classCode) {
        DocumentClass documentClass = getDocumentClassByCode(classCode);

        // attributeGroupMappings is loaded eagerly on DocumentClass and ordered
        // by displayOrder. Extract the AttributeGroup from each mapping.
        return documentClass.getAttributeGroupMappings()
                .stream()
                .map(DocumentClassAttributeGroup::getAttributeGroup)
                .toList();
    }

    @Override
    public List<AttributeDefinition> getAllAttributeDefinitionsForDocumentClass(String classCode) {
        // Flatten: for each group in display order, collect all definitions
        // in the group's own display order. The result is a single ordered
        // list across all groups — the complete schema for the document class.
        return getAttributeGroupsForDocumentClass(classCode)
                .stream()
                .flatMap(group -> group.getAttributeDefinitions().stream())
                .toList();
    }
}

package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.LinkDto;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.CodeCategory;
import com.solidstategroup.diagnosisview.model.codes.CodeExternalStandard;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import com.solidstategroup.diagnosisview.model.codes.LogoRule;
import com.solidstategroup.diagnosisview.model.codes.enums.CodeSourceTypes;
import com.solidstategroup.diagnosisview.model.codes.enums.CriteriaType;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
import com.solidstategroup.diagnosisview.model.codes.enums.Institution;
import com.solidstategroup.diagnosisview.repository.CategoryRepository;
import com.solidstategroup.diagnosisview.repository.CodeCategoryRepository;
import com.solidstategroup.diagnosisview.repository.CodeExternalStandardRepository;
import com.solidstategroup.diagnosisview.repository.CodeRepository;
import com.solidstategroup.diagnosisview.repository.ExternalStandardRepository;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.repository.LookupRepository;
import com.solidstategroup.diagnosisview.repository.LookupTypeRepository;
import com.solidstategroup.diagnosisview.service.CodeService;
import com.solidstategroup.diagnosisview.service.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * {@inheritDoc}.
 */
@Slf4j
@Service
public class CodeServiceImpl implements CodeService {

    private static final String DV_CODE = "dv_";
    private static final String DV_CODE_TEMPLATE = "dv_%s";
    private static final String CODE_SEQ = "code_seq";
    private static final String LINK_SEQ = "link_seq";
    private static final String CODE_CATEGORY_SEQ = "code_category_seq";
    private static final String CODE_EXTERNAL_STANDARD = "code_external_standard_seq";

    private final CodeRepository codeRepository;
    private final CategoryRepository categoryRepository;
    private final CodeCategoryRepository codeCategoryRepository;
    private final CodeExternalStandardRepository codeExternalStandardRepository;
    private final ExternalStandardRepository externalStandardRepository;
    private final LookupTypeRepository lookupTypeRepository;
    private final LookupRepository lookupRepository;
    private final LinkRepository linkRepository;
    private final LinkService linkService;

    // Temporary hack to get ids for codes and links.
    private EntityManager entityManager;

    public CodeServiceImpl(CodeRepository codeRepository,
                           CategoryRepository categoryRepository,
                           CodeCategoryRepository codeCategoryRepository,
                           CodeExternalStandardRepository codeExternalStandardRepository,
                           ExternalStandardRepository externalStandardRepository,
                           LinkRepository linkRepository,
                           LinkService linkService,
                           LookupTypeRepository lookupTypeRepository,
                           LookupRepository lookupRepository,
                           EntityManager entityManager) {

        this.codeRepository = codeRepository;
        this.categoryRepository = categoryRepository;
        this.codeCategoryRepository = codeCategoryRepository;
        this.codeExternalStandardRepository = codeExternalStandardRepository;
        this.externalStandardRepository = externalStandardRepository;
        this.linkRepository = linkRepository;
        this.linkService = linkService;
        this.lookupTypeRepository = lookupTypeRepository;
        this.lookupRepository = lookupRepository;
        this.entityManager = entityManager;
    }

    private static boolean shouldDisplayLink(Optional<String> linkMapping, Link link) {

        return linkMapping.isPresent() | !link.getTransformationsOnly();
    }

    private static boolean shouldBeDeleted(Code code) {
        return code.isRemovedExternally() || code.isHideFromPatients();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable("getAllCategories")
    public List<CategoryDto> getAllCategories() {

        return categoryRepository
                .findAll()
                .stream()
                .map(category -> new CategoryDto(
                        category.getId(),
                        category.getNumber(),
                        category.getIcd10Description(),
                        category.getFriendlyDescription(),
                        category.isHidden()))
                .collect(toList());
    }

    public List<Code> getAll() {

        return codeRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable("getAllCodes")
    public List<CodeDto> getAll(Institution institution) {

        return codeRepository
                .findAll()
                .parallelStream()
                .map(code -> CodeDto
                        .builder()
                        .code(code.getCode())
                        .links(buildLinkDtos(code, institution))
                        .categories(buildCategories(code))
                        .deleted(shouldBeDeleted(code))
                        .friendlyName(code.getPatientFriendlyName())
                        .build())
                .sorted(Comparator.comparing(CodeDto::getFriendlyName,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Code get(String code) {
        // should not happen, this to allow deleting and editing duplicates
        List<Code> existing = codeRepository.findByCode(code);
        if (CollectionUtils.isEmpty(existing)) {
            return null;
        }

        Code result = existing.get(0);
        result.getLinks()
                .forEach(l -> {
                    l.setLogoRule(null);
                    l.setMappingLinks(null);
                });

        return result;
    }

    public Code getByInstitution(String code, Institution institution) {

        Code result = codeRepository.findOneByCode(code);

        if (result == null) {

            return null;
        }

        result
                .getLinks()
                .forEach(l -> {
                    String originalLink = l.getLink();
                    Optional<String> transformed = buildLink(l.getMappingLinks(), institution);
                    l.setDisplayLink(shouldDisplayLink(transformed, l));
                    l.setLink(transformed.orElse(l.getLink()));
                    l.setOriginalLink(originalLink);
                    l.setLogoRule(null);
                    l.setMappingLinks(null);
                });

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
    public void delete(Code code) {

        if (code.getCode() == null) {

            throw new BadRequestException("code not set");
        }

        Code currentCode = codeRepository.findOneByCode(code.getCode());

        if (currentCode == null) {

            throw new BadRequestException("code not found");
        }

        if (currentCode.getSourceType() != CodeSourceTypes.DIAGNOSISVIEW) {

            throw new BadRequestException("can only delete diagnosis view created codes");
        }

        linkRepository.deleteByCode(currentCode);
        codeRepository.delete(currentCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
    public Code save(Code code) {

        return codeRepository.save(code);
    }

    private long selectIdFrom(String sequence) {
        String sql = "SELECT nextval('" + sequence + "')";
        return ((BigInteger) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
    public Code upsert(Code code, boolean fromSync) throws Exception {

        if (code == null) {
            throw new Exception("Missing code object");
        }

        if (StringUtils.isEmpty(code.getCode())) {
            throw new Exception("Missing code for Diagnosis Code");
        }

        // If the code is from dv web, then we append dv_ to the code so its unique.
        if (!fromSync) {

            String codeName = code.getCode();
            if (!code.getCode().toLowerCase().startsWith(DV_CODE)) {
                codeName = format(DV_CODE_TEMPLATE, code.getCode());
            }
            code.setCode(codeName);

            List<Code> existing = codeRepository.findByCode(codeName);
            if (!CollectionUtils.isEmpty(existing)) {
                if (code.getId() == null) {
                    throw new EntityExistsException("A duplicate diagnosis code exists in the database. " +
                            "Please amend and try again");
                } else {
                    existing.forEach(c -> {
                        if (!(code.getId().equals(c.getId()))) {
                            throw new EntityExistsException("A duplicate diagnosis code exists in the database. " +
                                    "Please amend and try again");
                        }
                    });
                }
            }

            if (code.getSourceType() == null) {
                code.setSourceType(CodeSourceTypes.DIAGNOSISVIEW);
            }

            if (code.getId() == null) {

                code.setId(selectIdFrom(CODE_SEQ));
            }

            code.setLinks(code
                    .getLinks()
                    .stream()
                    .peek(l -> {
                        if (l.getId() == null) {
                            l.setId(selectIdFrom(LINK_SEQ));
                        }
                    })
                    .collect(toSet()));

            if (code.getExternalStandards() != null) {

                code.setExternalStandards(code
                        .getExternalStandards()
                        .stream()
                        .peek(es -> {
                            if (es.getId() == null) {
                                es.setId(selectIdFrom(CODE_EXTERNAL_STANDARD));
                            }
                        })
                        .collect(toSet()));
            }

            if (code.getCodeCategories() != null) {

                code.setCodeCategories(code
                        .getCodeCategories()
                        .stream()
                        .peek(cc -> {
                            if (cc.getId() == null) {
                                cc.setId(selectIdFrom(CODE_CATEGORY_SEQ));
                            }
                        })
                        .collect(toSet()));
            }
        }

        if (upsertNotRequired(code)) {
            return null;
        }

        // The following are all items that wont be sent with the web creation
        if (fromSync) {

            saveAdditionalSyncObjects(code);

            if (code.getSourceType() == null) {
                code.setSourceType(CodeSourceTypes.PATIENTVIEW);
            }
        }

        Set<Link> links = code.getLinks();
        Set<CodeCategory> codeCategories = code.getCodeCategories();
        Set<CodeExternalStandard> externalStandards = code.getExternalStandards();

        // Remove code related fields, as PV already provides ids if the
        // objects have not already been saved to the repository jpa will thrown
        // an exception because it can't the the non-existent ids in the db.
        code.setLinks(new HashSet<>());
        code.setCodeCategories(new HashSet<>());
        code.setExternalStandards(new HashSet<>());
        code.setLastUpdate(new Date());

        code.setCodeCategories(codeCategories
                .stream()
                .peek(cc -> cc.setCode(code))
                .map(codeCategoryRepository::save)
                .collect(toSet()));

        code.setExternalStandards(externalStandards
                .stream()
                .peek(es -> es.setCode(code))
                .map(codeExternalStandardRepository::save)
                .collect(toSet()));

        code.setLinks(links
                .stream()
                .peek(l -> l.setCode(code))
                .map(linkService::upsert)
                .collect(toSet()));

        return codeRepository.save(code);
    }

    @Override
    public Code updateCode(Code code) {

        long start = System.currentTimeMillis();
        log.debug(" processing CODE {}", code.getCode());

        try {

            if (upsertNotRequired(code)) {
                log.info(" Update not required CODE {}", code.getCode());
                return code;
            }

            saveAdditionalSyncObjects(code);

            if (code.getSourceType() == null) {
                code.setSourceType(CodeSourceTypes.PATIENTVIEW);
            }

            Set<Link> links = code.getLinks();
            Set<CodeCategory> codeCategories = code.getCodeCategories();
            Set<CodeExternalStandard> externalStandards = code.getExternalStandards();

            // Remove code related fields, as PV already provides ids if the
            // objects have not already been saved to the repository jpa will thrown
            // an exception because it can't the the non-existent ids in the db.
            code.setLinks(new HashSet<>());
            code.setCodeCategories(new HashSet<>());
            code.setExternalStandards(new HashSet<>());

            final Code persistedCode = codeRepository.save(code);
            code.setCreated(persistedCode.getCreated());
            code.setLastUpdate(persistedCode.getLastUpdate());

            code.setCodeCategories(codeCategories
                    .stream()
                    .peek(cc -> cc.setCode(code))
                    .map(codeCategoryRepository::save)
                    .collect(toSet()));

            code.setExternalStandards(externalStandards
                    .stream()
                    .peek(es -> es.setCode(code))
                    .map(codeExternalStandardRepository::save)
                    .collect(toSet()));

            code.setLinks(links
                    .stream()
                    .peek(l -> l.setCode(code))
                    .map(linkService::upsert)
                    .collect(toSet()));

            codeRepository.save(code);

        } catch (Exception e) {
            log.error("Update failed for code: " + code.getCode() + " with error: " + e.getMessage());
        }
        long stop = System.currentTimeMillis();
        log.debug("  DONE code update {} timing {}", code.getCode(), (stop - start));
        return code;
    }

    private boolean upsertNotRequired(Code code) {

        if (code.getId() == null) {
            return false;
        }

        Code currentCode = codeRepository.findById(code.getId())
                .orElse(null);

        //If there is a code, or it has been updated, update
        return !(currentCode == null ||
                !currentCode.getCode().equals(code.getCode()) ||
                !currentCode.getPatientFriendlyName().equals(code.getPatientFriendlyName()) ||
                !currentCode.getDescription().equals(code.getDescription()) ||
                !currentCode.getFullDescription().equals(code.getFullDescription()) ||
                currentCode.getLinks().size() != code.getLinks().size() ||
                currentCode.getExternalStandards().size() != code.getExternalStandards().size() ||
                currentCode.getCodeCategories().size() != code.getCodeCategories().size() ||
                (currentCode.getLastUpdate() != null && currentCode.getLastUpdate().before(code.getLastUpdate())));
    }

    private void saveAdditionalSyncObjects(Code code) {

        lookupTypeRepository.save(code.getStandardType().getLookupType());
        lookupRepository.save(code.getStandardType());

        lookupTypeRepository.save(code.getCodeType().getLookupType());
        lookupRepository.save(code.getCodeType());

        code.getCodeCategories()
                .forEach(cc -> categoryRepository.save(cc.getCategory()));

        code.getExternalStandards()
                .forEach(es -> externalStandardRepository.save(es.getExternalStandard()));
    }

    private Set<LinkDto> buildLinkDtos(Code code, Institution institution) {

        return code
                .getLinks()
                .stream()
                .map(link -> {
                    String originalLink = link.getLink();
                    Optional<String> linkMapping = buildLink(link.getMappingLinks(), institution);

                    return new LinkDto(
                            link.getId(),
                            link.getLinkType(),
                            link.getDifficultyLevel(),
                            linkMapping.orElse(link.getLink()),
                            originalLink,
                            link.getDisplayOrder(),
                            shouldDisplayLink(linkMapping, link),
                            link.getName(), link.getFreeLink(),
                            link.getTransformationsOnly());
                })
                .collect(toSet());
    }

    private DifficultyLevel buildDifficultyLevel(Link link) {

        LogoRule rule = link.getLogoRule();

        if (rule == null) {
            return link.getDifficultyLevel();
        }

        DifficultyLevel override = rule.getOverrideDifficultyLevel();

        if (override == null) {
            return link.getDifficultyLevel();
        }

        return override;
    }

    private Optional<String> buildLink(
            Set<LinkRuleMapping> linkRuleMapping,
            Institution institution) {

        return linkRuleMapping
                .stream()
                .filter(r -> r.getCriteriaType() == CriteriaType.INSTITUTION)
                .filter(r -> Institution.valueOf(r.getCriteria()) == institution)
                .findFirst()
                .map(LinkRuleMapping::getReplacementLink);
    }

    private Set<CategoryDto> buildCategories(Code code) {

        return code
                .getCodeCategories()
                .stream()
                .map(cc ->
                        new CategoryDto(
                                cc.getCategory().getId(),
                                cc.getCategory().getNumber(),
                                cc.getCategory().getIcd10Description(),
                                cc.getCategory().getFriendlyDescription(),
                                cc.getCategory().isHidden()))
                .collect(toSet());
    }
}

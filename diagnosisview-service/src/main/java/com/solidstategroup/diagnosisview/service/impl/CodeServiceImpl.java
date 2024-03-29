package com.solidstategroup.diagnosisview.service.impl;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.solidstategroup.diagnosisview.exceptions.BadRequestException;
import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.LinkDto;
import com.solidstategroup.diagnosisview.model.Tag;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.CodeCategory;
import com.solidstategroup.diagnosisview.model.codes.CodeExternalStandard;
import com.solidstategroup.diagnosisview.model.codes.Institution;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import com.solidstategroup.diagnosisview.model.codes.LogoRule;
import com.solidstategroup.diagnosisview.model.codes.enums.CodeSourceTypes;
import com.solidstategroup.diagnosisview.model.codes.enums.CriteriaType;
import com.solidstategroup.diagnosisview.model.codes.enums.DifficultyLevel;
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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
  private static final String PAYWALLED_KEY = "paywalled";
  private static final String LINK_KEY = "link";

  private final CodeRepository codeRepository;
  private final CategoryRepository categoryRepository;
  private final CodeCategoryRepository codeCategoryRepository;
  private final CodeExternalStandardRepository codeExternalStandardRepository;
  private final ExternalStandardRepository externalStandardRepository;
  private final LookupTypeRepository lookupTypeRepository;
  private final LookupRepository lookupRepository;
  private final LinkRepository linkRepository;
  private final LinkService linkService;

  private final SynonymsService synonymsService;

  private final InstitutionService institutionService;

  private final TagsService tagsService;

  // Temporary hack to get ids for codes and links.
  private EntityManager entityManager;

  public CodeServiceImpl(CodeRepository codeRepository,
      CategoryRepository categoryRepository,
      CodeCategoryRepository codeCategoryRepository,
      CodeExternalStandardRepository codeExternalStandardRepository,
      ExternalStandardRepository externalStandardRepository,
      LinkRepository linkRepository,
      LinkService linkService,
      SynonymsService synonymsService,
      LookupTypeRepository lookupTypeRepository,
      LookupRepository lookupRepository,
      InstitutionService institutionService,
      TagsService tagsService,
      EntityManager entityManager) {

    this.codeRepository = codeRepository;
    this.categoryRepository = categoryRepository;
    this.codeCategoryRepository = codeCategoryRepository;
    this.codeExternalStandardRepository = codeExternalStandardRepository;
    this.externalStandardRepository = externalStandardRepository;
    this.linkRepository = linkRepository;
    this.linkService = linkService;
    this.synonymsService = synonymsService;
    this.lookupTypeRepository = lookupTypeRepository;
    this.lookupRepository = lookupRepository;
    this.institutionService = institutionService;
    this.tagsService = tagsService;
    this.entityManager = entityManager;
  }

  private static boolean shouldDisplayLink(String linkMapping, Link link) {

    return !StringUtils.isEmpty(linkMapping) | !link.getTransformationsOnly();
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
  public List<CodeDto> getAll(String institutionCode) throws ResourceNotFoundException {

    final Institution institution =
        StringUtils.isEmpty(institutionCode) ? null
            : institutionService.getInstitution(institutionCode);

    return codeRepository
        .findAll()
        .parallelStream()
        .map(code -> CodeDto
            .builder()
            .code(code.getCode())
            .links(buildLinkDtos(code, institution))
            .categories(buildCategories(code))
            .removedExternally(code.isRemovedExternally())
            .hideFromPatients(code.isHideFromPatients())
            .deleted(shouldBeDeleted(code))
            .friendlyName(code.getPatientFriendlyName())
            .tags(code.getTags())
            .created(code.getCreated())
            .build())
        .sorted(Comparator.comparing(CodeDto::getFriendlyName,
            Comparator.nullsFirst(Comparator.naturalOrder())))
        .collect(toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Cacheable("getAllCodes")
  public List<CodeDto> getAllActive(String institutionCode) throws ResourceNotFoundException {

    final Institution institution =
        StringUtils.isEmpty(institutionCode) ? null
            : institutionService.getInstitution(institutionCode);

    return codeRepository
        .findAllActive()
        .parallelStream()
        .map(code -> CodeDto
            .builder()
            .code(code.getCode())
            .links(buildLinkDtos(code, institution))
            .categories(buildCategories(code))
            .deleted(shouldBeDeleted(code))
            .friendlyName(code.getPatientFriendlyName())
            .tags(code.getTags())
            .build())
        .sorted(Comparator.comparing(CodeDto::getFriendlyName,
            Comparator.nullsFirst(Comparator.naturalOrder())))
        .collect(toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable("getAllCodes")
  public List<CodeDto> getAllActiveByCodes(List<String> codes, String institutionCode)
      throws ResourceNotFoundException {

    final Institution institution =
        StringUtils.isEmpty(institutionCode) ? null
            : institutionService.getInstitution(institutionCode);

    return codeRepository
        .findAllActiveByCodes(codes)
        .parallelStream()
        .map(code -> CodeDto
            .builder()
            .code(code.getCode())
            .links(buildLinkDtos(code, institution))
            .categories(buildCategories(code))
            .deleted(shouldBeDeleted(code))
            .friendlyName(code.getPatientFriendlyName())
            .tags(code.getTags())
            .build())
        .sorted(Comparator.comparing(CodeDto::getFriendlyName,
            Comparator.nullsFirst(Comparator.naturalOrder())))
        .collect(toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<CodeDto> getCodesBySynonyms(String searchTerm, String institutionCode)
      throws ResourceNotFoundException {
    List<CodeDto> filteredCodes = new ArrayList<>();
    Set<Code> foundCodes = new HashSet<>();
    if (StringUtils.isEmpty(searchTerm) || searchTerm.length() < 3) {
      return filteredCodes;
    }

    // search DV by synonyms
    List<Code> dvCodes = codeRepository.findBySynonym("%".concat(searchTerm).concat("%"));
    if (!CollectionUtils.isEmpty(dvCodes)) {
      foundCodes.addAll(dvCodes);
    }

    // search diagnosis by synonyms, and get icd10 codes back
    Set<String> externalStandardCodes = synonymsService.searchSynonyms(searchTerm);

    for (String code : externalStandardCodes) {

      // extract only first part before dot(.) as DV only stores
      // first part of the codes eg I25.5 we only need I25 part
      String[] codeArr = code.split("\\.");
      String codePrefix = codeArr[0];

      // do wildcard search eg search on I25%
      List<Code> wildcardCodes = codeRepository.findByExternalStandards(codePrefix.concat("%"));

      if (!CollectionUtils.isEmpty(wildcardCodes)) {
        // if more then 1 found do exact match search
        if (wildcardCodes.size() > 1) {
          List<Code> fullMatchCodes = codeRepository.findByExternalStandards(code);
          // found codes add to main list, otherwise
          // default to wildcard search
          if (!CollectionUtils.isEmpty(fullMatchCodes)) {
            foundCodes.addAll(fullMatchCodes);
          } else {
            foundCodes.addAll(wildcardCodes);
          }
        } else {
          foundCodes.addAll(wildcardCodes);
        }
      }
    }

    final Institution institution =
        StringUtils.isEmpty(institutionCode) ? null
            : institutionService.getInstitution(institutionCode);

    // convert Codes to DTO and return
    if (!CollectionUtils.isEmpty(foundCodes)) {
      return foundCodes.parallelStream()
          .map(code -> CodeDto
              .builder()
              .code(code.getCode())
              .links(buildLinkDtos(code, institution))
              .categories(buildCategories(code))
              .deleted(shouldBeDeleted(code))
              .friendlyName(code.getPatientFriendlyName())
              .tags(code.getTags())
              .build())
          .sorted(Comparator.comparing(CodeDto::getFriendlyName,
              Comparator.nullsFirst(Comparator.naturalOrder())))
          .collect(toList());
    }

    return filteredCodes;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<CodeDto> searchCodes(String searchTerm, String institutionCode) {

    Set<Code> foundCodes = new HashSet<>();
    if (StringUtils.isEmpty(searchTerm) || searchTerm.length() < 3) {
      log.warn("Search term is empty returning empty result");
      return Collections.EMPTY_LIST;
    }

    // search diagnosis by name, code or synonym
    List<Code> dvCodes = codeRepository.searchAllCodes("%".concat(searchTerm).concat("%"));
    if (!CollectionUtils.isEmpty(dvCodes)) {
      foundCodes.addAll(dvCodes);
    }

    // search diagnosis by synonyms, and get icd10 codes back
    Set<String> externalStandardCodes = synonymsService.searchSynonyms(searchTerm);

    for (String code : externalStandardCodes) {

      // extract only first part before dot(.) as DV only stores
      // first part of the codes eg I25.5 we only need I25 part
      String[] codeArr = code.split("\\.");
      String codePrefix = codeArr[0];

      // do wildcard search eg search on I25%
      List<Code> wildcardCodes = codeRepository.findByExternalStandards(codePrefix.concat("%"));

      if (!CollectionUtils.isEmpty(wildcardCodes)) {
        // if more then 1 found do exact match search
        if (wildcardCodes.size() > 1) {
          List<Code> fullMatchCodes = codeRepository.findByExternalStandards(code);
          // found codes add to main list, otherwise
          // default to wildcard search
          if (!CollectionUtils.isEmpty(fullMatchCodes)) {
            foundCodes.addAll(fullMatchCodes);
          } else {
            foundCodes.addAll(wildcardCodes);
          }
        } else {
          foundCodes.addAll(wildcardCodes);
        }
      }
    }

    // TODO: do we need to filter on institution using for admins only
//        final Institution institution =
//                StringUtils.isEmpty(institutionCode) ? null : institutionService.getInstitution(institutionCode);

    // convert Codes to DTO and return
    if (!CollectionUtils.isEmpty(foundCodes)) {
      return foundCodes.parallelStream()
          .map(code -> CodeDto
              .builder()
              .code(code.getCode())
              .deleted(shouldBeDeleted(code))
              .removedExternally(code.isRemovedExternally())
              .hideFromPatients(code.isHideFromPatients())
              .friendlyName(code.getPatientFriendlyName())
              .tags(code.getTags())
              .created(code.getCreated())
              .build())
          .sorted(Comparator.comparing(CodeDto::getFriendlyName,
              Comparator.nullsFirst(Comparator.naturalOrder())))
          .collect(toList());
    }

    return Collections.EMPTY_LIST;
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

  @Override
  public Code getByInstitution(String code, String institutionCode)
      throws ResourceNotFoundException {

    Code result = codeRepository.findOneByCode(code);

    if (result == null) {
      return null;
    }

    final Institution institution =
        StringUtils.isEmpty(institutionCode) ? null
            : institutionService.getInstitution(institutionCode);

    result.getLinks().forEach(l -> {
      String originalLink = l.getLink();
      Map<String, String> linkMapping = buildLink(l.getMappingLinks(), institution);
      l.setDisplayLink(shouldDisplayLink(linkMapping.get(LINK_KEY), l));
      l.setLink(linkMapping.get(LINK_KEY) != null ? linkMapping.get(LINK_KEY) : originalLink);
      l.setPaywalled(
          linkMapping.get(PAYWALLED_KEY) != null ? linkMapping.get(PAYWALLED_KEY) : null);
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
  public Code add(Code code) throws Exception {

    if (code == null) {
      throw new Exception("Missing code object");
    }

    if (StringUtils.isEmpty(code.getCode())) {
      throw new Exception("Missing code for Diagnosis Code");
    }

    if (code.getId() != null) {
      throw new Exception("Code id present, did you mean to update?");
    }

    // If the code is from dv web, then we append dv_ to the code so its unique.

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

    // validate Links order for the Code
    linkService.checkLinksOrder(code.getLinks(), code);

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

    Set<Link> links = code.getLinks();
    Set<CodeCategory> codeCategories = code.getCodeCategories();
    Set<CodeExternalStandard> externalStandards = code.getExternalStandards();
    Set<Tag> tags = code.getTags();

    // Remove code related fields, as PV already provides ids if the
    // objects have not already been saved to the repository jpa will thrown
    // an exception because it can't the the non-existent ids in the db.
    code.setLinks(new HashSet<>());
    code.setCodeCategories(new HashSet<>());
    code.setExternalStandards(new HashSet<>());
    code.setTags(new HashSet<>());
    code.setLastUpdate(new Date());

    codeRepository.save(code);

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
        .map(l -> linkService.upsert(l, links, false))
        .collect(toSet()));

    code.setTags(buildTags(tags));

    return codeRepository.save(code);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
  public Code update(Code code) throws Exception {

    if (code == null) {
      throw new Exception("Missing code object");
    }

    if (StringUtils.isEmpty(code.getCode())) {
      throw new Exception("Missing code for Diagnosis Code");
    }

    if (StringUtils.isEmpty(code.getId())) {
      throw new Exception("Missing id for Diagnosis Code");
    }

    // validate unique if changed
    List<Code> existing = codeRepository.findByCode(code.getCode());
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

    // TODO:  check if source type is sent
    if (code.getSourceType() == null) {
      code.setSourceType(CodeSourceTypes.DIAGNOSISVIEW);
    }

    // validate Links order for the Code
    linkService.checkLinksOrder(code.getLinks(), code);

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

    Set<Link> links = code.getLinks();
    Set<CodeCategory> codeCategories = code.getCodeCategories();
    Set<CodeExternalStandard> externalStandards = code.getExternalStandards();
    Set<Tag> tags = code.getTags();

    // Remove code related fields, as PV already provides ids if the
    // objects have not already been saved to the repository jpa will thrown
    // an exception because it can't the the non-existent ids in the db.
    code.setLinks(new HashSet<>());
    code.setCodeCategories(new HashSet<>());
    code.setExternalStandards(new HashSet<>());
    code.setTags(new HashSet<>());
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
        .map(l -> linkService.upsert(l, links, false))
        .collect(toSet()));

    code.setTags(buildTags(tags));

    return codeRepository.save(code);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
  public Code updateCodeSynonyms(Code code) throws Exception {
    Code existingCode = codeRepository.findById(code.getId())
        .orElseThrow(() -> new BadRequestException("The Code not exist within DiagnosisView."));

    existingCode.setSynonyms(new HashSet<>());
    existingCode.setSynonyms(code.getSynonyms());

    return codeRepository.save(existingCode);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  @CacheEvict(value = {"getAllCodes", "getAllCategories"}, allEntries = true)
  public Code updateCodeFromSync(Code code) {

    long start = System.currentTimeMillis();
    log.info(" processing CODE id {} {} ", code.getId(), code.getCode());

    Code existingCode = codeRepository.findById(code.getId())
        .orElse(null);

    try {

      if (upsertNotRequired(code, existingCode)) {
        log.info(" Update not required CODE {}", code.getCode());
        return code;
      }

      saveAdditionalSyncObjects(code);

//            if (code.getSourceType() == null) {
//                code.setSourceType(CodeSourceTypes.PATIENTVIEW);
//            }

      Set<Link> links = code.getLinks();
      if (existingCode != null) {
        // add any links that were added to the code via DV
        if (!CollectionUtils.isEmpty(existingCode.getLinks())) {
          links.addAll(existingCode.getLinks());
        }

        code.setSynonyms(existingCode.getSynonyms());
      }
      Set<CodeCategory> codeCategories = code.getCodeCategories();
      Set<CodeExternalStandard> externalStandards = code.getExternalStandards();

      // Remove code related fields, as PV already provides ids if the
      // objects have not already been saved to the repository jpa will thrown
      // an exception because it can't the the non-existent ids in the db.
      code.setLinks(new HashSet<>());
      code.setCodeCategories(new HashSet<>());
      code.setExternalStandards(new HashSet<>());
      code.setLastUpdate(new Date());

      // if new code we need to persist if first
      if (existingCode == null) {
        log.info(" no existing code, creating {} {} ", code.getId(), code.getCode());
        final Code persistedCode = codeRepository.save(code);
        code.setCreated(new Date());
      }

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
          .filter(l -> !StringUtils.isEmpty(l.getLink()))
          .peek(l -> l.setCode(code))
          .map(l -> linkService.upsert(l, links, true))
          .collect(toSet()));

      codeRepository.save(code);

    } catch (Exception e) {
      log.error("Update failed for code: {} ", code.getCode(), e);

    }
    long stop = System.currentTimeMillis();
    log.info("  DONE code update {} timing {}", code.getCode(), (stop - start));
    return code;
  }

  private boolean upsertNotRequired(Code code, final Code currentCode) {

    if (code.getId() == null) {
      return false;
    }

    // If there is a code, or it has been updated, update
    return !(currentCode == null ||
        !currentCode.getCode().equals(code.getCode()) ||
        (!StringUtils.isEmpty(currentCode.getPatientFriendlyName()) &&
            !currentCode.getPatientFriendlyName().equals(code.getPatientFriendlyName())) ||
        (!StringUtils.isEmpty(currentCode.getDescription()) &&
            !currentCode.getDescription().equals(code.getDescription())) ||
        (!StringUtils.isEmpty(currentCode.getFullDescription()) &&
            !currentCode.getFullDescription().equals(code.getFullDescription())) ||
        currentCode.getLinks().size() != code.getLinks().size() ||
        currentCode.getExternalStandards().size() != code.getExternalStandards().size() ||
        currentCode.getCodeCategories().size() != code.getCodeCategories().size() ||
        (currentCode.getLastUpdate() != null && currentCode.getLastUpdate()
            .before(code.getLastUpdate())));
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

  /**
   * Build links based on the given Institution.
   * <p>
   * Institution is used to transform url for subscribed users.
   *
   * @param code
   * @param institution
   * @return
   */
  private Set<LinkDto> buildLinkDtos(Code code, Institution institution) {

    return code
        .getLinks()
        .stream()
        .map(link -> {
          String originalLink = link.getLink();
          // check if we have link rules for transformation for
          // given institution also if it's paywalled link
          Map<String, String> linkMapping = buildLink(link.getMappingLinks(), institution);

          return new LinkDto(
              link.getId(),
              link.getLinkType(),
              link.getDifficultyLevel(),
              linkMapping.get(LINK_KEY) != null ? linkMapping.get(LINK_KEY) : originalLink,
              originalLink,
              link.getDisplayOrder(),
              shouldDisplayLink(linkMapping.get(LINK_KEY), link),
              link.getName(), link.getFreeLink(),
              link.getTransformationsOnly(),
              linkMapping.get(PAYWALLED_KEY) != null ?
                  LinkDto.PaywalledType.valueOf(linkMapping.get(PAYWALLED_KEY)) : null);
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

  /**
   * From given set of link mapping find the one that matches given Institution criteria
   *
   * @param linkRuleMapping
   * @param institution
   * @return a transformed link url
   */
  private Map<String, String> buildLink(Set<LinkRuleMapping> linkRuleMapping,
      Institution institution) {

    Map<String, String> data = new HashMap<>();

    for (LinkRuleMapping r : linkRuleMapping) {
      // if we have an institution against rule means its
      // link is Paywalled eg transformable
      if (r.getCriteriaType() != null && r.getCriteriaType() == CriteriaType.INSTITUTION) {

        // default to locked and original url
        data.put(PAYWALLED_KEY, LinkDto.PaywalledType.LOCKED.name());
        data.put(LINK_KEY, r.getLink().getLink());

        // now check for transformation based on Institution
        if (institution != null && r.getCriteria().equals(institution.getCode())) {
          // matches given institution set to unlocked
          data.put(PAYWALLED_KEY, LinkDto.PaywalledType.UNLOCKED.name());
          data.put(LINK_KEY, r.getReplacementLink());
          break;
        }
      }
    }
    return data;
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

  /**
   * From given set of Tag values find Tag lookup and build set.
   *
   * @param tags a set of Tag codes
   * @return a set of Tag
   */
  private Set<Tag> buildTags(Set<Tag> tags) {
    Set<Tag> tagSet = new HashSet<>();
    if (!CollectionUtils.isEmpty(tags)) {
      for (Tag t : tags) {
        try {
          tagSet.add(tagsService.getTag(t.getCode()));
        } catch (ResourceNotFoundException e) {
          log.error("Could not find Tag for value {}", t.getCode());
        }
      }
    }
    return tagSet;
  }
}

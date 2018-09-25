package com.solidstategroup.diagnosisview.service.impl;


import com.solidstategroup.diagnosisview.model.CategoryDto;
import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.LinkDto;
import com.solidstategroup.diagnosisview.model.codes.Category;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.repository.CategoryRepository;
import com.solidstategroup.diagnosisview.repository.CodeRepository;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.service.CodeService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.annotations.Cacheable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;


/**
 * {@inheritDoc}.
 */
@Log
@Service
public class CodeServiceImpl implements CodeService {

    @Autowired
    private CodeRepository codeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Override
    @Cacheable("getAllCategories")
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        ArrayList<CategoryDto> categoryDtos = new ArrayList<>();

        categories.stream().forEach(category -> categoryDtos
                .add(new CategoryDto(category.getNumber(),
                        category.getIcd10Description(),
                        category.getFriendlyDescription(),
                        category.isHidden())));

        return categoryDtos;
    }


    @Override
    @Cacheable("getAllCodes")
    public List<CodeDto> getAllCodes() {
        List<CodeDto> codeDtoList = new ArrayList<>();
        List<Code> codeList = codeRepository.findAll();
        codeList.parallelStream().forEach(code -> {
            CodeDto codeDto = new CodeDto();
            codeDto.setCode(code.getCode());
            ArrayList<LinkDto> linkDtos = new ArrayList<>();
            ArrayList<CategoryDto> categoryDtos = new ArrayList<>();

            code.getLinks().stream().forEach(link -> linkDtos
                    .add(new LinkDto(link.getLinkType(), link.getDifficultyLevel(),
                            link.getLink(), link.getFreeLink())));

            codeDto.setLinks(new HashSet<>(linkDtos));

            code.getCodeCategories().stream().forEach(codeCategory -> categoryDtos
                    .add(new CategoryDto(codeCategory.getCategory().getNumber(),
                            codeCategory.getCategory().getIcd10Description(),
                            codeCategory.getCategory().getFriendlyDescription(),
                            codeCategory.getCategory().isHidden())));

            codeDto.setCategories(new HashSet<>(categoryDtos));


            if (code.isRemovedExternally() || code.isHideFromPatients()) {
                codeDto.setDeleted(true);
            } else {
                codeDto.setDeleted(false);
            }


            codeDto.setFriendlyName(code.getPatientFriendlyName());

            if (codeDto != null) {
                codeDtoList.add(codeDto);
            }
        });
        codeDtoList.removeAll(Collections.singleton(null));

        codeDtoList.sort(Comparator.comparing(CodeDto::getFriendlyName,
                Comparator.nullsFirst(Comparator.naturalOrder())));

        return codeDtoList;
    }

    @Override
    public Code getCode(String code) {
        return codeRepository.findOneByCode(code);
    }

    @Override
    public Link getLink(Long id) {
        return linkRepository.findOne(id);
    }

    @Override
    public Link saveLink(Link link) {
        Link existingLink = linkRepository.findOne(link.getId());
        //Currently you can only update certain fields
        existingLink.setDifficultyLevel(link.getDifficultyLevel());
        existingLink.setFreeLink(link.getFreeLink());

        return linkRepository.save(existingLink);
    }

    @Override
    public void delete(Code code) {
        codeRepository.delete(code);
    }

    @Override
    public Code save(Code code) {
        return codeRepository.save(code);
    }
}

package com.solidstategroup.diagnosisview.service.impl;


import com.solidstategroup.diagnosisview.model.CodeDto;
import com.solidstategroup.diagnosisview.model.LinkDto;
import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.repository.CodeRepository;
import com.solidstategroup.diagnosisview.service.CodeService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.annotations.Cacheable;

import java.util.ArrayList;
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

    @Override
    @Cacheable("getAllCodes")
    public List<CodeDto> getAllCodes() {
        List<CodeDto> codeDtoList = new ArrayList<>();
        List<Code> codeList = codeRepository.findAll();
        codeList.parallelStream().forEach(code -> {
            CodeDto codeDto = new CodeDto();
            codeDto.setCode(code.getCode());
            ArrayList<LinkDto> linkDtos = new ArrayList<LinkDto>();

            code.getLinks().stream().forEach(link -> linkDtos
                    .add(new LinkDto(link.getLinkType(), link.getDifficultyLevel(), link.getLink())));

            codeDto.setLinks(new HashSet<>(linkDtos));

            if (code.isRemovedExternally() || code.isHideFromPatients()) {
                codeDto.setDeleted(true);
            } else {
                codeDto.setDeleted(false);
            }


            codeDto.setFriendlyName(code.getPatientFriendlyName());

            codeDtoList.add(codeDto);
        });

        codeDtoList.sort(Comparator.comparing(CodeDto::getFriendlyName));

        return codeDtoList;
    }

    @Override
    public Code getCode(String code) {
        return codeRepository.findOneByCode(code);
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

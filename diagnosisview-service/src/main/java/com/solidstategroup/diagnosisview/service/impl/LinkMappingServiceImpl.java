package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.model.LinkMappingDto;
import com.solidstategroup.diagnosisview.model.codes.LinkMapping;
import com.solidstategroup.diagnosisview.repository.LinkMappingRepository;
import com.solidstategroup.diagnosisview.service.LinkMappingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LinkMappingServiceImpl implements LinkMappingService {

    private final LinkMappingRepository linkTransformationRepository;

    public LinkMappingServiceImpl(LinkMappingRepository linkTransformationRepository) {

        this.linkTransformationRepository = linkTransformationRepository;
    }

    @Override
    public LinkMapping addLinkTransformation(LinkMappingDto linkMappingDto) {

        LinkMapping linkMapping = LinkMapping
                .builder()
                .transform(linkMappingDto.getTransformation())
                .link(linkMappingDto.getLink())
                .institution(linkMappingDto.getInstitution())
                .build();

        return linkTransformationRepository.save(linkMapping);

    }

    @Override
    public List<LinkMapping> getLinkTransformations() {

        return linkTransformationRepository.findAll();
    }

    @Override
    public LinkMapping getLinkTransformation(String uuid) {

        return linkTransformationRepository.getOne(uuid);
    }

    @Override
    public LinkMapping updateLinkTransformation(String uuid, LinkMappingDto linkTransformation)
            throws Exception {

        LinkMapping current = linkTransformationRepository.findOne(uuid);

        if (current == null) {
            throw new Exception();
        }

        current.setInstitution(linkTransformation.getInstitution());
        current.setTransform(linkTransformation.getTransformation());
        current.setLink(linkTransformation.getLink());

        return linkTransformationRepository.save(current);
    }

    @Override
    public void deleteLinkTransformation(String uuid) {

        linkTransformationRepository.delete(uuid);
    }
}

package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.model.Master;
import com.khoilnm.ims.repository.MasterRepository;
import com.khoilnm.ims.service.MasterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
public class MasterServiceImpl implements MasterService {

    private final MasterRepository masterRepository;
    private final MessageSource messageSource;

    public MasterServiceImpl(MasterRepository masterRepository, MessageSource messageSource) {
        this.masterRepository = masterRepository;
        this.messageSource = messageSource;
    }

    /**
     * @param category
     * @param value
     * @return String
     */
    @Override
    public String findByCategoryAndValue(String category, String value) {
        Master master = masterRepository.findByCategoryAndValue(category, value)
                .orElse(null);
        return master != null ? master.getCategoryValue() : "";
    }

    /**
     * @param category
     * @return
     */
    @Override
    public int findMaxCategoryId(String category) {
        return masterRepository.findMaxCategoryId(category).isPresent()
                ? masterRepository.findMaxCategoryId(category).get() : 0;
    }

    /**
     * @param master
     */
    @Override
    public void createCategory(Master master) {
        masterRepository.save(master);
    }
}

package com.khoilnm.ims.service;

import com.khoilnm.ims.model.Master;

import java.util.Map;
import java.util.Optional;

public interface MasterService {

    String findByCategoryAndValue(String category, String value);

    int findMaxCategoryId(String category);

    void createCategory(Master master);
}

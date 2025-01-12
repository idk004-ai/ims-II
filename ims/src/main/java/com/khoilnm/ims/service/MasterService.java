package com.khoilnm.ims.service;

import com.khoilnm.ims.model.Master;

public interface MasterService {

    Master findByCategoryAndValue(String category, String value);

    Master findByCategoryAndCategoryId(String category, int roleId);

    int findMaxCategoryId(String category);

    void createCategory(Master master);

}

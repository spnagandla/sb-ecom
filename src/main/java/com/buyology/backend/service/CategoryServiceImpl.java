package com.buyology.backend.service;

import com.buyology.backend.dto.CategoryDTO;
import com.buyology.backend.dto.response.CategoryResponseDTO;
import com.buyology.backend.exception.APIException;
import com.buyology.backend.exception.ResourceNotFoundException;
import com.buyology.backend.model.Category;
import com.buyology.backend.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.buyology.backend.utils.CommonMethods.sortByAndOrderBy;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper){
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Override
    public CategoryResponseDTO getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String orderBy) {
        log.info("Fetching all categories");

        Sort sortByAndOrder  = sortByAndOrderBy(sortBy,orderBy);

        //pagination
        Pageable pageRequired = PageRequest.of(pageNumber,pageSize, sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageRequired);

        List<Category> categories = categoryPage.getContent();
        if(categories.isEmpty()) throw new APIException("No categories Found");
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category,CategoryDTO.class))
                .toList();

        CategoryResponseDTO categoryResponseDTO =new CategoryResponseDTO();
        categoryResponseDTO.setContent(categoryDTOS);
        categoryResponseDTO.setPageNumber(categoryPage.getNumber());
        categoryResponseDTO.setPageSize(categoryPage.getSize());
        categoryResponseDTO.setTotalElements(categoryPage.getTotalElements());
        categoryResponseDTO.setTotalPages(categoryPage.getTotalPages());
        categoryResponseDTO.setLastPage(categoryPage.isLast());
        return categoryResponseDTO;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO,Category.class);
        log.info("Request to create category: {}", category);

        Category exsistingCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if(exsistingCategory!= null) throw new APIException("Category with name " + category.getCategoryName() + " already exists!!");
        Category savedCategory = categoryRepository.save(category);

        log.info("Created category with id: {}", savedCategory.getCategoryId());
        return modelMapper.map(savedCategory,CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","categroyId",categoryId));
        categoryRepository.deleteById(categoryId);
        log.info("Category with id:{} deleted successfully", categoryId);
        return modelMapper.map(existingCategory,CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryRequest) {

        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","categroyId",categoryId));
        existingCategory.setCategoryName(categoryRequest.getCategoryName());
        Category savedCategory = categoryRepository.save(existingCategory);
        return modelMapper.map(savedCategory,CategoryDTO.class);
    }

    @Override
    public void deleteAllCategory(){

        Long size = categoryRepository.count();
        if (size == 0) {
            throw new APIException("Database is already Empty - nothing to delete");
        }
        categoryRepository.deleteAll();
        log.info("Emptied the database - deleted all the items");
    }

    @Override
    public List<CategoryDTO> createBulkCategories(List<CategoryDTO> categoryDTOs) {

        log.info("Requested to create bulk categories at once");

        if (categoryDTOs == null || categoryDTOs.isEmpty()) {
            throw new APIException("Category list cannot be empty");
        }

        List<String> requestedNames  = categoryDTOs.stream()
                .map(name -> name.getCategoryName())
                .toList();

        List<Category> existingCategories  = categoryRepository.findByCategoryNameIn(requestedNames);

        if(!existingCategories.isEmpty()){
            List<String> existingNames = existingCategories.stream()
                    .map(name -> name.getCategoryName())
                    .toList();

            throw new APIException("These categories already exist: " + String.join(", ", existingNames)
            );
        }

        List<Category> categoriesToSave = categoryDTOs.stream()
                .map(categoryDTO -> modelMapper.map(categoryDTO,Category.class))
                .toList();

        List<Category> savedCategories = categoryRepository.saveAll(categoriesToSave);

        List<CategoryDTO> savedCategoryDTOs = savedCategories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        log.info("Created {} categories in bulk", savedCategoryDTOs.size());
        return savedCategoryDTOs;

    }

}

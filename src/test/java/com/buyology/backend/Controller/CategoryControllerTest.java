package com.buyology.backend.Controller;

import com.buyology.backend.dto.CategoryDTO;
import com.buyology.backend.dto.response.CategoryResponseDTO;
import com.buyology.backend.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;


    @Test
    void getAllCategories_returnsOkResponse(){
        CategoryResponseDTO expectedDto = new CategoryResponseDTO();

        when(categoryService.getAllCategories(1, 10, "categoryName", "asc"))
                .thenReturn(expectedDto);

        ResponseEntity<CategoryResponseDTO> response =
                categoryController.getAllCategories(1, 10, "categoryName", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedDto, response.getBody());

        verify(categoryService).getAllCategories(1, 10, "categoryName", "asc");
    }

    @Test
    void createCategory_returnsCreatedResponse(){
        CategoryDTO inputDto = new CategoryDTO();
        inputDto.setCategoryName("Electronics");

        CategoryDTO savedDTO = new CategoryDTO();
        savedDTO.setCategoryId(1L);
        savedDTO.setCategoryName("Electronics");

        when(categoryService.createCategory(inputDto)).thenReturn(savedDTO);

        ResponseEntity<CategoryDTO> response =
                categoryController.createCategory(inputDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(savedDTO, response.getBody());

        verify(categoryService).createCategory(inputDto);
    }

    @Test
    void deleteCategory_returnsDeletedResponse(){
        CategoryDTO expectedDTO = new CategoryDTO();
        expectedDTO.setCategoryId(1L);
        expectedDTO.setCategoryName("Mobiles");

        when(categoryService.deleteCategory(1L)).thenReturn(expectedDTO);

        ResponseEntity<CategoryDTO> response =
                categoryController.deleteCategory(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedDTO, response.getBody());

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    void updateCategory_returnOkResponse(){
        CategoryDTO inputDto = new CategoryDTO();
        inputDto.setCategoryName("Electronics");

        CategoryDTO expectedDTO = new CategoryDTO();
        expectedDTO.setCategoryId(1L);
        expectedDTO.setCategoryName("Mobiles");

        when(categoryService.updateCategory(1L, inputDto))
                .thenReturn(expectedDTO);

        ResponseEntity<CategoryDTO> response =
                categoryController.updateCategory(1L, inputDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedDTO, response.getBody());

        verify(categoryService).updateCategory(1L, inputDto);
    }

    @Test
    void deleteAllCategories_returnOkResponse(){
        ResponseEntity<Void> response =
                categoryController.deleteBulkCategory();

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(categoryService).deleteAllCategory();
    }

    @Test
    void deleteBulkCategories_returnResponse(){
        CategoryDTO category1 = new CategoryDTO();
        category1.setCategoryName("Mobiles");

        CategoryDTO category2 = new CategoryDTO();
        category2.setCategoryName("Laptops");

        List<CategoryDTO> inputList = new ArrayList<>();
        inputList.add(category1);
        inputList.add(category2);

        List<CategoryDTO> outputList = new ArrayList<>();
        outputList.add(category1);
        outputList.add(category2);

        when(categoryService.createBulkCategories(inputList))
                .thenReturn(outputList);

        ResponseEntity<List<CategoryDTO>> response =
                categoryController.createBulkCategories(inputList);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(outputList, response.getBody());

        verify(categoryService).createBulkCategories(inputList);
    }
}

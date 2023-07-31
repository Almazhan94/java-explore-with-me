package ru.practicum.main.service.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.service.category.dto.CategoryDto;
import ru.practicum.main.service.category.dto.NewCategoryDto;
import ru.practicum.main.service.error.ObjectNotFoundException;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryDto createNewCategory(NewCategoryDto newCategoryDto) {
        Category category = new Category();
        category.setName(newCategoryDto.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto updateCategory(int catId, NewCategoryDto newCategoryDto) {

        Category category = categoryRepository.findById(catId)
            .orElseThrow(() -> new ObjectNotFoundException("Категория с Id = " + catId + " не найдена"));

        category.setName(newCategoryDto.getName());
        categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(int catId) {
        Category category = categoryRepository.findById(catId)
            .orElseThrow(() -> new ObjectNotFoundException("Категория с Id = " + catId + " не найдена"));
        categoryRepository.deleteById(catId);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategory(Integer from, Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        List<Category> categoryList = categoryPage.getContent();
        return CategoryMapper.toCategoryDtoList(categoryList);
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Integer catId) {

        Category category = categoryRepository.findById(catId)
            .orElseThrow(() -> new ObjectNotFoundException("Категория с Id = " + catId + " не найдена"));

        return CategoryMapper.toCategoryDto(category);
    }
}

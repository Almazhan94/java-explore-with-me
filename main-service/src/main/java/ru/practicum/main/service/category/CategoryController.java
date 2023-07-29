package ru.practicum.main.service.category;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.service.category.dto.CategoryDto;
import ru.practicum.main.service.category.dto.NewCategoryDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("Добавляется новая категория: {}", newCategoryDto);
        return categoryService.createNewCategory(newCategoryDto);
    }

    @PatchMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable int catId, @RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("Добавляется новая категория: {}", newCategoryDto);
        return categoryService.updateCategory(catId, newCategoryDto);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int catId) {
        log.info("Удаляется категория с идентификатором: {}", catId);
        categoryService.deleteCategory(catId);
    }

    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getCategoryList(@PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                             @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("Запрос на получение категорий");
        return categoryService.getCategory(from, size);
    }

    @GetMapping("/categories/{catId}")                           /*TODO  В случае, если категории с заданным id не найдено, возвращает статус код 404*/
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategoryById(@PathVariable int catId) {
        log.info("Запрос на получение категорий");
        return categoryService.getCategoryById(catId);
    }
}

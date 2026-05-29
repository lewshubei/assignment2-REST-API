package com.restaurant.controller;

import com.restaurant.DBService.MenuService;
import com.restaurant.entity.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
public class MenuRestController {

    @Autowired
    private MenuService service;

    @GetMapping
    public ResponseEntity<List<MenuItem>> getAllMenuItems() {
        return ResponseEntity.ok(service.getAllMenu());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable Long id) {
        MenuItem item = service.getMenuById(id);

        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(item);
    }

    @PostMapping
    public ResponseEntity<MenuItem> createMenuItem(@RequestBody MenuItem item) {
        item.setId(null);
        validateMenuItem(item, false);

        MenuItem savedItem = service.saveMenu(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long id,
                                                   @RequestBody MenuItem item) {
        MenuItem existingItem = service.getMenuById(id);

        if (existingItem == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        item.setId(id);
        validateMenuItem(item, true);

        MenuItem updatedItem = service.saveMenu(item);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        MenuItem existingItem = service.getMenuById(id);

        if (existingItem == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        service.deleteMenu(id);
        return ResponseEntity.noContent().build();
    }

    private void validateMenuItem(MenuItem item, boolean allowMissingImagePath) {
        if (!StringUtils.hasText(item.getName())) {
            throw new IllegalArgumentException("Menu item name is required");
        }

        if (!StringUtils.hasText(item.getDescription())) {
            throw new IllegalArgumentException("Menu item description is required");
        }

        if (!StringUtils.hasText(item.getCategory())) {
            throw new IllegalArgumentException("Menu item category is required");
        }

        if (item.getPrice() == null) {
            throw new IllegalArgumentException("Menu item price is required");
        }

        if (!StringUtils.hasText(item.getAvailability())) {
            throw new IllegalArgumentException("Menu item availability is required");
        }

        if (!allowMissingImagePath && !StringUtils.hasText(item.getImagePath())) {
            throw new IllegalArgumentException("Menu item imagePath is required");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(IllegalArgumentException exception) {
        Map<String, String> errorBody = new LinkedHashMap<>();
        errorBody.put("error", "Bad Request");
        errorBody.put("message", exception.getMessage());

        return ResponseEntity.badRequest().body(errorBody);
    }
}
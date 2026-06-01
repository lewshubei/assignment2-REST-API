package com.restaurant.DBService;

import com.restaurant.entity.MenuItem;
import com.restaurant.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class MenuService {

    @Autowired
    private MenuRepository repository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    // View all menu items
    public List<MenuItem> getAllMenu() {
        return repository.findAll();
    }

    // Add menu item (basic save)
    public MenuItem saveMenu(MenuItem item) {
        return repository.save(item);
    }

    // Save menu item with optional image file handling
    public MenuItem saveMenuWithImage(MenuItem item, MultipartFile imageFile) throws IOException {
        MenuItem existingItem = item.getId() == null ? null : getMenuById(item.getId());

        String previousImagePath = existingItem == null ? null : existingItem.getImagePath();

        if (imageFile != null && !imageFile.isEmpty()) {
            String storedImagePath = storeImage(imageFile);
            item.setImagePath(storedImagePath);
        } else if (!StringUtils.hasText(item.getImagePath()) && existingItem != null) {
            item.setImagePath(existingItem.getImagePath());
        }

        MenuItem saved = saveMenu(item);

        if (previousImagePath != null && !previousImagePath.equals(saved.getImagePath())) {
            deleteStoredImage(previousImagePath);
        }

        return saved;
    }

    // Search menu items by keyword and/or category
    public List<MenuItem> searchMenu(String keyword, String category) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = category != null && !category.trim().isEmpty() && !"all".equalsIgnoreCase(category);

        if (hasKeyword || hasCategory) {
            return repository.searchByKeywordAndCategory(hasKeyword ? keyword : null, hasCategory ? category : null);
        }

        return repository.findAll();
    }

    // Find by category
    public List<MenuItem> findByCategory(String category) {
        return repository.findByCategoryIgnoreCase(category);
    }

    // Find by availability
    public List<MenuItem> findByAvailability(String status) {
        return repository.findByAvailabilityIgnoreCase(status);
    }

    // Delete menu item and its stored image
    public void deleteMenu(Long id) {
        MenuItem item = getMenuById(id);
        if (item != null) {
            try {
                deleteStoredImage(item.getImagePath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete stored image", e);
            }
            repository.deleteById(id);
        }
    }

    // Find menu item by ID
    public MenuItem getMenuById(Long id) {
        return repository.findById(id);
    }

    // Store image on filesystem and return public path
    private String storeImage(MultipartFile imageFile) throws IOException {
        String originalFilename = org.springframework.util.StringUtils.cleanPath(
                imageFile.getOriginalFilename() == null ? "" : imageFile.getOriginalFilename());
        String fileName = UUID.randomUUID() + "-" + originalFilename;

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        Path targetLocation = uploadPath.resolve(fileName).normalize();
        Files.copy(imageFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + fileName;
    }

    // Delete stored image from filesystem
    private void deleteStoredImage(String imagePath) throws IOException {
        if (!StringUtils.hasText(imagePath) || !imagePath.startsWith("/uploads/")) {
            return;
        }

        String fileName = Paths.get(imagePath).getFileName().toString();
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.deleteIfExists(uploadPath.resolve(fileName));
    }
}
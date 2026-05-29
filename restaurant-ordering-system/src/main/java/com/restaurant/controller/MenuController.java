package com.restaurant.controller;

import com.restaurant.DBService.MenuService;
import com.restaurant.entity.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class MenuController {

    @Autowired
    private MenuService service;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    // Display menu list
    @GetMapping("/menu")
    public String viewMenu(@RequestParam(value = "keyword", required = false) String keyword,
                           @RequestParam(value = "category", required = false) String category,
                           @RequestParam(value = "sort", required = false, defaultValue = "popular") String sort,
                           Model model) {

        List<MenuItem> allMenuItems = service.getAllMenu();
        List<String> categories = allMenuItems.stream()
                .map(MenuItem::getCategory)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        List<MenuItem> menuList = allMenuItems;

        if (StringUtils.hasText(keyword)) {
            String lowerKeyword = keyword.toLowerCase();
            menuList = menuList.stream()
                    .filter(menu -> containsIgnoreCase(menu.getName(), lowerKeyword)
                            || containsIgnoreCase(menu.getCategory(), lowerKeyword)
                            || containsIgnoreCase(menu.getDescription(), lowerKeyword))
                    .collect(Collectors.toList());
        }

        if (StringUtils.hasText(category) && !"all".equalsIgnoreCase(category)) {
            menuList = menuList.stream()
                    .filter(menu -> category.equalsIgnoreCase(menu.getCategory()))
                    .collect(Collectors.toList());
        }

        menuList = sortMenu(menuList, sort);

        long availableCount = menuList.stream()
                .filter(menu -> "Available".equalsIgnoreCase(menu.getAvailability()))
                .count();
        long soldOutCount = menuList.stream()
                .filter(menu -> "Unavailable".equalsIgnoreCase(menu.getAvailability()))
                .count();
        double averagePrice = menuList.stream()
                .map(MenuItem::getPrice)
                .filter(price -> price != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        model.addAttribute("menuList", menuList);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("selectedCategory", category == null ? "all" : category);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("totalSpecialties", menuList.size());
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("soldOutCount", soldOutCount);
        model.addAttribute("archivedCount", 0);
        model.addAttribute("averagePrice", BigDecimal.valueOf(averagePrice));
        model.addAttribute("catalogMatchCount", menuList.size());

        return "menu-list";
    }

    // Show add menu form
    @GetMapping("/addMenu")
    public String addMenuPage(Model model) {
        prepareMenuForm(model, new MenuItem(), false);

        return "add-menu";
    }

    // Show edit menu form
    @GetMapping("/editMenu/{id}")
    public String editMenuPage(@PathVariable Long id, Model model) {
        MenuItem item = service.getMenuById(id);

        if (item == null) {
            return "redirect:/menu";
        }

        prepareMenuForm(model, item, true);

        return "add-menu";
    }

    // Save menu item
    @PostMapping("/save")
    public String saveMenu(@ModelAttribute MenuItem item,
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {

        MenuItem existingItem = item.getId() == null ? null : service.getMenuById(item.getId());

        if (existingItem == null && (imageFile == null || imageFile.isEmpty())) {
            throw new IllegalArgumentException("Image file is required");
        }

        String previousImagePath = existingItem == null ? null : existingItem.getImagePath();

        if (imageFile != null && !imageFile.isEmpty()) {
            String storedImagePath = storeImage(imageFile);
            item.setImagePath(storedImagePath);
        } else if (!StringUtils.hasText(item.getImagePath()) && existingItem != null) {
            item.setImagePath(existingItem.getImagePath());
        }

        service.saveMenu(item);

        if (previousImagePath != null && !previousImagePath.equals(item.getImagePath())) {
            deleteStoredImage(previousImagePath);
        }

        return "redirect:/menu";
    }

    // Delete menu item
    @PostMapping("/deleteMenu/{id}")
    public String deleteMenu(@PathVariable Long id) throws IOException {
        MenuItem item = service.getMenuById(id);

        if (item != null) {
            deleteStoredImage(item.getImagePath());
            service.deleteMenu(id);
        }

        return "redirect:/menu";
    }

    private void prepareMenuForm(Model model, MenuItem item, boolean editMode) {
        model.addAttribute("menuItem", item);
        model.addAttribute("editMode", editMode);
        model.addAttribute("pageTitle", editMode ? "Edit Menu Item" : "Add New Menu Item");
        model.addAttribute("formButtonLabel", editMode ? "Update Menu" : "Save Menu");
        model.addAttribute("formSubtitle", editMode ? "Replace the current image or keep it as-is." : "Upload a menu image and store it in the shared uploads folder.");
    }

    private String storeImage(MultipartFile imageFile) throws IOException {
        String originalFilename = StringUtils.cleanPath(
                imageFile.getOriginalFilename() == null ? "" : imageFile.getOriginalFilename());
        String fileName = UUID.randomUUID() + "-" + originalFilename;

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        Path targetLocation = uploadPath.resolve(fileName).normalize();
        Files.copy(imageFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + fileName;
    }

    private void deleteStoredImage(String imagePath) throws IOException {
        if (!StringUtils.hasText(imagePath) || !imagePath.startsWith("/uploads/")) {
            return;
        }

        String fileName = Paths.get(imagePath).getFileName().toString();
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.deleteIfExists(uploadPath.resolve(fileName));
    }

    private List<MenuItem> sortMenu(List<MenuItem> menuList, String sort) {
        Comparator<MenuItem> byName = Comparator.comparing(
                menu -> menu.getName() == null ? "" : menu.getName().toLowerCase());

        if ("price-low".equalsIgnoreCase(sort)) {
            return menuList.stream()
                    .sorted(Comparator.comparing(menu -> menu.getPrice() == null ? Double.MAX_VALUE : menu.getPrice()))
                    .collect(Collectors.toList());
        }

        if ("price-high".equalsIgnoreCase(sort)) {
            return menuList.stream()
                    .sorted(Comparator.comparing((MenuItem menu) -> menu.getPrice() == null ? Double.MIN_VALUE : menu.getPrice()).reversed())
                    .collect(Collectors.toList());
        }

        return menuList.stream()
                .sorted(byName)
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}

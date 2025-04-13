package poly.asm.Controller;

import java.io.File;
// import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import poly.asm.DAO.CategoryDAO;
import poly.asm.DAO.ProductDAO;
import poly.asm.DAO.ProductImageDAO;
import poly.asm.Models.Category;
import poly.asm.Models.Product;
import poly.asm.Models.ProductImage;

@Controller
public class ManagePrController {
    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private ProductImageDAO productImageDAO;

    @Autowired
    CategoryDAO categoryDAO;
    
    // Base directory for storing images
    private final String UPLOAD_DIR = 
    "D:\\HOCTAP\\HocKyV\\BLOCK_II\\Java6\\Project_Java6\\asm\\src\\main\\resources\\static\\Images\\";
    
    @GetMapping("/manageproduct")
    public String manageProduct(Model model, @RequestParam("p") Optional<Integer> p) { 
        Pageable pageable = PageRequest.of(p.orElse(0), 4);
        Page<Product> page = productDAO.findAll(pageable);

        model.addAttribute("page", page);
        model.addAttribute("product", new Product());
        model.addAttribute("isEditMode", false);

        List<Category> categories = categoryDAO.findAll();
        model.addAttribute("categories", categories);
        return "Admin/ManageProduct"; 
    }

    @GetMapping("/manageproduct/search")
    public String searchProducts(Model model,
                               @RequestParam("keyword") String keyword,
                               @RequestParam("p") Optional<Integer> p) {
        Pageable pageable = PageRequest.of(p.orElse(0), 4);
        Page<Product> page;

        if (keyword != null && !keyword.isEmpty()) {
            page = productDAO.findByNameContainingIgnoreCase(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            page = productDAO.findAll(pageable);
        }

        model.addAttribute("page", page);
        model.addAttribute("product", new Product());
        model.addAttribute("isEditMode", false);

        List<Category> categories = categoryDAO.findAll();
        model.addAttribute("categories", categories);

        // Set active tab to list
        model.addAttribute("activeTab", "list");

        return "Admin/ManageProduct";
    }

    @GetMapping("/manageproduct/edit/{id}")
    public String editProduct(Model model, @PathVariable("id") Integer id, @RequestParam("p") Optional<Integer> p) {
        Optional<Product> productOpt = productDAO.findById(id);
        Product product;
        if (productOpt.isPresent()) {
            product = productOpt.get();
            if (product.getCategory() == null) {
                product.setCategory(new Category()); // Khởi tạo category nếu null
            }
            
            // Get additional images for the product
            List<ProductImage> productImages = productImageDAO.findByProduct(product);
            model.addAttribute("productImages", productImages);
        } else {
            product = new Product();
        }
        model.addAttribute("product", product);
        model.addAttribute("isEditMode", true);

        Pageable pageable = PageRequest.of(p.orElse(0), 4);
        Page<Product> page = productDAO.findAll(pageable);
        model.addAttribute("page", page);

        List<Category> categories = categoryDAO.findAll();
        model.addAttribute("categories", categories);

        return "Admin/ManageProduct";
    }

    @RequestMapping("/manageproduct/create")
    public String createProduct(@ModelAttribute Product item, 
                               @RequestParam("imageFile") MultipartFile imageFile,
                               @RequestParam("additionalImages") MultipartFile[] additionalImages,
                               RedirectAttributes redirectAttributes) {
        // Handle main image
        if (!imageFile.isEmpty()) {
            try {
                // Create directory if it doesn't exist
                File directory = new File(UPLOAD_DIR);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Create unique filename
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                File saveFile = new File(UPLOAD_DIR + fileName);

                // Save image file
                imageFile.transferTo(saveFile);

                // Set image path to product
                item.setImage("/Images/" + fileName);
            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("message", "Lỗi khi lưu file: " + e.getMessage());
                redirectAttributes.addFlashAttribute("alertType", "danger");
                return "redirect:/manageproduct";
            }
        }

        try {
            // Save product to database
            Product savedProduct = productDAO.save(item);
            
            // Handle additional images
            if (additionalImages != null && additionalImages.length > 0) {
                for (MultipartFile additionalImage : additionalImages) {
                    if (!additionalImage.isEmpty()) {
                        try {
                            // Create unique filename
                            String fileName = System.currentTimeMillis() + "_" + additionalImage.getOriginalFilename();
                            File saveFile = new File(UPLOAD_DIR + fileName);

                            // Save image file
                            additionalImage.transferTo(saveFile);

                            // Create and save ProductImage entity
                            ProductImage productImage = new ProductImage();
                            productImage.setImageUrl("/Images/" + fileName);
                            productImage.setProduct(savedProduct);
                            productImageDAO.save(productImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Continue with other images even if one fails
                        }
                    }
                }
            }
            
            redirectAttributes.addFlashAttribute("message", "Thêm sản phẩm thành công!");
            redirectAttributes.addFlashAttribute("alertType", "success");
            redirectAttributes.addFlashAttribute("reloadPage", true);
            redirectAttributes.addFlashAttribute("activeTab", "list");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi khi lưu sản phẩm: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }
        
        return "redirect:/manageproduct";
    }

    @PostMapping("/manageproduct/update")
    public String updateProduct(@ModelAttribute Product item, 
                               @RequestParam("imageFile") MultipartFile imageFile,
                               @RequestParam("additionalImages") MultipartFile[] additionalImages,
                               @RequestParam(value = "deleteImageIds", required = false) List<Integer> deleteImageIds,
                               RedirectAttributes redirectAttributes) {
        // Check if product exists
        Optional<Product> existingProductOpt = productDAO.findById(item.getId());
        if (!existingProductOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy sản phẩm với ID: " + item.getId());
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/manageproduct";
        }
        
        Product existingProduct = existingProductOpt.get();
        
        // Handle main image if new one is provided
        if (!imageFile.isEmpty()) {
            try {
                // Create directory if it doesn't exist
                File directory = new File(UPLOAD_DIR);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Delete old image if exists
                if (existingProduct.getImage() != null && !existingProduct.getImage().isEmpty()) {
                    String oldImagePath = UPLOAD_DIR + existingProduct.getImage().replace("/Images/", "");
                    File oldImageFile = new File(oldImagePath);
                    if (oldImageFile.exists()) {
                        oldImageFile.delete();
                    }
                }

                // Create unique filename
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                File saveFile = new File(UPLOAD_DIR + fileName);

                // Save image file
                imageFile.transferTo(saveFile);

                // Set image path to product
                item.setImage("/Images/" + fileName);
            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("message", "Lỗi khi lưu file: " + e.getMessage());
                redirectAttributes.addFlashAttribute("alertType", "danger");
                return "redirect:/manageproduct";
            }
        } else {
            // Keep old image if no new one is provided
            item.setImage(existingProduct.getImage());
        }

        try {
            // Save updated product
            Product savedProduct = productDAO.save(item);
            
            // Delete selected images if any
            if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                for (Integer imageId : deleteImageIds) {
                    Optional<ProductImage> imageOpt = productImageDAO.findById(imageId);
                    if (imageOpt.isPresent()) {
                        ProductImage image = imageOpt.get();
                        // Delete file from filesystem
                        if (image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
                            String imagePath = UPLOAD_DIR + image.getImageUrl().replace("/Images/", "");
                            File imageFile2 = new File(imagePath);
                            if (imageFile2.exists()) {
                                imageFile2.delete();
                            }
                        }
                        // Delete from database
                        productImageDAO.deleteById(imageId);
                    }
                }
            }
            
            // Add new additional images
            if (additionalImages != null && additionalImages.length > 0) {
                for (MultipartFile additionalImage : additionalImages) {
                    if (!additionalImage.isEmpty()) {
                        try {
                            // Create unique filename
                            String fileName = System.currentTimeMillis() + "_" + additionalImage.getOriginalFilename();
                            File saveFile = new File(UPLOAD_DIR + fileName);

                            // Save image file
                            additionalImage.transferTo(saveFile);

                            // Create and save ProductImage entity
                            ProductImage productImage = new ProductImage();
                            productImage.setImageUrl("/Images/" + fileName);
                            productImage.setProduct(savedProduct);
                            productImageDAO.save(productImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Continue with other images even if one fails
                        }
                    }
                }
            }
            
            redirectAttributes.addFlashAttribute("message", "Cập nhật sản phẩm thành công!");
            redirectAttributes.addFlashAttribute("alertType", "success");
            redirectAttributes.addFlashAttribute("activeTab", "list");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }
        
        return "redirect:/manageproduct";
    }

    @GetMapping("/manageproduct/delete/{id}")
    @Transactional
    public String deleteProduct(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            // Check if product exists
            Optional<Product> productOpt = productDAO.findById(id);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                
                // Delete all associated product images
                List<ProductImage> productImages = productImageDAO.findByProduct(product);
                for (ProductImage image : productImages) {
                    // Delete file from filesystem
                    if (image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
                        String imagePath = UPLOAD_DIR + image.getImageUrl().replace("/Images/", "");
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            imageFile.delete();
                        }
                    }
                }
                
                // Delete all product images from database
                productImageDAO.deleteByProduct(product);
                
                // Delete main image from filesystem
                if (product.getImage() != null) {
                    String imagePath = UPLOAD_DIR + product.getImage().replace("/Images/", "");
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }
                
                // Delete product from database
                productDAO.deleteById(id);
                redirectAttributes.addFlashAttribute("message", "Xóa sản phẩm thành công!");
                redirectAttributes.addFlashAttribute("alertType", "success");
                redirectAttributes.addFlashAttribute("activeTab", "list");
            } else {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy sản phẩm với ID: " + id);
                redirectAttributes.addFlashAttribute("alertType", "warning");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Lỗi khi xóa sản phẩm: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }
        return "redirect:/manageproduct";
    }
}

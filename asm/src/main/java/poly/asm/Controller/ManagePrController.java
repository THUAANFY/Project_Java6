package poly.asm.Controller;

import java.io.File;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import poly.asm.DAO.CategoryDAO;
import poly.asm.DAO.ProductDAO;
import poly.asm.Models.Category;
import poly.asm.Models.Product;


@Controller
public class ManagePrController {
    @Autowired
    private ProductDAO productDAO;

    @Autowired
    CategoryDAO categoryDAO;
    
    @GetMapping("/manageproduct")
    public String manageProduct(Model model, @RequestParam("p") Optional<Integer> p) { 
        Pageable pageable = PageRequest.of(p.orElse(0), 4);
        Page<Product> page = productDAO.findAll(pageable);

        model.addAttribute("page", page);
        model.addAttribute("product", new Product());

        List<Category> categories = categoryDAO.findAll();
        model.addAttribute("categories", categories);
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
        } else {
            product = new Product();
        }
        model.addAttribute("product", product);

        Pageable pageable = PageRequest.of(p.orElse(0), 4);
        Page<Product> page = productDAO.findAll(pageable);
        model.addAttribute("page", page);

        List<Category> categories = categoryDAO.findAll();
        model.addAttribute("categories", categories);

        return "Admin/ManageProduct";
    }

    @RequestMapping("/manageproduct/create")
    public String createProduct(@ModelAttribute Product item, @RequestParam("imageFile") MultipartFile imageFile, Model model) {
        if (!imageFile.isEmpty()) {
            try {
                // Đường dẫn lưu ảnh
                String uploadDir = "D:/HOCTAP/HocKyV/BLOCK_II/Java6/Assignment/Project_Java6/asm/src/main/resources/static/Images/";

                // Tạo thư mục nếu chưa tồn tại
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Tạo tên file duy nhất
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                File saveFile = new File(uploadDir + fileName);

                // Lưu file ảnh
                imageFile.transferTo(saveFile);

                // Gán đường dẫn ảnh vào product
                item.setImage("/Images/" + fileName);
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("message", "Lỗi khi lưu file: " + e.getMessage());
                // Trả về lại trang quản lý sản phẩm với thông báo lỗi
                Pageable pageable = PageRequest.of(0, 4);
                Page<Product> page = productDAO.findAll(pageable);
                model.addAttribute("page", page);
                model.addAttribute("product", item);
                List<Category> categories = categoryDAO.findAll();
                model.addAttribute("categories", categories);
                return "Admin/ManageProduct";
            }
        }

        // Lưu sản phẩm vào database
        productDAO.save(item);
        return "redirect:/manageproduct";
    }

    @GetMapping("/manageproduct/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id, Model model) {
        try {
            // Kiểm tra sản phẩm có tồn tại không
            Optional<Product> productOpt = productDAO.findById(id);
            if (productOpt.isPresent()) {
                // Xóa ảnh khỏi hệ thống file (nếu có)
                Product product = productOpt.get();
                if (product.getImage() != null) {
                    String imagePath = "D:/HOCTAP/HocKyV/BLOCK_II/Java6/Assignment/Project_Java6/asm/src/main/resources/static" + product.getImage();
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }
                // Xóa sản phẩm khỏi database
                productDAO.deleteById(id);
                model.addAttribute("message", "Xóa sản phẩm thành công!");
            } else {
                model.addAttribute("message", "Không tìm thấy sản phẩm với ID: " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/manageproduct";
    }

}

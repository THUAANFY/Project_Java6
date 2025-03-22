package poly.asm.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.asm.DAO.CategoryDAO;
import poly.asm.Models.Category;


@Controller
public class ManageCategoryController {
    @Autowired
    CategoryDAO categoryDAO;

    @RequestMapping("/managecategory")
    public String index(Model model) {
        Category item = new Category();
        model.addAttribute("item", item);
        List<Category> items = categoryDAO.findAll();
        model.addAttribute("items", items);
        return "Admin/ManageCategory";
    }
    
    @RequestMapping("/managecategory/edit/{id}")
    public String edit(Model model, @PathVariable("id") String id) {
        try {
            Category item = categoryDAO.findById(id).orElse(new Category());
            model.addAttribute("item", item);
            List<Category> items = categoryDAO.findAll();
            model.addAttribute("items", items);
            return "Admin/ManageCategory";
        } catch (Exception e) {
            model.addAttribute("message", "Không tìm thấy danh mục với ID: " + id);
            model.addAttribute("alertClass", "danger");
            return "redirect:/managecategory";
        }
    }

    @RequestMapping("/managecategory/create")
    public String create(Category item, RedirectAttributes redirectAttributes) {
        try {
            categoryDAO.save(item);
            redirectAttributes.addFlashAttribute("message", "Thêm danh mục thành công!");
            redirectAttributes.addFlashAttribute("alertClass", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi khi thêm danh mục: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "danger");
        }
        return "redirect:/managecategory";
    }

    @RequestMapping("/managecategory/update")
    public String update(Category item, RedirectAttributes redirectAttributes) {
        try {
            categoryDAO.save(item);
            redirectAttributes.addFlashAttribute("message", "Cập nhật danh mục thành công!");
            redirectAttributes.addFlashAttribute("alertClass", "info");
            return "redirect:/managecategory/edit/" + item.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi khi cập nhật danh mục: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "danger");
            return "redirect:/managecategory";
        }
    }

    @RequestMapping("/managecategory/delete/{id}")
    public String delete(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            categoryDAO.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Xóa danh mục thành công!");
            redirectAttributes.addFlashAttribute("alertClass", "danger");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi khi xóa danh mục: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "danger");
        }
        return "redirect:/managecategory";
    }
}


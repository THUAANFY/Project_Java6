package poly.asm.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
        Category item = categoryDAO.findById(id).get();
        model.addAttribute("item", item);
        List<Category> items = categoryDAO.findAll();
        model.addAttribute("items", items);
        return "Admin/ManageCategory";
    }

    @RequestMapping("/managecategory/create")
        public String create(Category item) {
        categoryDAO.save(item); 
        return "redirect:/managecategory";
    }

    @RequestMapping("/managecategory/update")
        public String update(Category item) {
        categoryDAO.save(item); 
        return "redirect:/managecategory/edit/" + item.getId();
    }

    @RequestMapping("/managecategory/delete/{id}")
        public String create(@PathVariable("id") String id) {
        categoryDAO.deleteById(id);
        return "redirect:/managecategory";
    }
}

package poly.asm.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


import ch.qos.logback.core.model.Model;


@Controller
public class ContactController {
    @GetMapping("/Contact")
    public String Contact(Model model ) {
        return "Home/contact";
    }
    
}

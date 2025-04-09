package poly.asm.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ch.qos.logback.core.model.Model;


@Controller
public class IntroduceController {
    @GetMapping("/Introduce")
    public String Introduce(Model model ) {
        return "Home/introduce";
    }
    
}
 
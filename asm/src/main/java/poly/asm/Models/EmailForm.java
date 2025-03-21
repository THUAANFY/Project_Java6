package poly.asm.Models;

import lombok.Data;

@Data
public class EmailForm {
    private String from;
    private String to;
    private String subject;
    private String body;
}

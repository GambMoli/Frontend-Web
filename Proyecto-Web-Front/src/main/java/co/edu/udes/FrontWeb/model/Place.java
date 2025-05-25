package co.edu.udes.FrontWeb.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Place {
    private Long id;
    private String name;
    private String type;
    private int quantity;
    private boolean available;
    private String description;

}

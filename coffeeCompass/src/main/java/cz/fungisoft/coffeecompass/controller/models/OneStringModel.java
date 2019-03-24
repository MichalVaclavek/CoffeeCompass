package cz.fungisoft.coffeecompass.controller.models;

import lombok.Data;

/**
 * A model for passing one String input from thymeleaf form to Spring Controller
 * 
 * @author Michal Vaclavek
 *
 */
@Data
public class OneStringModel
{
    private String input;
}

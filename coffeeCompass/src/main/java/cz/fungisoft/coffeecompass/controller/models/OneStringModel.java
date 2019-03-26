package cz.fungisoft.coffeecompass.controller.models;

import javax.validation.constraints.NotEmpty;

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
    @NotEmpty
    private String input;
}

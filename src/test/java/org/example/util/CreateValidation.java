package org.example.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CreateValidation {

    protected WebDriver driver;

    public CreateValidation(WebDriver driver) {
        this.driver = driver;
    }

    private  By tagPusernameValidation = By.className("user-form-error");

    public String usernameValidation(){

        By tagPusernameValidation = By.className("user-form-error");
        return   driver.findElement(tagPusernameValidation).getText();
    }
}

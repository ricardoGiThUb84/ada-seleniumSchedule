package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.model.User;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTest {
    static WebDriver webDriver;
    static User user;


    @BeforeAll
    public static void load() {

        WebDriverManager.chromedriver().setup();

        webDriver = new ChromeDriver(
                new ChromeOptions().addArguments("--remote-allow-origins=*")
        );


    }

    @AfterAll
    public static void destroy() {

        webDriver.quit();
    }

    @Test
    public void checkIfTheUrlCreatePage_IsTheSame_If_AnInvalidValueIsEnter() throws InterruptedException {

        getCreatePage();
        User user = getUser();

        user.setName("");
        user.setUsername("");
        user.setPassword("");

        createAnUserAtPageWithoutValidation(user);

        Assertions.assertEquals("http://localhost:8080/app/users/create", webDriver.getCurrentUrl());
    }


    @Test

    public void createAnUser() throws InterruptedException {

        getCreatePage();

        User user = getUser();

        createAnUserAtPage(user);

        String usernameRecordedAtList = returnUsernameFromListUserPage(user, webDriver);

        Assertions.assertEquals(user.getUsername() , usernameRecordedAtList);

}

    @Test
    public void verifyIfIsPossibleCreateAnUserWtithSameUsername(){

        getListUserPage();

        User user = getUser();

        String registeredUsername = getAnRegisteredUsers().get(0).getUsername();

        user.setUsername(registeredUsername);

        getCreatePage();

        assertThrows(RuntimeException.class, () ->
                createAnUserAtPage(user)
        );


    }

    @Test
    public void verify_If_IsPossible_EditAnUserNameOfARegisteredUser(){

        getCreatePage();
        User user = getUser();

        createAnUserAtPageWithoutValidation(user);

      Assertions.assertNotNull(updateAnUsernameFromAnRegisteredUser(user));
    }

    @Test
    public void verify_IfAPasswordFieldIsEmpty_WhenUpdateAsUser(){

        getCreatePage();
        User user = getUser();

        createAnUserAtPageWithoutValidation(user);

        user.setName(RandomStringUtils.randomAlphabetic(10));
        user.setPassword(RandomStringUtils.randomAlphanumeric(8));

        findAnElementByUsernameAndSelectToUpdate(user);

        Assertions.assertTrue(webDriver.findElement(By.id("password")).getText().equalsIgnoreCase(""));
    }

    @Test
    public void updateTheNameAndPasswordOfTheUser(){

        getListUserPage();

        User updatedUser = getAnRegisteredUsers().get(0);

        findAnElementByUsernameAndSelectToUpdate(updatedUser);

        String name = RandomStringUtils.randomAlphabetic(10);
        String password = RandomStringUtils.randomAlphabetic(8);

        updatedUser.setName(name);
        updatedUser.setPassword(password);

        updateANameAndPasswordUser(updatedUser);

        Assertions.assertEquals(name , updatedUser.getName());
        Assertions.assertEquals(password , updatedUser.getPassword());


    }

    private void updateANameAndPasswordUser(User user) {

//       findAnElementByUsernameAndSelectToUpdate(user);

        webDriver.findElement(By.id("name")).sendKeys("");
        webDriver.findElement(By.id("name")).sendKeys(user.getName());

        webDriver.findElement(By.id("password")).sendKeys(user.getPassword());

        webDriver.findElement(By.tagName("button")).click();
    }




    private String updateAnUsernameFromAnRegisteredUser(User user) {

        findAnElementByUsernameAndSelectToUpdate(user);

        return webDriver.findElement(By.id("username")).getAttribute("readonly");

    }

    private static void findAnElementByUsernameAndSelectToUpdate(User user) {
        By getAnUserByUsername =  By
                .xpath("//table[@class=\"table\" ]/tbody/tr/td[text()=\""
                        + user.getUsername()+"\"]//following-sibling::td/a");

        webDriver.findElement(getAnUserByUsername).click();
    }

    private void getListUserPage() {

        webDriver.get("http://localhost:8080/app/users");
    }

    private static void getCreatePage() {
        webDriver.get("http://localhost:8080/app/users/create");
    }

    private List<User> getAnRegisteredUsers() {

        By elementsPerLine = By.xpath("//table[@class=\"table\"]/tbody[*]/tr");

        final List<User> userCollection = webDriver.findElements(elementsPerLine).stream().map(usuarios -> {
            {
                String[] s = usuarios.getText().split(" ");
                return new User(s[0], s[1], "");
            }
        }).collect(Collectors.toList());

        return userCollection;

    }

    private static void createAnUserAtPage(User user) {
        webDriver.findElement(By.id("name")).sendKeys(user.getName());
        webDriver.findElement(By.id("username")).sendKeys(user.getUsername());
        webDriver.findElement(By.id("password")).sendKeys(user.getPassword());

        new WebDriverWait(webDriver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(
                        By.tagName("button"))).click();

        usernameValidation(webDriver);
    }

    private static void createAnUserAtPageWithoutValidation(User user) {
        webDriver.findElement(By.id("name")).sendKeys(user.getName());
        webDriver.findElement(By.id("username")).sendKeys(user.getUsername());
        webDriver.findElement(By.id("password")).sendKeys(user.getPassword());

        new WebDriverWait(webDriver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(
                        By.tagName("button"))).click();
    }

    public static void usernameValidation(WebDriver webDriver){

        if(verifyingIfRegisterPage(webDriver)){

            By tagPusernameValidation = By.className("user-form-error");

            boolean b = new WebDriverWait(webDriver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.textToBe(tagPusernameValidation, "Username already in use"));

            if(b) throw new RuntimeException("Username already in use");
        }

    }

    private static boolean verifyingIfRegisterPage(WebDriver webDriver) {

       return webDriver.findElement
                (By.xpath("//div[@class=\"panel-heading\"]/label"))
                .getText().equalsIgnoreCase("User -> Register");
    }

    private static String returnUsernameFromListUserPage(User user, WebDriver webDriver) {

        By usernamesBy = By.xpath("//table[@class=\"table\"]/tbody[*]//child::td[contains(text() ,'"
                +user.getUsername()+"')]");

        return webDriver.findElements(usernamesBy).stream()
                .filter(element -> element.getText().equalsIgnoreCase(user.getUsername()))
                .findFirst().get().getText();
    }







    private static User getUser() {
        String name = RandomStringUtils.randomAlphabetic(10);
        String username = RandomStringUtils.randomAlphabetic(10);
        String password = RandomStringUtils.randomAlphanumeric(8);
        return  new User(name, username, password);
    }
    private static void putPageOnHold(long forMilliseconds) throws InterruptedException {
        Thread.sleep(forMilliseconds);
    }
}

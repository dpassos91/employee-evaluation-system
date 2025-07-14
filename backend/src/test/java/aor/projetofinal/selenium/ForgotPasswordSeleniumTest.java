package aor.projetofinal.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

public class ForgotPasswordSeleniumTest {

    private WebDriver driver;

    @BeforeEach
    public void setup() {
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void testForgotPasswordForm() {
        driver.get("https://localhost:3000/forgotpassword");

        // Preenche o campo email
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("davidjccoelho@ua.pt");

        // Clica no botão recuperar
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        // Aqui podes verificar algo que aconteça depois do submit
        // Como o submit não faz navegação nem mudanças visíveis,
        // apenas vamos esperar 2 segundos para ver se não crasha
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.visibilityOf(emailInput)); // só espera o input ficar visível (continua na página)

        // Se quiseres, podes assertar que o campo ainda está visível
        assertTrue(emailInput.isDisplayed());
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

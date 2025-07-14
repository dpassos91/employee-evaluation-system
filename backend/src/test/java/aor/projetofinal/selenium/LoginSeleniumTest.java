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

public class LoginSeleniumTest {

    private WebDriver driver;

    @BeforeEach
    public void setup() {
        // Configura o driver do Firefox (geckodriver)
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void testLogin() {
        driver.get("https://localhost:3000/login");

        // Preenche o campo email
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("davidjccoelho@gmail.com");

        // Preenche o campo password
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys("senha123?");

        // Clica no botão submit
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        // Espera até a URL conter "/dashboard" ou "/home" até 10 segundos
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        boolean urlChanged = wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/home")
        ));

        assertTrue(urlChanged, "Login falhou ou não redirecionou para o dashboard.");

        // Alternativamente, podes esperar por um elemento específico na página de destino:
        // WebElement dashboardElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dashboard-welcome")));
        // assertNotNull(dashboardElement);
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

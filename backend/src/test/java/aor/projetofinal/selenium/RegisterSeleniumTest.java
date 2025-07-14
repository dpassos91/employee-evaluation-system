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

public class RegisterSeleniumTest {

    private WebDriver driver;

    @BeforeEach
    public void setup() {
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void testRegister() {
        driver.get("https://localhost:3000/register");

        // Preencher campo email
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("marpicoelho@gmail.com");

        // Preencher campo password
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys("senha123?");

        // Preencher confirmação da password
        WebElement confirmInput = driver.findElement(By.xpath("//input[@name='confirmPassword']"));
        confirmInput.sendKeys("senha123?");

        // Clicar no botão submit
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        // Espera até que URL contenha "/login" (redirecionamento pós-registo)
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        boolean urlChanged = wait.until(ExpectedConditions.urlContains("/login"));

        assertTrue(urlChanged, "Registo falhou ou não redirecionou para a página de login.");

        // Opcional: verificar se aparece mensagem de confirmação no toast
        // WebElement toastMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("Toastify__toast")));
        // assertTrue(toastMessage.getText().contains("Registration not complete"));
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

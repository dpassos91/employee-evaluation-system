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

public class ChatPageSeleniumTest {

    private WebDriver driver;

    @BeforeEach
    public void setup() {
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void testLoginAndChatPageLoads() {
        driver.get("https://localhost:3000/login");

        // Login
        driver.findElement(By.id("email")).sendKeys("davidjccoelho@gmail.com");
        driver.findElement(By.id("password")).sendKeys("senha123?");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/home")
        ));

        // Navega para a página de chat
        driver.get("https://localhost:3000/chat");

        // Espera até o container principal do chat estar visível, seletor simplificado sem h-[500px]
        WebElement chatContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.flex.w-full.max-w-6xl.mx-auto.bg-white.rounded-2xl.shadow.overflow-hidden")
        ));

        assertNotNull(chatContainer, "A página de chat não carregou corretamente.");
    }


    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

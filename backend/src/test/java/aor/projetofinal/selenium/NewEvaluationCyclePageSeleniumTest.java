package aor.projetofinal.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.*;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class NewEvaluationCyclePageSeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setup() {
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test
    public void testPageLoads() {
        // Aceder à página de login
        driver.get("https://localhost:3000/login");

        // Fazer login
        driver.findElement(By.id("email")).sendKeys("davidjccoelho@gmail.com");
        driver.findElement(By.id("password")).sendKeys("senha123?");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Esperar página dashboard ou home
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/home")
        ));

        // Navegar para página de novo ciclo de avaliação
        driver.get("https://localhost:3000/newevaluationcycle");

        // Verificar se input de data de fim do ciclo está visível
        WebElement endDateInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='date']")));
        assertNotNull(endDateInput, "Input de data fim do ciclo não encontrado.");
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

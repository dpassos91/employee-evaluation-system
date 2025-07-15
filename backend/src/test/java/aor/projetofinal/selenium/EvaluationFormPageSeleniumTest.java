package aor.projetofinal.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.*;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluationFormPageSeleniumTest {

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
    public void testLoginAndLoadEvaluationForm() {
        // 1. Login
        driver.get("https://localhost:3000/login");
        driver.findElement(By.id("email")).sendKeys("davidjccoelho@ua.pt");
        driver.findElement(By.id("password")).sendKeys("senha123?");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/home")
        ));

        // 2. Navegar para a página de avaliação do userId 4 (exemplo)
        driver.get("https://localhost:3000/evaluationform/4");

        // 3. Esperar input do nome aparecer e verificar se está preenchido
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='text'][readonly]")));
        assertNotNull(nameInput, "Campo nome não encontrado.");
        assertFalse(nameInput.getAttribute("value").isEmpty(), "Campo nome está vazio.");

        // 4. Verificar se o select de avaliação está presente
        WebElement gradeSelect = driver.findElement(By.cssSelector("select"));
        assertNotNull(gradeSelect);

        // Opcional: verificar botão salvar
        WebElement saveButton = driver.findElement(By.xpath("//button[contains(text(),'Salvar') or contains(text(),'Save')]"));
        assertNotNull(saveButton);
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

package aor.projetofinal.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.*;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CoursesPageSeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setup() {
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @Test
    public void testLoginAndLoadCoursesPage() {
        // 1. Login
        driver.get("https://localhost:3000/login");
        driver.findElement(By.id("email")).sendKeys("davidjccoelho@gmail.com");
        driver.findElement(By.id("password")).sendKeys("senha123?");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/home")
        ));

        // 2. Navega para a página de cursos
        driver.get("https://localhost:3000/courses");

        // 3. Espera que a tabela de cursos ou mensagem "Nenhuma formação encontrada" apareça
        boolean tablePresent = false;
        boolean emptyMsgPresent = false;

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table")));
            // Opcional: Verifica se tem linhas na tabela
            List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
            tablePresent = !rows.isEmpty();
        } catch (TimeoutException e) {
            // tabela não apareceu
        }

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Nenhuma formação encontrada')]")));
            emptyMsgPresent = true;
        } catch (TimeoutException e) {
            // mensagem não apareceu
        }

        assertTrue(tablePresent || emptyMsgPresent, "Nem tabela de cursos nem mensagem de vazio foram encontradas.");
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

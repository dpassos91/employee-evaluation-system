package aor.projetofinal.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.*;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UsersPageSeleniumTest {

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
    public void testLoginAndLoadUsersPage() {
        // 1. Aceder à página de login
        driver.get("https://localhost:3000/login");

        // 2. Fazer login
        driver.findElement(By.id("email")).sendKeys("davidjccoelho@gmail.com");
        driver.findElement(By.id("password")).sendKeys("senha123?");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // 3. Esperar redirecionamento para dashboard/home
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/home")
        ));

        // 4. Navegar para a página de utilizadores (confirma a rota correta)
        driver.get("https://localhost:3000/userslist");

        // 5. Esperar que a tabela ou mensagem de vazio esteja presente
        By tableLocator = By.cssSelector("table");
        By emptyMsgLocator = By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'nenhum utilizador encontrado') or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no users found')]");

        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(tableLocator),
                ExpectedConditions.visibilityOfElementLocated(emptyMsgLocator)
        ));

        // 6. Verificar que a tabela ou mensagem de vazio está presente
        List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
        List<WebElement> emptyMessages = driver.findElements(emptyMsgLocator);

        boolean hasDataOrEmptyMessage = !rows.isEmpty() || !emptyMessages.isEmpty();
        assertTrue(hasDataOrEmptyMessage, "Nem tabela nem mensagem de utilizadores encontrados.");
    }


    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

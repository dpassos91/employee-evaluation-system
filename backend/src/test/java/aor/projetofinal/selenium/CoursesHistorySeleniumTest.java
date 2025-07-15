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
import java.util.List;

public class CoursesHistorySeleniumTest {

    private WebDriver driver;

    @BeforeEach
    public void setup() {
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void testLoginAndLoadCoursesHistoryPage() {
        driver.get("https://localhost:3000/login");

        driver.findElement(By.id("email")).sendKeys("davidjccoelho@ua.pt");
        driver.findElement(By.id("password")).sendKeys("senha123?");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/home")
        ));

        driver.get("https://localhost:3000/profile/4/courseshistory");

        WebElement yearSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("yearSelect")));
        assertNotNull(yearSelect, "Dropdown de seleção do ano não encontrado.");

        boolean tablePresent = false;
        boolean emptyMsgPresent = false;

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table tbody tr")));
            tablePresent = true;
        } catch (Exception e) {}

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Sem formações')]")));
            emptyMsgPresent = true;
        } catch (Exception e) {}

        assertTrue(tablePresent || emptyMsgPresent, "Nem tabela de formações nem mensagem 'Sem formações' foram encontradas.");
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

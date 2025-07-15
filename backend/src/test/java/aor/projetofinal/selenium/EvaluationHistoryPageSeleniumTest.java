package aor.projetofinal.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.*;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluationHistoryPageSeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setup() {
        // Configura o WebDriver para Firefox
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        driver.manage().window().maximize();
        // Espera explícita com timeout de 20 segundos para maior estabilidade
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @Test
    public void testLoginAndLoadEvaluationHistory() {
        // 1. Aceder à página de login
        driver.get("https://localhost:3000/login");

        // 2. Fazer login com utilizador válido
        driver.findElement(By.id("email")).sendKeys("davidjccoelho@ua.pt");
        driver.findElement(By.id("password")).sendKeys("senha123?");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // 3. Esperar que a página redirecione para dashboard ou home
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/home")
        ));

        // 4. Navegar para a página do histórico de avaliações do userId 4
        driver.get("https://localhost:3000/profile/4/evaluationhistory");

        // 5. Esperar que o filtro de ciclo esteja visível (suporte português/inglês)
        WebElement filtroCiclo = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder='Ciclo'], input[placeholder='Cycle']")
        ));
        assertNotNull(filtroCiclo, "Filtro de ciclo não encontrado.");

        // 6. Esperar até que ou a tabela de avaliações apareça ou a mensagem de "Nenhuma avaliação encontrada"
        boolean carregouConteudo = false;
        try {
            carregouConteudo = wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table")),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Nenhuma avaliação encontrada') or contains(text(),'No evaluations found')]"))
            )) != null;
        } catch (TimeoutException e) {
            // Ignorar timeout aqui para assert abaixo
        }

        assertTrue(carregouConteudo, "Nem tabela nem mensagem de avaliações encontradas.");

        // 7. Se existir tabela, verificar se as colunas "Ciclo" e "Avaliação" estão presentes
        List<WebElement> linhasTabela = driver.findElements(By.cssSelector("table tbody tr"));
        if (!linhasTabela.isEmpty()) {
            List<WebElement> cabecalhos = driver.findElements(By.cssSelector("table thead th"));
            boolean temColunaCiclo = cabecalhos.stream()
                    .anyMatch(h -> h.getText().contains("Ciclo") || h.getText().contains("Cycle"));
            boolean temColunaAvaliacao = cabecalhos.stream()
                    .anyMatch(h -> h.getText().contains("Avaliação") || h.getText().contains("Grade"));

            assertTrue(temColunaCiclo, "Coluna 'Ciclo' não encontrada na tabela.");
            assertTrue(temColunaAvaliacao, "Coluna 'Avaliação' não encontrada na tabela.");
        }
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

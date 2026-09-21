package com.example.bookshelf.automation;

import com.example.bookshelf.repository.BookRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * Black-box functional tests driving the running application over real HTTP (REST Assured)
 * and through the browser UI (Selenium), exercising full user journeys end to end.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookApiFunctionalTest extends AbstractTestNGSpringContextTests {

    @LocalServerPort
    private int port;

    @Autowired
    private BookRepository bookRepository;

    private WebDriver driver;

    @BeforeClass
    void setUpClass() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--disable-gpu", "--window-size=1280,800");
        driver = new ChromeDriver(options);
    }

    @AfterClass
    void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeMethod
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        bookRepository.deleteAll();
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void fullLifecycle_createFilterUpdateDelete() {
        String id = given()
                .contentType(ContentType.JSON)
                .body(Map.of("title", "Dune", "author", "Frank Herbert"))
                .when().post("/books")
                .then().statusCode(201)
                .body("title", equalTo("Dune"))
                .extract().path("id");

        given()
                .queryParam("title", "dun")
                .queryParam("author", "herbert")
                .when().get("/books")
                .then().statusCode(200)
                .body("$", hasSize(1));

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("author", "New Author"))
                .when().put("/books/{id}", id)
                .then().statusCode(200)
                .body("author", equalTo("New Author"))
                .body("title", equalTo("Dune"));

        when()
                .get("/books")
                .then().statusCode(200)
                .body("[0].author", equalTo("New Author"));

        when()
                .delete("/books/{id}", id)
                .then().statusCode(204);

        when()
                .get("/books")
                .then().statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    void creatingDuplicateBookReturns400WithMessage() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("title", "1984", "author", "George Orwell"))
                .when().post("/books")
                .then().statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("title", "1984", "author", "George Orwell"))
                .when().post("/books")
                .then().statusCode(400)
                .body("message", equalTo("record already exist"));
    }

    @Test
    void creatingInvalidBookReturns400WithMessage() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("title", "", "author", "George Orwell"))
                .when().post("/books")
                .then().statusCode(400)
                .body("message", equalTo("please check the request payload"));
    }

    @Test
    void updatingMissingBookReturns404WithMessage() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("author", "New Author"))
                .when().put("/books/does-not-exist")
                .then().statusCode(404)
                .body("message", equalTo("no book present"));
    }

    @Test
    void deletingMissingBookReturns404WithMessage() {
        when()
                .delete("/books/does-not-exist")
                .then().statusCode(404)
                .body("message", equalTo("no book present"));
    }

    @Test
    void filteringWithNoMatchReturnsEmptyArray() {
        given()
                .queryParam("title", "nonexistent")
                .when().get("/books")
                .then().statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    void uiAddBookThroughFormUpdatesShelfAndCount() {
        driver.get(baseUrl() + "/index.html");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        driver.findElement(By.id("title")).sendKeys("Dune");
        driver.findElement(By.id("author")).sendKeys("Frank Herbert");
        driver.findElement(By.cssSelector("#book-form button[type='submit']")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("book-count"), "1"));

        WebElement bookTitle = driver.findElement(By.cssSelector(".book-title"));
        WebElement bookAuthor = driver.findElement(By.cssSelector(".book-author"));
        Assert.assertEquals(bookTitle.getText(), "Dune");
        Assert.assertEquals(bookAuthor.getText(), "Frank Herbert");
    }
}


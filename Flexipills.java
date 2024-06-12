package FlexiPillsAssignment;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class Flx {
    WebDriver driver;
    JavascriptExecutor js;
    String token;
    ExtentTest test;
    ExtentReports report;
    WebDriverWait wait;

    @BeforeClass
    public void setupClass() {
        report = new ExtentReports(System.getProperty("user.dir") + "\\FlexiPillsReport.html");
        test = report.startTest("FlexiPillsTest");
    }

    @BeforeMethod
    public void setup() throws InterruptedException, IOException {
      //  System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");
        driver = new ChromeDriver();
        Assert.assertNotNull(driver, "WebDriver initialization failed");
        test.log(LogStatus.PASS, "Chrome driver has opened successfully");

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        driver.get("https://flexipill-ui-new-staging.vercel.app/");
        test.log(LogStatus.PASS, "Entered URL is valid");
        driver.manage().window().maximize();
        js = (JavascriptExecutor) driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void login() throws InterruptedException {
        driver.findElement(By.linkText("Login")).click();
        test.log(LogStatus.PASS, "Clicked on Login link");
        Thread.sleep(3000);
        driver.findElement(By.id(":r2:")).sendKeys("1111111111");
        test.log(LogStatus.PASS, "Entered phone number");
        Thread.sleep(3000);
        driver.findElement(By.xpath("//p[text()='Continue']")).click();
        test.log(LogStatus.PASS, "Clicked on Continue button");
        Thread.sleep(3000);
        driver.findElement(By.id(":r4:")).sendKeys("1");
        driver.findElement(By.id(":r5:")).sendKeys("1");
        driver.findElement(By.id(":r6:")).sendKeys("1");
        driver.findElement(By.id(":r7:")).sendKeys("1");
        test.log(LogStatus.PASS, "Entered OTP");
        Thread.sleep(3000);

        // Simulate API login to capture JWT token
        JSONObject requestBody = new JSONObject();
        requestBody.put("phone_number", "1111111111");
        requestBody.put("otp_code", "1111");

        Response response = RestAssured.given().header("Content-Type", "application/json")
                .body(requestBody.toString()).post("https://backendstaging.platinumrx.in/auth/login");

        token = response.jsonPath().getString("token");
        test.log(LogStatus.PASS, "JWT token received: " + token);

        // Assert the token is received and status code is 200
        Assert.assertNotNull(token, "Token should not be null after login");
        Assert.assertEquals(response.getStatusCode(), 200, "Login API response should be 200");
    }

    @Test(priority = 1, enabled = false)
    public void addToCart() throws InterruptedException {
        login();
        WebElement elem = driver.findElement(By.xpath("//p[text()='Top Categories']"));
        js.executeScript("arguments[0].scrollIntoView()", elem);
        test.log(LogStatus.PASS, "Scrolled to 'Top Categories'");

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/category/Heart Care']")));
        Thread.sleep(3000);

        driver.findElement(By.xpath("//a[@href='/category/Heart Care']")).click();
        test.log(LogStatus.PASS, "Clicked on Heart Care");

        Thread.sleep(3000);
        driver.findElement(By.xpath("(//span[text()='Add to Cart'])[1]")).click();
        test.log(LogStatus.PASS, "Clicked on Add to Cart button");

        // Add to cart API call
        JSONObject requestBody = new JSONObject();
        requestBody.put("increaseQuantityBy", 2);
        requestBody.put("drugCode", 1110806);

        Response response = RestAssured.given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token).body(requestBody.toString())
                .post("https://backendstaging.platinumrx.in/cart/addItem");

        // Log and assert the response
        test.log(LogStatus.PASS, "Add to Cart API response: " + response.getBody().asString());
        Assert.assertEquals(response.getStatusCode(), 200, "Add to Cart API response should be 200");
        Assert.assertTrue(response.getBody().asString().contains("success"), "Add to Cart response should contain 'success'");

        Thread.sleep(8000);
    }

    @Test(priority = 2, enabled = false)
    public void createOrder() throws InterruptedException {
        login();
        Thread.sleep(3000);

        driver.findElement(By.xpath("//button[@class='Header_cartButton__Giyrb']")).click();
        test.log(LogStatus.PASS, "Clicked on cart button");
        Thread.sleep(3000);

        WebElement dropdown = driver.findElement(By.cssSelector(".AddToCartDropdown_arrow__pFEjt"));
        dropdown.click();
        test.log(LogStatus.PASS, "Clicked on quantity dropdown");
        Thread.sleep(3000);

        List<WebElement> options = driver.findElements(By.xpath("//div[@class='quantity-select AddToCartDropdown_quantity-list__7edu0']"));

        for (WebElement option : options) {
            if (option.getText().equals("9")) {
                Thread.sleep(3000);
                option.click();
                test.log(LogStatus.PASS, "Selected quantity 9");
                break;
            }
        }

        Thread.sleep(3000);

        WebElement scrl = driver.findElement(By.xpath("//h4[text()='Contact Us']"));
        js.executeScript("arguments[0].scrollIntoView()", scrl);
        test.log(LogStatus.PASS, "Scrolled to 'Contact Us' section");
        Thread.sleep(3000);

        driver.findElement(By.xpath("(//input[@type='radio'])[1]")).click();
        test.log(LogStatus.PASS, "Selected payment method");
        Thread.sleep(3000);
        driver.findElement(By.xpath("//button[text()='Place Order']")).click();
        test.log(LogStatus.PASS, "Clicked on Place Order button");

        // Create order API call
        JSONObject requestBody = new JSONObject();
        requestBody.put("paymentType", "COD");
        requestBody.put("orderType", "SEARCH");
        requestBody.put("patientName", "test");
        requestBody.put("patientAddress", "test-block test-city test-state 577201");
        requestBody.put("patientMobileNumber", "9876543219");
        requestBody.put("patientAge", "23");
        requestBody.put("patientGender", "male");
        requestBody.put("pincode", 577201);
        requestBody.put("city", "test-city");
        requestBody.put("state", "test-state");

        Response response = RestAssured.given().header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token).body(requestBody.toString())
                .post("https://backendstaging.platinumrx.in/orders/initiateOrder");

        test.log(LogStatus.PASS, "Create Order API response: " + response.getBody().asString());
        Assert.assertEquals(response.getStatusCode(), 200, "Create Order API response should be 200");
        Assert.assertTrue(response.getBody().asString().contains("success"), "Create Order response should contain 'success'");

        Thread.sleep(10000);
    }

    @Test(priority = 3)
    public void loginWithInvalidOtp() throws InterruptedException {
     
        driver.findElement(By.linkText("Login")).click();
        test.log(LogStatus.PASS, "Clicked on Login link");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(":r2:"))).sendKeys("1111111111");
        test.log(LogStatus.PASS, "Entered phone number");

        driver.findElement(By.xpath("//p[text()='Continue']")).click();
        test.log(LogStatus.PASS, "Clicked on Continue button");

        Thread.sleep(3000);
        driver.findElement(By.id(":r4:")).sendKeys("1");
        driver.findElement(By.id(":r5:")).sendKeys("2");
        driver.findElement(By.id(":r6:")).sendKeys("3");
        driver.findElement(By.id(":r7:")).sendKeys("4");
        test.log(LogStatus.PASS, "Entered OTP");
        Thread.sleep(3000);
        driver.findElement(By.xpath("//p[text()='Submit OTP']")).click();
        test.log(LogStatus.PASS, "Clicked on Submit OTP button");

        boolean loginFailed = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'Invalid OTP')]"))).isDisplayed();
        Assert.assertTrue(loginFailed, "Login should fail with invalid OTP");
    }

    @Test(priority = 4)
    public void loginWithInvalidPhoneNumber() throws InterruptedException {
     
        driver.findElement(By.linkText("Login")).click();
        test.log(LogStatus.PASS, "Clicked on Login link");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(":r2:"))).sendKeys("112184148411");
        test.log(LogStatus.PASS, "Entered phone number");

        driver.findElement(By.xpath("//p[text()='Continue']")).click();
        test.log(LogStatus.PASS, "Clicked on Continue button");

        Thread.sleep(3000);
        driver.findElement(By.id(":r4:")).sendKeys("1");
        driver.findElement(By.id(":r5:")).sendKeys("1");
        driver.findElement(By.id(":r6:")).sendKeys("1");
        driver.findElement(By.id(":r7:")).sendKeys("1");
        test.log(LogStatus.PASS, "Entered OTP");
        Thread.sleep(3000);
        driver.findElement(By.xpath("//p[text()='Submit OTP']")).click();
        test.log(LogStatus.PASS, "Clicked on Submit OTP button");

        boolean loginFailed = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'Invalid phone number')]"))).isDisplayed();
        Assert.assertTrue(loginFailed, "Login should fail with invalid phone number");
    }

    @Test(priority = 5)
    public void loginWithBlankOtp() throws InterruptedException {
      
        driver.findElement(By.linkText("Login")).click();
        test.log(LogStatus.PASS, "Clicked on Login link");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(":r2:"))).sendKeys("1111111111");
        test.log(LogStatus.PASS, "Entered phone number");

        driver.findElement(By.xpath("//p[text()='Continue']")).click();
        test.log(LogStatus.PASS, "Clicked on Continue button");

        Thread.sleep(3000);
        driver.findElement(By.id(":r4:")).sendKeys("");
        driver.findElement(By.id(":r5:")).sendKeys("");
        driver.findElement(By.id(":r6:")).sendKeys("");
        driver.findElement(By.id(":r7:")).sendKeys("");
        test.log(LogStatus.PASS, "Entered OTP");
        Thread.sleep(3000);
        driver.findElement(By.xpath("//p[text()='Submit OTP']")).click();
        test.log(LogStatus.PASS, "Clicked on Submit OTP button");

        boolean loginFailed = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'OTP cannot be blank')]"))).isDisplayed();
        Assert.assertTrue(loginFailed, "Login should fail with blank OTP");
    }

    @Test(priority = 6)
    public void loginWithBlankPhoneNumber() throws InterruptedException {
       
        driver.findElement(By.linkText("Login")).click();
        test.log(LogStatus.PASS, "Clicked on Login link");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(":r2:"))).sendKeys("");
        test.log(LogStatus.PASS, "Entered phone number");

        driver.findElement(By.xpath("//p[text()='Continue']")).click();
        test.log(LogStatus.PASS, "Clicked on Continue button");

        Thread.sleep(3000);
        driver.findElement(By.id(":r4:")).sendKeys("1");
        driver.findElement(By.id(":r5:")).sendKeys("2");
        driver.findElement(By.id(":r6:")).sendKeys("3");
        driver.findElement(By.id(":r7:")).sendKeys("4");
        test.log(LogStatus.PASS, "Entered OTP");
        Thread.sleep(3000);
        driver.findElement(By.xpath("//p[text()='Submit OTP']")).click();
        test.log(LogStatus.PASS, "Clicked on Submit OTP button");

        boolean loginFailed = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'Phone number cannot be blank')]"))).isDisplayed();
        Assert.assertTrue(loginFailed, "Login should fail with blank phone number");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        report.endTest(test);
        report.flush();
    }
}


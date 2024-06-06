package assignment;
// Replace "your.package.name" with your actual package name

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class Flexipills {
    WebDriver driver;
    JavascriptExecutor js;
    String token;
    ExtentTest test;
    ExtentReports report;

    @BeforeClass
    public void setupClass() {
        report = new ExtentReports(System.getProperty("user.dir") + "\\FlexiPillsReport.html");
        test = report.startTest("FlexiPillsTest");
    }

    @BeforeMethod
    public void setup() throws InterruptedException, IOException, ParseException {
        driver = new ChromeDriver();
        test.log(LogStatus.PASS, "Chrome driver has opened successfully");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get("https://flexipill-ui-new-staging.vercel.app/");
        test.log(LogStatus.PASS, "Entered URL is valid");
        driver.manage().window().maximize();
        js = (JavascriptExecutor) driver;

        // Perform API login to get token
        token = loginApi("1111111111", "1111");

        // Login to the website
        login();
    }

    public String loginApi(String phoneNumber, String otpCode) throws IOException, ParseException {
        String loginUrl = "https://backendstaging.platinumrx.in/auth/login";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(loginUrl);

        JSONObject json = new JSONObject();
        json.put("phone_number", phoneNumber);
        json.put("otp_code", otpCode);

        StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");

        CloseableHttpResponse response = client.execute(httpPost);
        String responseBody = EntityUtils.toString(response.getEntity());
        JSONObject responseJson = new JSONObject(responseBody);

        Assert.assertEquals(response.getCode(), 200);
        client.close();

        return responseJson.getString("token");
    }

    @Test(priority = 1)
    public void login() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
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
    }

    @Test(priority = 2)
    public void addToCart() throws InterruptedException, IOException, ParseException {
        WebElement elem = driver.findElement(By.xpath("//p[text()='The PlatinumRx Advantage']"));
        js.executeScript("arguments[0].scrollIntoView()", elem);
        test.log(LogStatus.PASS, "Scrolled to 'The PlatinumRx Advantage' section");
        Thread.sleep(7000);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//span[text()='Add to Cart'])[5]")));
        driver.findElement(By.xpath("(//span[text()='Add to Cart'])[5]")).click();
        test.log(LogStatus.PASS, "Clicked on Add to Cart button");

        addToCartApi(1110806, 2);
    }

    public void addToCartApi(int drugCode, int increaseQuantityBy) throws IOException, ParseException {
        String addToCartUrl = "https://backendstaging.platinumrx.in/cart/addItem";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(addToCartUrl);

        JSONObject json = new JSONObject();
        json.put("increaseQuantityBy", String.valueOf(increaseQuantityBy));
        json.put("drugCode", drugCode);

        StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + token);

        CloseableHttpResponse response = client.execute(httpPost);
        String responseBody = EntityUtils.toString(response.getEntity());
        JSONObject responseJson = new JSONObject(responseBody);

        Assert.assertEquals(response.getCode(), 200);
        Assert.assertTrue(responseJson.getBoolean("success"));
        test.log(LogStatus.PASS, "Added item to cart via API");
        client.close();
    }

    @Test(priority = 3)
    public void createOrder() throws InterruptedException, IOException, ParseException {
        driver.findElement(By.xpath("//button[@class='Header_cartButton__Giyrb']"))
                .click();
        test.log(LogStatus.PASS, "Clicked on cart button");

        WebElement dropdown = driver.findElement(By.cssSelector(".AddToCartDropdown_arrow__pFEjt"));
        dropdown.click();
        test.log(LogStatus.PASS, "Clicked on quantity dropdown");

        List<WebElement> options = driver.findElements(
                By.xpath("//div[@class='quantity-select AddToCartDropdown_quantity-list__7edu0']"));

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

        driver.findElement(By.xpath("(//input[@type='radio'])[1]"))
                .click();
        test.log(LogStatus.PASS, "Selected payment method");
        Thread.sleep(3000);
        driver.findElement(By.xpath("//button[text()='Place Order']")).click();
        test.log(LogStatus.PASS, "Clicked on Place Order button");

        createOrderApi("COD", "SEARCH", "test", "test-block test-city test-state 577201", "9876543219", 23, "male",
                577201, "test-city", "test-state");
    }

    public void createOrderApi(String paymentType, String orderType, String patientName, String patientAddress,
            String patientMobileNumber, int patientAge, String patientGender, int pincode, String city, String state)
            throws IOException, ParseException {
        String createOrderUrl = "https://backendstaging.platinumrx.in/orders/initiateOrder";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(createOrderUrl);

        JSONObject json = new JSONObject();
        json.put("paymentType", paymentType);
        json.put("orderType", orderType);
        json.put("patientName", patientName);
        json.put("patientAddress", patientAddress);
        json.put("patientMobileNumber", patientMobileNumber);
        json.put("patientAge", String.valueOf(patientAge));
        json.put("patientGender", patientGender);
        json.put("pincode", pincode);
        json.put("city", city);
        json.put("state", state);

        StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + token);

        CloseableHttpResponse response = client.execute(httpPost);
        String responseBody = EntityUtils.toString(response.getEntity());
        JSONObject responseJson = new JSONObject(responseBody);

        Assert.assertEquals(response.getCode(), 200);
        Assert.assertTrue(responseJson.getBoolean("success"));
        test.log(LogStatus.PASS, "Created order via API");
        client.close();
    }

    // Negative Scenarios

    @Test(priority = 4)
    public void testInvalidLogin() throws IOException, ParseException {
        String loginUrl = "https://backendstaging.platinumrx.in/auth/login";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(loginUrl);

        JSONObject json = new JSONObject();
        json.put("phone_number", "1111111111");
        json.put("otp_code", "wrong");

        StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");

        CloseableHttpResponse response = client.execute(httpPost);

        // For negative cases, assert failure response code
        Assert.assertNotEquals(response.getCode(), 200);
        // For negative cases, assert expected failure code (401)
        Assert.assertEquals(response.getCode(), 401);

        client.close();
    }

    @Test(priority = 5)
    public void testInvalidAddToCart() throws IOException, ParseException {
        String addToCartUrl = "https://backendstaging.platinumrx.in/cart/addItem";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(addToCartUrl);

        JSONObject json = new JSONObject();
        json.put("increaseQuantityBy", "2");
        json.put("drugCode", "invalid");

        StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + "invalid_token"); // Making the token invalid

        CloseableHttpResponse response = client.execute(httpPost);

        // For negative cases, assert failure response code
        Assert.assertNotEquals(response.getCode(), 200);

        client.close();
    }

    @Test(priority = 6)
    public void testInvalidCreateOrder() throws IOException, ParseException {
        String createOrderUrl = "https://backendstaging.platinumrx.in/orders/initiateOrder";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(createOrderUrl);

        JSONObject json = new JSONObject();
        json.put("paymentType", "COD");
        json.put("orderType", "SEARCH");
        json.put("patientName", "");
        json.put("patientAddress", "");
        json.put("patientMobileNumber", "");
        json.put("patientAge", "");
        json.put("patientGender", "");
        json.put("pincode", 0);
        json.put("city", "");
        json.put("state", "");

        StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + "invalid_token"); // Making the token invalid

        CloseableHttpResponse response = client.execute(httpPost);

        // For negative cases, assert failure response code
        Assert.assertNotEquals(response.getCode(), 200);

        client.close();
    }

    @AfterMethod
    public void tearDown() {
        driver.close();
        test.log(LogStatus.PASS, "Browser closed successfully");
    }

    @AfterClass
    public void last() {
        report.endTest(test);
        report.flush();
    }
}


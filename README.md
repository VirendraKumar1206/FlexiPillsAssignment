# FlexiPillsAssignment
steps to run test---

how to run the code
1. Prerequisites
2. Steps to Run
1. Clone the Repository
2. Navigate to Project Directory
3. Update ChromeDriver Path
4. Run the Tests
5. View Test Results
3. Test Scenarios Covered
4. Important Notes
1. Prerequisites
Before running the code, ensure the following are installed and configured:
- Java Development Kit (JDK): Ensure JDK 8 or later is installed on your system.
- Maven: Make sure Maven is installed to manage project dependencies and execute the
tests.
- ChromeDriver: Download the appropriate ChromeDriver version compatible with your
Chrome browser and ensure it is accessible in your system's PATH.
2. Steps to Run
Clone the Repository
First, clone the repository containing the provided Java code to your local machine.
Navigate to Project Directory
Open a terminal or command prompt and change the directory to the project folder where
the Java code is located.
Update ChromeDriver Path
Open the `FlexiPillsAssign.java` file and update the path to ChromeDriver. Locate the
following line of code:
In java
System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");
```
Replace `"path/to/chromedriver"` with the actual path where your ChromeDriver executable
is located.
Run the Tests
Execute the following Maven command in the terminal to run the tests:
```bash
mvn test
```
Maven will compile the code, download dependencies, and execute the test methods
defined in the Java file.
View Test Results
After running the tests, review the test results in the terminal. Additionally, detailed test
reports will be generated in the `target/surefire-reports` directory. Open the `index.html` file
in this directory using a web browser to view comprehensive test results.
3. Test Scenarios Covered
The provided code includes test scenarios for:
- User login with valid credentials.
- Adding an item to the cart.
- Creating an order with valid details.
- Negative scenarios such as invalid login, invalid add-to-cart, and invalid order creation are
also covered.
Important Notes
- Ensure that all prerequisites are met and the ChromeDriver path is correctly set before
running the tests.
- Make sure the URLs and API endpoints referenced in the code are accessible and
functional.
- Review the test output and reports for any errors or failures.

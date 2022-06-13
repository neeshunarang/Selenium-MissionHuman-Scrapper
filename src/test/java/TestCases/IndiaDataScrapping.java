package TestCases;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import Utilities.XLUtils;
import io.github.bonigarcia.wdm.WebDriverManager;

public class IndiaDataScrapping {
	WebDriver driver = null;
	int page = 1;
	String excelPath = "Excel\\TestData2.xlsx";
	String sheetName = "Data";
	int exlRow = 1;

	@BeforeMethod
	public void SetUp() throws IOException, InterruptedException {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.get("https://live.angelnextdoor.in/resources.php");
		driver.manage().window().maximize();

		// ****Filter Data
		String strCity = XLUtils.getCellData(excelPath, "FilterData", 0, 1);// "Delhi";
		String category = XLUtils.getCellData(excelPath, "FilterData", 1, 1);// "Plasma";
		String availability = XLUtils.getCellData(excelPath, "FilterData", 2, 1);// Out of Stock
		// Enter your City

		driver.findElement(By.id("select2-search_city-container")).click();
		driver.findElement(By.className("select2-search__field")).sendKeys(strCity);

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//ul[@id='select2-search_city-results']/li[text()='" + strCity + "']")));
		List<WebElement> cities = driver.findElements(By.xpath("//ul[@id='select2-search_city-results']/li"));
		for (WebElement city : cities) {
			if (city.getText().equalsIgnoreCase(strCity)) {
				city.click();
				break;
			}
		}
		// Select Category
		driver.findElement(By.xpath("//span[@title='Select category']")).click();
		driver.findElement(By.xpath("//ul[@id='select2-search_category-results']/li[text()='" + category + "']"))
				.click();

		// Select Status
		Select searchAvl = new Select(driver.findElement(By.id("search_availability")));
		List<WebElement> options = searchAvl.getOptions();
		for (WebElement option : options) {
			if (option.getText().equalsIgnoreCase(availability)) {
				option.click();
				break;
			}
		}

		// Click on Search button
		driver.findElement(By.xpath("//input[@value='SEARCH']")).click();

		XLUtils.setCellData(excelPath, sheetName, 0, 0, "Hospital Name");
		XLUtils.setCellData(excelPath, sheetName, 0, 1, "Category");
		XLUtils.setCellData(excelPath, sheetName, 0, 2, "Address");
		XLUtils.setCellData(excelPath, sheetName, 0, 3, "Contact");
		XLUtils.setCellData(excelPath, sheetName, 0, 4, "Status");
	}

	@Test
	public void ScrapData() throws IOException {
		int rows = driver.findElements(By.xpath("//tbody/tr")).size();// total rows/hospital in current page
		String parent = driver.getWindowHandle();
		for (int r = 1; r <= rows; r++) {
			driver.findElement(By.xpath("//tbody/tr[" + r + "]/td/a")).click();// Click on Hospital Name
			Set<String> allwindows = driver.getWindowHandles();
			for (String child : allwindows) {
				if (!child.equalsIgnoreCase(parent)) {
					driver.switchTo().window(child);
					XLUtils.setCellData(excelPath, sheetName, exlRow, 0,
							driver.findElement(By.tagName("h1")).getText());// Hospital Name
					XLUtils.setCellData(excelPath, sheetName, exlRow, 1,
							driver.findElement(By.className("cat_info")).getText());// Category
					XLUtils.setCellData(excelPath, sheetName, exlRow, 2,
							driver.findElement(By.className("address_info")).getText());// Address
					XLUtils.setCellData(excelPath, sheetName, exlRow, 3,
							driver.findElement(By.className("phone_info")).getText());// Phone No
					XLUtils.setCellData(excelPath, sheetName, exlRow, 4,
							driver.findElement(By.className("cls_availability")).getText());// Availability
					driver.close();
				}
			}
			driver.switchTo().window(parent);
			exlRow++;
		}

		try {
			if (driver.findElement(By.xpath("//ul//li/a[text()='Next']")).isDisplayed())
				driver.findElement(By.xpath("//ul//li/a[text()='Next']")).click();
		} catch (NoSuchElementException e) {
			return;
		}
		ScrapData();
	}

	@AfterTest
	public void TeatDown() throws IOException {
		XLUtils.wbClose();
		driver.quit();
	}
}

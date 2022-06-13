package TestCases;

import java.awt.AWTException;
import java.io.IOException;
import java.time.Duration;

import org.apache.poi.ss.usermodel.CellType;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import Utilities.XLUtils;
import io.github.bonigarcia.wdm.WebDriverManager;

public class WestBengalDataScrapping {
	WebDriver driver = null;
	String baseUrl;
	String excelPath;
	String sheetName;
	int count = 1;
	String areas;
	JavascriptExecutor js ;
	@BeforeMethod
	public void SetUp() throws IOException {
		baseUrl = "https://westbengal.covidsafe.in/";
		excelPath = "Excel\\TestData.xlsx";
		sheetName = "Sheet1";
		areas="All Areas";//Birbhum,All Areas,Darjeeling
		
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
		driver.manage().window().maximize();
		driver.get(baseUrl);
		js= (JavascriptExecutor) driver;
		
		//Filter Data
		driver.findElement(By.id("searchQueryInput")).sendKeys("");//esi
		driver.findElement(By.xpath("//div[contains(@class,'dropdown')]/button")).click();
		
		driver.findElement(By.linkText(areas)).click();
	}

	@Test
	public void ScrappingData() throws IOException, InterruptedException, AWTException {
		
		int rows=driver.findElements(By.xpath("//tr[@style='cursor: pointer;']")).size();
		if (count==1)
		{
			XLUtils.setCellData(excelPath, sheetName, 0, 0,"Hospital Name");
			XLUtils.setCellData(excelPath, sheetName, 0, 1,"Without Oxygen");
			XLUtils.setCellData(excelPath, sheetName, 0, 2,"With Oxygen" );
			XLUtils.setCellData(excelPath, sheetName, 0, 3,"ICU Without Ventilator");
			XLUtils.setCellData(excelPath, sheetName, 0, 4,"ICU With Ventilator");
			XLUtils.setCellData(excelPath, sheetName, 0, 5,"Last Updated");
			XLUtils.setCellData(excelPath, sheetName, 0, 6,"Phone Number");
			XLUtils.setCellData(excelPath, sheetName, 0, 7,"Pin Code");
			XLUtils.setCellData(excelPath, sheetName, 0, 8,"Address");
			XLUtils.setCellData(excelPath, sheetName, 0, 9,"In-Charge");
		}
		for (int i = count; i <=rows; i++) {
			
			String tr = "//tr["+i+"]";
					
			XLUtils.setCellData(excelPath, sheetName, i, 0,driver.findElement(By.xpath(tr + "//strong")).getText());//Hospital Name	
			XLUtils.setCellData(excelPath, sheetName, i, 1,driver.findElement(By.xpath(tr + "//td[2]")).getText());//Without Oxygen
			XLUtils.setCellData(excelPath, sheetName, i, 2,driver.findElement(By.xpath(tr + "//td[3]")).getText());//With Oxygen
			XLUtils.setCellData(excelPath, sheetName, i,3,driver.findElement(By.xpath(tr + "//td[4]")).getText());//ICU without Ventilator
			XLUtils.setCellData(excelPath, sheetName, i, 4,driver.findElement(By.xpath(tr + "//td[5]")).getText());//ICU with Ventilator
						
			//Click + button to get more info about hospital
			WebElement moreInfoBtn = driver.findElement(By.xpath(tr + "//button[contains(@class,'p-0')]"));
			js.executeScript("arguments[0].scrollIntoView()", moreInfoBtn);
			js.executeScript("arguments[0].click();", moreInfoBtn);
			
			String brdBotton = "//tr[@class='border-bottom']";
			
			XLUtils.setCellData(excelPath, sheetName, i, 5,driver.findElement(By.xpath(brdBotton+"//p[1]//span")).getText().replace("Last updated ", ""));//Last updated
			XLUtils.setCellData(excelPath, sheetName, i, 6,driver.findElement(By.xpath(brdBotton+"//p[2]//span")).getText().replace("Phone:", ""));//Phone No
			XLUtils.setCellData(excelPath, sheetName, i, 7,driver.findElement(By.xpath(brdBotton+"//p[3]/span")).getText().replace("Pincode:", ""));//PinCode
			XLUtils.setCellData(excelPath, sheetName, i, 8,driver.findElement(By.xpath(brdBotton+"//p[4]/span")).getText().replace("Address:", ""));//Address
			XLUtils.setCellData(excelPath, sheetName, i, 9,driver.findElement(By.xpath(brdBotton+"//p[5]/span")).getText().replace("In-charge:", "").replace("-", ""));//In-Charge
			
			js.executeScript("arguments[0].click();", moreInfoBtn);
			
		}
		//Storing the value of rows which is already printed
		count = rows;
		
		//Once reach to last page, close the workbook and return
		if (isDisplayedLoadPage() == false) {
			XLUtils.wbClose();
			return;
		}
		
		//Making it recursive to print all newly loaded rows 
		ScrappingData();

	}
	
	//Method to check Load page button is visible or not
	public boolean isDisplayedLoadPage() {
		try {
			WebElement loadPage = driver.findElement(By.xpath("//button[@class='btn btn-primary']"));
			boolean isPage = loadPage.isDisplayed();
			js.executeScript("arguments[0].scrollIntoView()", loadPage);
			js.executeScript("arguments[0].click();", loadPage);
			return isPage;
		} catch (NoSuchElementException e) {
			return false;
		}

	}

	@AfterMethod
	public void TearDown() {
		System.out.println("Web Scrapping is done successfully");
		//driver.close();
		driver.quit();
	}
}

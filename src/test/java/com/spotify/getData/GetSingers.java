package com.spotify.getData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

public class GetSingers {
	@Test
	public void getData() throws FileNotFoundException, IOException, InterruptedException {
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
		driver.get("https://open.spotify.com/");

		driver.findElement(By.xpath("//span[contains(text(),'Log in')]")).click();
		Properties prop = new Properties();
		FileInputStream fis = new FileInputStream("./configAppData/spotify_common_data.properties");
		prop.load(fis);
		Thread.sleep(3000);
		String em = prop.getProperty("email");
		driver.findElement(By.xpath("//input[@id='username']")).sendKeys(em, Keys.ENTER);
		Thread.sleep(10000);

		driver.findElement(By.xpath("//input[@placeholder='What do you want to play?']")).sendKeys("Artists",
				Keys.ENTER);
		WebElement artists = driver.findElement(By.xpath("//a[contains(text(),'Albums')]"));

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		wait.until(ExpectedConditions.visibilityOf(artists));

		Actions actions = new Actions(driver);
		actions.moveToElement(artists).perform();

		// Get singer name from excel

		FileInputStream fis1 = new FileInputStream("./testData/spotify.xlsx");
		Workbook wb = WorkbookFactory.create(fis1);

		FileInputStream fis2 = new FileInputStream("./testData/Results.xlsx");
		Workbook wb2 = WorkbookFactory.create(fis2);
//Get each singer name
		for (int i = 4; i < 7; i++) {

			String singer = wb.getSheet("search").getRow(i).getCell(0).getStringCellValue();
			driver.findElement(By.xpath("//span[contains(text(),'" + singer + "')]")).click();

			Thread.sleep(2000);

			List<WebElement> songNames = driver
					.findElements(By.xpath("(//a[@data-testid='internal-track-link'])[position()<=5]"));
			List<WebElement> songViews = driver.findElements(By
					.xpath("//div[@data-testid='tracklist-row']/descendant::div[@aria-colindex='3']/descendant::div"));

			Sheet sh2 = wb2.getSheet(singer);
			if (sh2 == null) {
				sh2 = wb2.createSheet(singer);
			}
//Get each song name and views and write to Excel
			for (int j = 0; j < songNames.size(); j++) {

				String song = songNames.get(j).getText();
				String view = songViews.get(j).getText();

				Row row = sh2.getRow(j);
				if (row == null) {
					row = sh2.createRow(j);
				}

				row.createCell(0).setCellValue(song);

				row.createCell(1).setCellValue(view);
			}

			driver.navigate().back();
		}

		FileOutputStream fos = new FileOutputStream("./testData/Results.xlsx");
		wb2.write(fos);

		fos.close();
		wb.close();
		wb2.close();
		fis1.close();
		fis2.close();

		// Logout from Spotify Application
		WebElement profile = driver.findElement(By.xpath("//button[@data-testid='user-widget-link']"));
		Actions action = new Actions(driver);
		action.moveToElement(profile).click().build().perform();

		WebElement logoutBtn = driver
				.findElement(By.xpath("//button[@data-testid='user-widget-dropdown-logout']/descendant::span"));
		Actions a = new Actions(driver);
		a.click(logoutBtn).build().perform();

		// Close the Browser
		driver.quit();

		System.out.println("Data----->to--->Excel--------///Successful");

	}

}


/*
 * @author: Sachin Saligram
 * @Description: This java file performs selenium testing to validate the performance
 * 				of our Alexa enabled bot 'Scrumster'.
 * @Updated: 11/14/2017
 * 
 * @NOTE: Please make sure to change the date in line ___. This is due to limitations in accessing
 * 		information via calendar.google.com.
*/

package selenium.tests;

import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.ChromeDriverManager;

public class WebTest
{
	private static WebDriver driver;
	
	@BeforeClass
	public static void setUp() throws Exception 
	{
		ChromeDriverManager.getInstance().setup();
		driver = new ChromeDriver();
	}
	
	@AfterClass
	public static void  tearDown() throws Exception
	{
		driver.close();
		driver.quit();
	}
	
	private String convertResponseToString(HttpResponse response) throws IOException {
        InputStream responseStream = response.getEntity().getContent();
        Scanner scanner = new Scanner(responseStream, "UTF-8");
        String responseString = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return responseString;
    }
	
	@Test
	public void jira() throws Exception
	{
		driver.get("https://scrumster.atlassian.net/secure/RapidBoard.jspa?rapidView=1");
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-submit")));
		WebElement signin = driver.findElement(By.id("login-submit"));

		// Find email and type in user email info.
		WebElement email = driver.findElement(By.id("username"));
		email.sendKeys("scrumuser2017@gmail.com");
		// Click button
		signin.click();
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
		
		// Find password and type in user password info.		
		WebElement pw = driver.findElement(By.id("password"));
		pw.sendKeys("scrumster2017");
		// Click button
		signin.click();
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='ghx-pool']/div[2]/ul/li[1]")));
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = null;		
        httpPost = new HttpPost("http://localhost:8080" + "/scrum/task/" + "30" + "/" + "21");
        httpClient.execute(httpPost);
        
        Thread.sleep(1000);
        driver.navigate().refresh();
        Thread.sleep(1000);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='ghx-pool']/div[2]/ul/li[1]")));
        Thread.sleep(2000);  
	}
	
	
	@Test
	public void jira_summary() throws Exception
	{
		driver.get("https://scrumster.atlassian.net/secure/RapidBoard.jspa?rapidView=1");
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-submit")));
		WebElement signin = driver.findElement(By.id("login-submit"));

		// Find email and type in user email info.
		WebElement email = driver.findElement(By.id("username"));
		email.sendKeys("scrumuser2017@gmail.com");
		// Click button
		signin.click();
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
		
		// Find password and type in user password info.		
		WebElement pw = driver.findElement(By.id("password"));
		pw.sendKeys("scrumster2017");
		// Click button
		signin.click();
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='ghx-pool']/div[2]/ul/li[1]")));
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet("http://localhost:8080" + "/scrum/summary");
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        System.out.println(httpResponse);
        String response1 = convertResponseToString(httpResponse);
        httpResponse.close();
        
        CloseableHttpClient httpClient1 = HttpClients.createDefault();
		HttpPost httpPost1 = null;		
        httpPost1 = new HttpPost("http://localhost:8080" + "/scrum/task/" + "22" + "/" + "31");
        httpClient1.execute(httpPost1);
        
        Thread.sleep(1000);
        driver.navigate().refresh();
        Thread.sleep(2000);

        HttpGet httpGet1 = new HttpGet("http://localhost:8080" + "/scrum/summary");
        httpResponse = httpClient.execute(httpGet1);
        String response2 = convertResponseToString(httpResponse);
        assertNotEquals(response1, response2); 
	}
	
	@Test
	public void google() throws Exception
	{
		driver.get("https://calendar.google.com/calendar/render#main_7");

		// Enter email
		driver.findElement(By.id("identifierId")).sendKeys("sachin@ouruse.com");
        driver.findElement(By.id("identifierNext")).click();

        WebDriverWait wait = new WebDriverWait(driver, 30);
        
        // Enter password
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("password"))); 
        driver.findElement(By.name("password")).sendKeys("scrumster2017");
        driver.findElement(By.id("passwordNext")).click();
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='mainlogo']")));
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = null;
		
		// Change date to any date in the current week. Format should be YYYY-MM-dd of type String.
        httpPost = new HttpPost("http://localhost:8080" + "/calendar/" + "2017-11-17");
      	httpClient.execute(httpPost);
		
      	Thread.sleep(1000);
		driver.navigate().refresh();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='mainlogo']")));
		Thread.sleep(2000);
	
	}
	
}

import junit.framework.ComparisonFailure;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;

import static java.lang.Thread.sleep;
import static junit.framework.TestCase.assertEquals;

public class InsuranceTest {
    WebDriver driver;
    boolean taskFlag = true;
    String website;
    String xpath;

    @Before
    public void setUp() throws Exception {
        System.setProperty("webdriver.chrome.driver", "drv/chromedriver.exe");
    }

    @Test
    public void testRgs() throws Exception{
        System.out.println("Task 1:");
        driver = new ChromeDriver();
        final Wait<WebDriver> wait = new WebDriverWait(driver, 5, 1000);
        driver.manage().window().maximize();

        website = "http://www.rgs.ru";
        driver.get(website);

        //Кнопка "Меню", кнопка "ДМС"
        //???1 Нужно ли прикручивать ожидание ко всем моментам, где хоть что-то грузится и открывается? У меня быстрый интернет и я нигде не ловлю ошибку, но возможно при медленном интернете они будут появляться?
        //???2 На этом и следующем этапе программа периодически подтормаживает, браузер пишет "Ожидание api.flocktory...". Я верно понимаю, что это косяк сайта и я ничего не могу тут сделать?
//        xpath = "//a[@class='collapsed'][contains(text(), 'Меню' )]"; //???3 почему-то не работает, хотя в F12 определяется однозначно
        xpath = "(//a[@href='#'][contains(text(), 'Меню' )])[2]";
        driver.findElement(By.xpath(xpath)).click();
        xpath = "//a[contains(text(), 'ДМС' )]";
        driver.findElement(By.xpath(xpath)).click();

        //Ожидание загрузки, проверка правильности страницы, кнопка "Отправить заявку"
//        xpath = "//h1[@class='content-document-header']";
        WebElement element = driver.findElement(By.className("content-document-header"));
        wait.until(ExpectedConditions.visibilityOf(element));
        try {
            assertEquals("ДМС — добровольное медицинское страхование", element.getText());
        } catch (ComparisonFailure e) {
            System.err.println("Error: wrong page");
            taskFlag = false;
            tearDown();
        }
        xpath = "//a[contains(text(), 'Отправить заявку')]";
        driver.findElement(By.xpath(xpath)).click();

        //Ожидание загрузки, проверка появления формы
        xpath = "//b[@data-bind='text: options.title']";
        element = driver.findElement(By.xpath(xpath));
        wait.until(ExpectedConditions.visibilityOf(element));
        try {
            assertEquals("Заявка на добровольное медицинское страхование", element.getText());
        } catch (ComparisonFailure e) {
            System.err.println("Error: wrong page");
            taskFlag = false;
            tearDown();
        }

        //Заполнение формы (select и checkbox отдельно)
        xpath = "//div[@class='row']//input[contains(@class, 'form-control')] | //textarea[contains(@class, 'form-control')]";
        List<WebElement> elements = driver.findElements(By.xpath(xpath));
        List<String> data = new ArrayList<String>(Arrays.asList("Фамилия", "Имя", "Отчество", "7777777777\n", "qwertyqwerty", "06102019\n", "Комментарий"));
        Iterator<String> iterator = data.iterator();
        for (WebElement webElement : elements) {
            if (iterator.hasNext()) {
                webElement.sendKeys(iterator.next());
            }
        }
        //???4 Периодически регион почему-то не выбиратеся, что приводит к ошибке
        //Я надеялся, что wait решит эту проблему, но она всё равно присутствует
        Select region;
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("Region")));
        try {
            region = new Select(driver.findElement(By.name("Region")));
            region.selectByVisibleText("Москва");
        } catch (org.openqa.selenium.NoSuchElementException elemEx) {
            System.err.println("Error: problems with region selection");
            tearDown();
            throw new Error();
        }
        xpath = "//input[@class='checkbox']";
        driver.findElement(By.xpath(xpath)).click();

        //Проверка заполнения формы
        data.set(3, "+7 (777) 777-77-77");
        data.set(5, "06.10.2019");
        iterator = data.iterator();
        for (WebElement webElement : elements) {
            if (iterator.hasNext()) {
                if (!webElement.getAttribute("value").equals(iterator.next())) {
                    System.err.println("Error: wrong text " + webElement.getAttribute("value"));
                }
            }
        }
        if (!region.getAllSelectedOptions().get(0).getText().equals("Москва")) {
            System.err.println("Error: wrong region " + region.getAllSelectedOptions().get(0).getText());
        }
        if (!driver.findElement(By.xpath(xpath)).isSelected()) {
            System.err.println("Error: I checked if ckeckbox is checked and checkbox is not checked.");
        }

        //Кнопка "Отправить"
//        xpath = "//button[@id='button-m']";
        driver.findElement(By.id("button-m")).click();

        //Проверка ошибочности Эл.почты
        xpath = "//span[@class='validation-error-text']";
        try {
            assertEquals("Введите адрес электронной почты",
                    driver.findElement(By.xpath(xpath)).getAttribute("innerText"));
            //???5 по неясной причине getText возвращал пустую строку
        } catch (ComparisonFailure e) {
            System.err.println("Error: task 1 failed wrongly\n");
            taskFlag = false;
            tearDown();
        }

        if (taskFlag) {
            System.out.println("Task 1 failed successfully\n");
        }
//        tearDown();
    }

    @Test
    public void testSber() throws Exception{
        System.out.println("Task 2:");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        website = "http://www.sberbank.ru/ru/person";
        driver.get(website);

        //Кнопка выбора региона
        xpath = "//div[@class='hd-ft-region__title'][1]//span";
        driver.findElement(By.xpath(xpath)).click();

        //Поисковая строка
        xpath = "//input[@class='kit-input__control'][@type='search']";
        driver.findElement(By.xpath(xpath)).sendKeys("Нижегородская область\n");

        //Проверка правильности региона
        xpath = "//div[@class='hd-ft-region__title'][1]//span";
        try {
            assertEquals("Нижегородская область", driver.findElement(By.xpath(xpath)).getText());
        } catch (ComparisonFailure e) {
            System.err.println("Error: wrong region");
            tearDown();
        }

        //Прокрутка до конца страницы
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");

        //Проверка наличия соц. сетей
        xpath = "//ul[@class='footer__social']";
        if (driver.findElements(By.xpath(xpath)).isEmpty()) {
            System.err.println("Error: no social network icons in footer");
        }

        System.out.println("Task 2 completed successfully");
//        tearDown();
    }

    @After
    public void tearDown() throws Exception {

        sleep(1000);
        driver.quit();
    }
}
package io.github.lucysuslova;

import com.codeborne.selenide.AssertionMode;
import com.codeborne.selenide.Configuration;
import io.qameta.allure.TmsLink;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Selenide.*;

public class GoogleTestNGTest {

    @BeforeMethod
    public void setUp() {
        Configuration.timeout = 1;
        Configuration.assertionMode = AssertionMode.STRICT;
        open("https://google.com/ncr");
    }

    @Test
    @TmsLink("1234") //1234 - ID in TestRail. @TmsLinks and @DataProvider also supported
    public void successfulMethod() {
        $(By.name("q"))
                .setValue("selenide")
                .pressEnter();
        $$("#res .g")
                .shouldHave(sizeGreaterThan(5));
    }
}

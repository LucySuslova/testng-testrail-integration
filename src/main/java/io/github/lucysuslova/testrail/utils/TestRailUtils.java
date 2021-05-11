package io.github.lucysuslova.testrail.utils;

import io.qameta.allure.TmsLink;
import io.qameta.allure.TmsLinks;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestRailUtils {

    public static Boolean isMethodValidForTestRail(Method method) {
        return method.getAnnotation(Test.class) != null
                && (null != method.getAnnotation(TmsLink.class) || null != method.getAnnotation(TmsLinks.class))
                && null == method.getAnnotation(Ignore.class);
    }

    public static String[] getMultipleLinks(Method method) {
        TmsLink[] links = method.getAnnotation(TmsLinks.class).value();
        return Arrays.stream(links)
                .map(l -> l.value())
                .toArray(String[]::new);
    }

    public static int[] getCaseIds(Method method) {
        return Stream.of(method.isAnnotationPresent(TmsLinks.class)
                ? getMultipleLinks(method)
                : new String[]{method.getAnnotation(TmsLink.class).value()})
                .mapToInt(Integer::parseInt).toArray();
    }

}

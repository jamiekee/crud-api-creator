package apiCreator.utils;

public class StringFormatter {

    public static String fs(String... strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings)
            stringBuilder.append(string);
        return stringBuilder.toString();
    }

}

package demo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jayvee on 2014/9/24.
 */
public class test {
    public static void main(String[] a) {
        test t = new test();
        t.test1();
    }

    public void test1() {
        String docSTR = "[{\"appendId\":217665558528,\"content\":\"客服梁静茹态度很好5分\",\"dayAfterConfirm\":0,\"photos\":[],\"reply\":{\"content\":\"\"感谢您的5分好评！系统已自动登记送您1年延保服务哦！赠人玫瑰手有余香您的好评是对我们最大的支持与鼓励也将是我们不断前进的动力，期待再次合作！O(∩_∩)O\"},\"show\":true,\"vicious\":\"\"}],\"[{\"appendId\":217665558528,\"content\":\"客服梁静茹态度很好5分\",\"dayAfterConfirm\":0,\"photos\":[],\"reply\":{\"content\":\"\"感谢您的5分好评！系统已自动登记送您1年延保服务哦！赠人玫瑰手有余香您的好评是对我们最大的支持与鼓励也将是我们不断前进的动力，期待再次合作！O(∩_∩)O\"},\"show\":true,\"vicious\":\"\"}],\"";
        Matcher content_matcher = Pattern.compile("\"content\":.*?,\"").matcher(docSTR);
        StringBuffer strBuffer = new StringBuffer();
        while (content_matcher.find()) {
            String content = content_matcher.group();
            Matcher m1 = Pattern.compile("\"").matcher(content);
            int count = 0;
            while (m1.find()) {
                count++;
            }
            if (count > 5) {
                System.out.println(content);
                StringBuffer sb = new StringBuffer();
                Matcher q = Pattern.compile("\"").matcher(content);
                for (int i = 0; i < count; i++) {
                    if (q.find() && i > 2 && i < (count - 2)) {
                        q.appendReplacement(sb, "'");
//                            System.out.println("替换后的字符串:" + sb);
                    }
                }
                q.appendTail(sb);
                System.out.println("最终字符串：" + sb);
                content_matcher.appendReplacement(strBuffer, sb.toString());
//                content_matcher.appendTail(strBuffer);
//                docSTR = strBuffer.toString();
                System.out.println(docSTR);
            }
        }
        content_matcher.appendTail(strBuffer);
        docSTR = strBuffer.toString();
        System.out.println(docSTR);
    }

    public void test2() {
        String text = "jiminwoiewd";
        Matcher m = Pattern.compile("i").matcher(text);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; m.find(); i++) {
            m.appendReplacement(sb, i + "");
            System.out.println(sb);
        }
        m.appendTail(sb);
    }
}

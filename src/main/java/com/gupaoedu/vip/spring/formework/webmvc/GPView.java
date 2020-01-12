package com.gupaoedu.vip.spring.formework.webmvc;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GPView {
    public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=utf-8";

    private File viewFile;
    public GPView(File viewFile) {
        this.viewFile = viewFile;
    }
    public String getContentType() {
        return DEFAULT_CONTENT_TYPE;
    }
    // GPView 是基于 HTML 文件来对页面进行渲染的，在模板页面中扫描到￥{name} 这样的表达式
    // 就会从 ModelAndView 的 Model 中找到 name 所对应的值，并且用正则表达式将其替换
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuffer sb = new StringBuffer();
        RandomAccessFile ra = new RandomAccessFile(this.viewFile, "r");

        try {
            String line = null;
            while (null != (line = ra.readLine())) {
                line = new String(line.getBytes("ISO-8859-1"), "utf-8");
                Pattern pattern = Pattern.compile("￥\\{[^\\}]+\\}", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
                    String paramName = matcher.group();
                    paramName = paramName.replaceAll("￥\\{|\\}", "");
                    Object paramValue = model.get(paramName);
                    if (null == paramValue) { continue; }
                    // 要把￥{}中间的这个字符串取出来
                    line = matcher.replaceFirst(makeStringForRegExp(paramValue.toString()));
                    matcher = pattern.matcher(line);
                }
                sb.append(line);
            }
        } finally {
            ra.close();
        }
        response.setCharacterEncoding("utf-8");
//        response.setContentType(DEFAULT_CONTENT_TYPE);
        response.getWriter().write(sb.toString());
    }

    public static String makeStringForRegExp(String str) {
        return str.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }
}

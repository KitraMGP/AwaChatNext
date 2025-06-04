package kitra.awachat.next.util;

public class UserInfoUtil {
    /**
     * 对用户名进行正则表达式判断（只能由字母或数字组成，长度1-24位）
     */
    public static boolean validateUsername(String username) {
        return username.matches("\\w{1,24}");
    }

    /**
     * 对昵称进行校验（不能为空，长度1-64）
     */
    public static boolean validateNickname(String nickname) {
        if (nickname.isBlank()) {
            return false;
        }
        return nickname.length() <= 64;
    }

    /**
     * 对密码进行校验（字母、数字或特殊符号，长度8-32位）
     */
    public static boolean validatePassword(String password) {
        return password.matches("[\\x21-\\x7E]{8,32}");
    }

    /**
     * 对个人描述进行校验（长度不超过255）
     */
    public static boolean validateDescription(String description) {
        return description.length() <= 255;
    }
}

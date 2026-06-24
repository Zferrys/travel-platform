package com.travel.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * 验证码生成工具
 * 生成 4 位数字+干扰线+噪点的图片验证码
 */
public class CaptchaUtil {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 44;
    private static final int CODE_COUNT = 4;
    private static final Random RANDOM = new Random();
    private static final char[] CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    /**
     * 生成随机验证码字符串
     */
    public static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_COUNT);
        for (int i = 0; i < CODE_COUNT; i++) {
            sb.append(CHARS[RANDOM.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }

    /**
     * 生成验证码图片（PNG字节数组）
     */
    public static byte[] generateImage(String code) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(new Color(240, 245, 250));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 干扰线（4条）
        for (int i = 0; i < 4; i++) {
            g.setColor(randomColor(160, 200));
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 噪点（30个）
        for (int i = 0; i < 30; i++) {
            g.setColor(randomColor(150, 200));
            int x = RANDOM.nextInt(WIDTH);
            int y = RANDOM.nextInt(HEIGHT);
            g.fillRect(x, y, 1, 1);
        }

        // 绘制验证码文字
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font[] fonts = {
            new Font("Arial", Font.BOLD, 28),
            new Font("Georgia", Font.BOLD, 26),
            new Font("Verdana", Font.BOLD, 27)
        };

        for (int i = 0; i < code.length(); i++) {
            g.setFont(fonts[RANDOM.nextInt(fonts.length)]);
            g.setColor(randomColor(20, 100));
            // 随机旋转 + 偏移
            double angle = (RANDOM.nextDouble() - 0.5) * 0.4;
            g.rotate(angle, 25 + i * 25, 30);
            g.drawString(String.valueOf(code.charAt(i)), 18 + i * 26, 32);
            g.rotate(-angle, 25 + i * 25, 30);
        }

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    private static Color randomColor(int min, int max) {
        int r = min + RANDOM.nextInt(max - min);
        int g = min + RANDOM.nextInt(max - min);
        int b = min + RANDOM.nextInt(max - min);
        return new Color(r, g, b);
    }
}

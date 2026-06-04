package com.example.vendeFacil.util;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

// Utilitario para padronizar a logo da loja no formato "iFood": um quadrado
// de 400x400 pixels. A imagem e redimensionada preservando a proporcao (sem
// cortar) e as bordas sao preenchidas com a cor de fundo do app.
public final class ImagemUtil {

    private static final int TAMANHO = 400;
    private static final Color FUNDO = new Color(0xFA, 0xF6, 0xF0); // cor "cream" do app

    private ImagemUtil() {
    }

    public static byte[] paraQuadrado400(byte[] original) {
        if (original == null || original.length == 0) {
            throw new IllegalArgumentException("Imagem vazia");
        }
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(original));
            if (img == null) {
                throw new IllegalArgumentException("Arquivo de imagem inválido");
            }

            double escala = Math.min((double) TAMANHO / img.getWidth(),
                                     (double) TAMANHO / img.getHeight());
            int novaLargura = Math.max(1, (int) Math.round(img.getWidth() * escala));
            int novaAltura = Math.max(1, (int) Math.round(img.getHeight() * escala));
            int x = (TAMANHO - novaLargura) / 2;
            int y = (TAMANHO - novaAltura) / 2;

            BufferedImage canvas = new BufferedImage(TAMANHO, TAMANHO, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = canvas.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(FUNDO);
            g.fillRect(0, 0, TAMANHO, TAMANHO);
            g.drawImage(img, x, y, novaLargura, novaAltura, null);
            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(canvas, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível processar a imagem");
        }
    }
}
